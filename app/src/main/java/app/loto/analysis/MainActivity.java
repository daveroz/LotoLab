package app.loto.analysis;

import android.app.Activity;
import android.content.SharedPreferences;
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
 * The one screen: choose a lottery, a draw (or all of its draws) and a period, then a tab. Glass cards over the
 * Worldly stage. Each tab's numbers are worked out on a background thread and the cards built when they're ready.
 */
public class MainActivity extends Activity {
    static final String[] TABS = {"Results", "Stats", "Randomness", "Picks", "Integrity", "Odds"};
    static final int[] PERIODS = {30, 90, 365, 0};
    static final String[] PERIOD_LABELS = {"30 days", "90 days", "1 year", "All"};
    static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    Ui ui;
    History history;
    private StageView stage;
    private ScrollView scroll;
    private LinearLayout selectors, body;
    private TextView statusLine;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private int generation;
    private boolean fetching;

    // what's chosen (kept between launches)
    String lottery;
    int gameIdx;       // index into the lottery's draws; == size for "all"
    int periodIdx;
    int tab;
    int weekday = -1;  // Results filter: -1 all, 0 Monday .. 6 Sunday
    int limit = 40;    // Results: days shown
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

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ui = new Ui(this);
        SharedPreferences sp = Data.sp(this);
        lottery = sp.getString("lottery", "Dorado");
        if (Game.of(lottery).isEmpty()) lottery = "Dorado";
        gameIdx = sp.getInt("game", Game.of(lottery).size() > 1 ? Game.of(lottery).size() : 0);
        periodIdx = Math.max(0, Math.min(PERIODS.length - 1, sp.getInt("period", 3)));
        tab = Math.max(0, Math.min(TABS.length - 1, sp.getInt("tab", 0)));
        try {
            model = Predictor.Model.valueOf(sp.getString("model", "FREQUENCY"));
        } catch (IllegalArgumentException ignored) { }

        FrameLayout room = new FrameLayout(this);
        stage = new StageView(this);
        room.addView(stage, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scroll = new ScrollView(this);
        scroll.setBackgroundColor(0);
        scroll.setFillViewport(true);
        room.addView(scroll, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        room.addView(new StageView.Film(this, 0), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout col = ui.vbox();
        col.setPadding(ui.dp(16), ui.dp(30), ui.dp(16), ui.dp(28));
        scroll.addView(col, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- header: a lit badge, the title, and the data status with a refresh button ----
        LinearLayout head = ui.hbox();
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView badge = ui.text("LOTO", 11, Theme.monoBold(this), Theme.LED);
        badge.setLetterSpacing(0.08f);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(Theme.lit(this, 12, false));
        head.addView(badge, new LinearLayout.LayoutParams(ui.dp(48), ui.dp(48)));
        LinearLayout titles = ui.vbox();
        titles.setPadding(ui.dp(12), 0, 0, 0);
        titles.addView(ui.text("LotoLab", 19, Theme.uiBold(this), Theme.TEXT));
        TextView tagline = ui.label("Dorado · Paisita · Baloto · MiLoto", Theme.AMBER_LIGHT);
        tagline.setLetterSpacing(0.14f);
        titles.addView(tagline);
        head.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView refresh = ui.secondaryButton("↻", v -> fetch(true));
        refresh.setTextSize(16);
        refresh.setPadding(ui.dp(14), ui.dp(8), ui.dp(14), ui.dp(8));
        head.addView(refresh);
        col.addView(head);
        statusLine = ui.note("");
        statusLine.setPadding(ui.dp(2), ui.dp(10), 0, 0);
        col.addView(statusLine);

        selectors = ui.vbox();
        col.addView(selectors, ui.matchWrap(4));
        body = ui.vbox();
        col.addView(body, ui.matchWrap(0));
        setContentView(room);

        // Load the history off the main thread (the bundled files hold several thousand draws).
        body.addView(loading("Loading the draw history…"));
        worker.execute(() -> {
            History h = Data.history(this);
            runOnUiThread(() -> {
                history = h;
                buildSelectors();
                render();
                updateStatus();
                fetch(false);
            });
        });
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
                .putInt("tab", tab).putString("model", model.name()).apply();
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
        statusLine.setText("Fetching the newest results…");
        new Thread(() -> {
            Fetcher.Status st = Fetcher.refresh(getApplicationContext(), history, everything);
            runOnUiThread(() -> {
                fetching = false;
                updateStatus();
                if (st.added > 0 || tab == 0) render(true);
            });
        }).start();
    }

    void updateStatus() {
        if (history == null) return;
        int total = 0;
        for (Game g : Game.ALL) total += history.size(g);
        long at = Data.sp(this).getLong("fetchAt", 0);
        String when = at == 0 ? "not fetched yet" : "updated " + ZonedDateTime.ofInstant(Instant.ofEpochMilli(at), ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.US));
        String log = Data.sp(this).getString("fetchLog", "");
        int failed = 0;
        for (String line : log.split("\n")) if (line.contains("\tfail\t")) failed++;
        statusLine.setText(String.format(Locale.US, "%,d draws stored · %s%s", total, when,
                failed > 0 ? " · " + failed + " source" + (failed > 1 ? "s" : "") + " unreachable (see Results)" : ""));
    }

    // ------------------------------------------------------------------ choices

    List<Game> chosenGames() {
        List<Game> all = Game.of(lottery);
        if (gameIdx >= all.size()) return new ArrayList<>(all);
        List<Game> one = new ArrayList<>();
        one.add(all.get(gameIdx));
        return one;
    }

    void buildSelectors() {
        selectors.removeAllViews();
        LinearLayout row = ui.chipRow(selectors, 14);
        for (String l : Game.LOTTERIES) {
            row.addView(ui.chip(l, l.equals(lottery), v -> {
                if (l.equals(lottery)) return;
                lottery = l;
                int n = Game.of(l).size();
                gameIdx = n > 1 ? n : 0;
                weekday = -1;
                limit = 40;
                check = "";
                buildSelectors();
                render();
            }), ui.wrap(row.getChildCount() == 0 ? 0 : 8));
        }
        ui.reveal(row, java.util.Arrays.asList(Game.LOTTERIES).indexOf(lottery));
        List<Game> games = Game.of(lottery);
        if (games.size() > 1) {
            LinearLayout gr = ui.chipRow(selectors, 8);
            for (int i = 0; i <= games.size(); i++) {
                final int idx = i;
                String label = i == games.size() ? "All draws" : games.get(i).shortName + " · " + slotWord(games.get(i));
                gr.addView(ui.chip(label, idx == gameIdx, v -> {
                    gameIdx = idx;
                    buildSelectors();
                    render();
                }), ui.wrap(i == 0 ? 0 : 8));
            }
            ui.reveal(gr, gameIdx);
        }
        LinearLayout pr = ui.chipRow(selectors, 8);
        for (int i = 0; i < PERIODS.length; i++) {
            final int idx = i;
            pr.addView(ui.chip(PERIOD_LABELS[i], i == periodIdx, v -> {
                periodIdx = idx;
                buildSelectors();
                render();
            }), ui.wrap(i == 0 ? 0 : 8));
        }
        LinearLayout tr = ui.chipRow(selectors, 16);
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            TextView t = ui.chip(TABS[i].toUpperCase(Locale.US), i == tab, v -> {
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
    }

    static String slotWord(Game g) {
        switch (g.slot) {
            case MORNING: return "morning";
            case AFTERNOON: return "afternoon";
            default: return "night";
        }
    }

    Sel snapshot() {
        Sel s = new Sel();
        s.h = history;
        s.lottery = lottery;
        s.games = chosenGames();
        s.days = PERIODS[periodIdx];
        s.periodLabel = PERIOD_LABELS[periodIdx];
        s.weekday = weekday;
        s.limit = limit;
        s.model = model;
        s.seed = seed;
        s.check = check;
        return s;
    }

    // ------------------------------------------------------------------ rendering

    /** Rebuilds the body for the current choice, doing the arithmetic in the background. */
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
            body.addView(loading(t == 4 ? "Running every test on every draw…" : "Working it out…"));
        }
        worker.execute(() -> {
            Object data;
            try {
                sel.sample = sel.h.sample(sel.games, sel.days);
                switch (t) {
                    case 0: data = ResultsTab.compute(sel); break;
                    case 1: data = StatsTab.compute(sel); break;
                    case 2: data = RandomTab.compute(sel); break;
                    case 3: data = PicksTab.compute(sel); break;
                    case 4: data = IntegrityTab.compute(sel); break;
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
                    LinearLayout c = ui.card(body, "SOMETHING WENT WRONG");
                    c.addView(ui.body("This view couldn't be worked out: " + ((RuntimeException) result).getMessage()));
                    return;
                }
                switch (t) {
                    case 0: ResultsTab.show(this, body, sel, result); break;
                    case 1: StatsTab.show(this, body, sel, result); break;
                    case 2: RandomTab.show(this, body, sel, result); break;
                    case 3: PicksTab.show(this, body, sel, result); break;
                    case 4: IntegrityTab.show(this, body, sel, result); break;
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
        LinearLayout c = a.ui.card(body, "NO DRAWS");
        c.addView(a.ui.body("There are no stored results for " + label(s) + " in this period yet. Tap ↻ to fetch the newest "
                + "results, or choose a longer period."));
    }

    static String label(Sel s) {
        List<Game> all = Game.of(s.lottery);
        if (s.games.size() == all.size() && all.size() > 1) return s.lottery + " (all draws)";
        return s.games.get(0).name;
    }

    /** "Dorado Mañana · 1 year · 288 draws (19 Nov 2025 – 17 Nov 2026)". */
    static String scope(Sel s) {
        Sample sm = s.sample;
        String range = sm.size() == 0 ? "" : " (" + Fmt.shortDate(sm.from()) + " – " + Fmt.shortDate(sm.to()) + ")";
        return label(s) + " · " + s.periodLabel + " · " + String.format(Locale.US, "%,d", sm.size()) + " draws" + range;
    }
}
