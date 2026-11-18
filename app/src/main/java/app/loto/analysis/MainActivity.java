package app.loto.analysis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The one screen: swipe to a lottery, pick one of its draws (or all of them), then a tab. Summary brings what
 * matters at a glance; the other tabs go deeper. Glass cards over the Worldly stage; each tab's numbers are
 * worked out on a background thread and its cards built when they're ready. Text follows the device language.
 */
public class MainActivity extends Activity {
    static final int SUMMARY = 0, HISTORY = 1, STATS = 2, RANDOM = 3, PICKS = 4, INTEGRITY = 5, ODDS = 6;
    static final int[] PERIODS = {30, 90, 365, 0};
    static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final int REQ_NOTIFY = 3;

    Ui ui;
    History history;
    private StageView stage;
    private ScrollView scroll;
    private LogoCarousel carousel;
    private LinearLayout drawBox, tabBox, periodBox, body;
    private TextView statusLine;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private int generation;
    private boolean fetching;

    // what's chosen (kept between launches)
    String lottery;
    int gameIdx;       // index into the lottery's draws; == size for "all"
    int periodIdx;
    int tab;
    int weekday = -1;  // History filter: -1 all, 0 Monday .. 6 Sunday
    int limit = 40;    // History: days shown
    Predictor.Model model = Predictor.Model.FREQUENCY;
    long seed = 1;
    String check = "";

    /** A snapshot of the choice, handed to the background work. */
    static final class Sel {
        History h;
        String lottery;
        List<Game> games;
        int days;
        Sample sample;
        int weekday, limit;
        Predictor.Model model;
        long seed;
        String check;
        String periodLabel;
    }

    static String tabName(int i) {
        switch (i) {
            case SUMMARY: return T.t("Summary");
            case HISTORY: return T.t("History");
            case STATS: return T.t("Stats");
            case RANDOM: return T.t("Randomness");
            case PICKS: return T.t("Picks");
            case INTEGRITY: return T.t("Integrity");
            default: return T.t("Odds");
        }
    }

    static String periodName(int i) {
        switch (i) {
            case 0: return T.t("30 days");
            case 1: return T.t("90 days");
            case 2: return T.t("1 year");
            default: return T.t("All");
        }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        T.init(Locale.getDefault());
        ui = new Ui(this);
        SharedPreferences sp = Data.sp(this);
        lottery = sp.getString("lottery", "Dorado");
        if (Game.of(lottery).isEmpty()) lottery = "Dorado";
        gameIdx = sp.getInt("game", Game.of(lottery).size() > 1 ? Game.of(lottery).size() : 0);
        periodIdx = Math.max(0, Math.min(PERIODS.length - 1, sp.getInt("period", 3)));
        tab = SUMMARY; // Summary first, every time the app opens
        try {
            model = Predictor.Model.valueOf(sp.getString("model", "FREQUENCY"));
        } catch (IllegalArgumentException ignored) { }
        fromIntent(getIntent());

        FrameLayout room = new FrameLayout(this);
        stage = new StageView(this);
        room.addView(stage, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scroll = new ScrollView(this);
        scroll.setBackgroundColor(0);
        scroll.setFillViewport(true);
        room.addView(scroll, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        room.addView(new StageView.Film(this, 0), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout col = ui.vbox();
        col.setPadding(ui.dp(16), ui.dp(26), ui.dp(16), ui.dp(28));
        scroll.addView(col, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- header: title and the data status, with a refresh button ----
        LinearLayout head = ui.hbox();
        head.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titles = ui.vbox();
        titles.addView(ui.text("LotoLab", 19, Theme.uiBold(this), Theme.TEXT));
        statusLine = ui.note("");
        statusLine.setTextSize(11);
        titles.addView(statusLine);
        head.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView refresh = ui.secondaryButton("↻", v -> fetch(true));
        refresh.setTextSize(16);
        refresh.setPadding(ui.dp(14), ui.dp(8), ui.dp(14), ui.dp(8));
        refresh.setContentDescription(T.t("Fetch the newest results"));
        head.addView(refresh);
        col.addView(head);

        // ---- the lotteries: swipe through their emblems ----
        String[] subs = {T.t("Chance · 3 draws a day"), T.t("Chance · day, night and Saturday"),
                T.t("5 of 43 + superbalota"), T.t("5 of 39 · 4 draws a week")};
        int[] res = {R.drawable.logo_dorado, R.drawable.logo_paisita, R.drawable.logo_baloto, R.drawable.logo_miloto};
        int[] colors = {0xFFF2B230, 0xFF3FAE6A, 0xFFF5A623, 0xFF8E7CF0};
        carousel = new LogoCarousel(this, Game.LOTTERIES, Game.LOTTERIES, subs, res, colors,
                java.util.Arrays.asList(Game.LOTTERIES).indexOf(lottery), i -> {
                    lottery = Game.LOTTERIES[i];
                    int n = Game.of(lottery).size();
                    gameIdx = n > 1 ? n : 0;
                    weekday = -1;
                    limit = 40;
                    check = "";
                    buildSelectors();
                    render();
                });
        col.addView(carousel, ui.matchWrap(10));

        drawBox = ui.vbox();
        col.addView(drawBox, ui.matchWrap(2));
        tabBox = ui.vbox();
        col.addView(tabBox, ui.matchWrap(6));
        periodBox = ui.vbox();
        col.addView(periodBox, ui.matchWrap(0));
        body = ui.vbox();
        col.addView(body, ui.matchWrap(0));
        setContentView(room);

        // Load the history off the main thread (the bundled files hold several thousand draws).
        body.addView(loading(T.t("Loading the draw history…")));
        worker.execute(() -> {
            History h = Data.history(this);
            runOnUiThread(() -> {
                history = h;
                buildSelectors();
                render();
                updateStatus();
                fetch(false);
                Notifier.schedule(getApplicationContext());
            });
        });

        // Ask once for permission to announce results (Android 13+).
        if (Build.VERSION.SDK_INT >= 33 && !sp.getBoolean("notifyAsked", false)) {
            sp.edit().putBoolean("notifyAsked", true).apply();
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFY);
            }
        }
    }

    /** A notification was tapped: show that lottery and draw on the Summary. */
    private boolean fromIntent(Intent i) {
        if (i == null || i.getStringExtra("lottery") == null) return false;
        String l = i.getStringExtra("lottery");
        if (Game.of(l).isEmpty()) return false;
        lottery = l;
        List<Game> games = Game.of(l);
        Game g = Game.byId(i.getStringExtra("game"));
        int idx = g == null ? -1 : games.indexOf(g);
        gameIdx = idx >= 0 ? idx : (games.size() > 1 ? games.size() : 0);
        tab = SUMMARY;
        check = "";
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (fromIntent(intent) && history != null) {
            recreate(); // simplest way to move the carousel and every row to the new choice
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == REQ_NOTIFY && tab == SUMMARY) render(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stage.setRunning(true);
        if (history != null && System.currentTimeMillis() - Data.sp(this).getLong("fetchAt", 0) > 15 * 60_000L) fetch(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stage.setRunning(false);
        Data.sp(this).edit().putString("lottery", lottery).putInt("game", gameIdx).putInt("period", periodIdx)
                .putString("model", model.name()).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        worker.shutdownNow();
    }

    // ------------------------------------------------------------------ data

    private void fetch(boolean everything) {
        if (fetching || history == null) return;
        fetching = true;
        statusLine.setText(T.t("Fetching the newest results…"));
        new Thread(() -> {
            Fetcher.Status st = Fetcher.refresh(getApplicationContext(), history, everything);
            Notifier.markSeen(getApplicationContext(), st.fresh); // shown here, no need to announce them later
            Notifier.schedule(getApplicationContext());
            runOnUiThread(() -> {
                fetching = false;
                updateStatus();
                if (st.added > 0 || tab == HISTORY || tab == SUMMARY) render(true);
            });
        }).start();
    }

    void updateStatus() {
        if (history == null) return;
        int total = 0;
        for (Game g : Game.ALL) total += history.size(g);
        long at = Data.sp(this).getLong("fetchAt", 0);
        String when;
        if (at == 0) {
            when = T.t("not fetched yet");
        } else {
            ZonedDateTime z = ZonedDateTime.ofInstant(Instant.ofEpochMilli(at), ZoneId.systemDefault());
            when = T.f("updated %s, %s", Fmt.shortDate(z.toLocalDate()), String.format(Locale.US, "%02d:%02d", z.getHour(), z.getMinute()));
        }
        String log = Data.sp(this).getString("fetchLog", "");
        int failed = 0;
        for (String line : log.split("\n")) if (line.contains("\tfail\t")) failed++;
        String s = T.f("%s draws stored · %s", Fmt.num(total), when);
        if (failed > 0) s += " · " + T.f("%d source(s) unreachable (see History)", failed);
        statusLine.setText(s);
    }

    // ------------------------------------------------------------------ choices

    List<Game> chosenGames() {
        List<Game> all = Game.of(lottery);
        if (gameIdx >= all.size()) return new ArrayList<>(all);
        List<Game> one = new ArrayList<>();
        one.add(all.get(gameIdx));
        return one;
    }

    /** "Mañana · morning" in English; in Spanish the draw's own name says it already. */
    static String drawLabel(Game g) {
        if (T.es || !g.digits()) return g.shortName();
        String w;
        switch (g.slot) {
            case MORNING: w = "morning"; break;
            case AFTERNOON: w = "afternoon"; break;
            default: w = "night";
        }
        return g.shortName() + " · " + w;
    }

    void buildSelectors() {
        drawBox.removeAllViews();
        List<Game> games = Game.of(lottery);
        if (games.size() > 1) {
            LinearLayout gr = ui.chipRow(drawBox, 4, true);
            for (int i = 0; i <= games.size(); i++) {
                final int idx = i;
                String label = i == games.size() ? T.t("All draws") : drawLabel(games.get(i));
                gr.addView(ui.chip(label, idx == gameIdx, v -> {
                    gameIdx = idx;
                    buildSelectors();
                    render();
                }), ui.wrap(i == 0 ? 0 : 8));
            }
            ui.reveal(gr, gameIdx);
        }
        tabBox.removeAllViews();
        LinearLayout tr = ui.chipRow(tabBox, 10, false);
        for (int i = 0; i <= ODDS; i++) {
            final int idx = i;
            TextView t = ui.chip(tabName(i).toUpperCase(T.L), i == tab, v -> {
                tab = idx;
                buildSelectors();
                render();
            });
            t.setLetterSpacing(0.14f);
            t.setTextSize(11);
            t.setTypeface(Theme.uiBold(this));
            tr.addView(t, ui.wrap(i == 0 ? 0 : 8));
        }
        ui.reveal(tr, tab);
        periodBox.removeAllViews();
        if (tab != SUMMARY && tab != ODDS) {
            LinearLayout pr = ui.chipRow(periodBox, 8, false);
            for (int i = 0; i < PERIODS.length; i++) {
                final int idx = i;
                pr.addView(ui.chip(periodName(i), i == periodIdx, v -> {
                    periodIdx = idx;
                    buildSelectors();
                    render();
                }), ui.wrap(i == 0 ? 0 : 8));
            }
        }
    }

    Sel snapshot() {
        Sel s = new Sel();
        s.h = history;
        s.lottery = lottery;
        s.games = chosenGames();
        boolean periodApplies = tab != SUMMARY && tab != ODDS;
        s.days = periodApplies ? PERIODS[periodIdx] : 0;
        s.periodLabel = periodApplies ? periodName(periodIdx) : T.t("All");
        s.weekday = weekday;
        s.limit = limit;
        s.model = model;
        s.seed = seed;
        s.check = check;
        return s;
    }

    // ------------------------------------------------------------------ rendering

    void render() { render(false); }

    /**
     * inPlace: keep showing the current cards while working (a choice made inside a tab, or new results arriving),
     * then swap them and keep the scroll position.
     */
    void render(boolean inPlace) {
        if (history == null) return;
        final int gen = ++generation;
        final Sel sel = snapshot();
        final int t = tab;
        if (!inPlace || body.getChildCount() == 0) {
            body.removeAllViews();
            body.addView(loading(t == INTEGRITY ? T.t("Running every test on every draw…") : T.t("Working it out…")));
        }
        worker.execute(() -> {
            Object data;
            try {
                sel.sample = sel.h.sample(sel.games, sel.days);
                switch (t) {
                    case SUMMARY: data = SummaryTab.compute(sel); break;
                    case HISTORY: data = HistoryTab.compute(sel); break;
                    case STATS: data = StatsTab.compute(sel); break;
                    case RANDOM: data = RandomTab.compute(sel); break;
                    case PICKS: data = PicksTab.compute(sel); break;
                    case INTEGRITY: data = IntegrityTab.compute(sel); break;
                    default: data = OddsTab.compute(sel);
                }
            } catch (RuntimeException e) {
                data = e;
            }
            final Object result = data;
            runOnUiThread(() -> {
                if (gen != generation || isFinishing()) return;
                final int y = scroll.getScrollY();
                body.removeAllViews();
                if (result instanceof RuntimeException) {
                    LinearLayout c = ui.card(body, T.t("SOMETHING WENT WRONG"));
                    c.addView(ui.body(T.f("This view couldn't be worked out: %s", ((RuntimeException) result).getMessage())));
                    return;
                }
                switch (t) {
                    case SUMMARY: SummaryTab.show(this, body, sel, result); break;
                    case HISTORY: HistoryTab.show(this, body, sel, result); break;
                    case STATS: StatsTab.show(this, body, sel, result); break;
                    case RANDOM: RandomTab.show(this, body, sel, result); break;
                    case PICKS: PicksTab.show(this, body, sel, result); break;
                    case INTEGRITY: IntegrityTab.show(this, body, sel, result); break;
                    default: OddsTab.show(this, body, sel, result);
                }
                if (inPlace) scroll.post(() -> scroll.scrollTo(0, y));
            });
        });
    }

    /** Re-renders keeping the scroll position (for choices made inside a tab). */
    void rerenderInPlace() { render(true); }

    private View loading(String msg) {
        LinearLayout c = ui.vbox();
        c.setBackground(Theme.glass(this, 16));
        c.setPadding(ui.dp(14), ui.dp(18), ui.dp(14), ui.dp(18));
        TextView t = ui.body(msg);
        t.setGravity(Gravity.CENTER);
        c.addView(t);
        c.setLayoutParams(ui.matchWrap(14));
        return c;
    }

    /** "Not enough draws" card. */
    static void empty(MainActivity a, LinearLayout body, Sel s) {
        LinearLayout c = a.ui.card(body, T.t("NO DRAWS"));
        c.addView(a.ui.body(T.f("There are no stored results for %s in this period yet. Tap ↻ to fetch the newest results, "
                + "or choose a longer period.", label(s))));
    }

    static String label(Sel s) {
        List<Game> all = Game.of(s.lottery);
        if (s.games.size() == all.size() && all.size() > 1) return T.f("%s (all draws)", s.lottery);
        return s.games.get(0).name;
    }

    /** "Dorado Mañana · 1 year · 288 draws (19 Nov 2025 – 17 Nov 2026)". */
    static String scope(Sel s) {
        Sample sm = s.sample;
        String base = T.f("%s · %s · %s draws", label(s), s.periodLabel, Fmt.num(sm.size()));
        return sm.size() == 0 ? base : base + " (" + Fmt.shortDate(sm.from()) + " – " + Fmt.shortDate(sm.to()) + ")";
    }
}
