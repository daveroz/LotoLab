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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The one screen: swipe to a lottery, pick one of its draws, then a tab. Summary brings what matters at a glance;
 * History and Stats go deeper. Glass cards over the Worldly stage; each tab's numbers are worked out on a background
 * thread and its cards built when they're ready. Text follows the device language.
 */
public class MainActivity extends Activity {
    static final int SUMMARY = 0, HISTORY = 1, STATS = 2;
    static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final int REQ_NOTIFY = 3;

    Ui ui;
    History history;
    private StageView stage;
    private ScrollView scroll;
    private LogoCarousel carousel;
    private LinearLayout drawBox, tabBox, body;
    private View refresh;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private int generation;
    private boolean fetching;

    // what's chosen (kept between launches)
    String lottery;
    int gameIdx;       // index into the lottery's draws
    int tab;
    int limit = 40;    // History: days shown
    double checkWager = 1000; // El Dorado's prize list on Summary

    /** A snapshot of the choice, handed to the background work. */
    static final class Sel {
        double checkWager;
        long fetchAt;
        String fetchLog;
        android.content.Context ctx;
        History h;
        String lottery;
        List<Game> games;
        int days;
        Sample sample;
        int limit;
    }

    static String tabName(int i) {
        switch (i) {
            case SUMMARY: return T.t("Summary");
            case HISTORY: return T.t("History");
            default: return T.t("Stats");
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
        gameIdx = Math.max(0, Math.min(Game.of(lottery).size() - 1, sp.getInt("game", 0)));
        tab = SUMMARY; // Summary first, every time the app opens
        fromIntent(getIntent());

        FrameLayout room = new FrameLayout(this);
        stage = new StageView(this);
        room.addView(stage, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scroll = new ScrollView(this);
        scroll.setBackgroundColor(0);
        scroll.setFillViewport(true);
        // glass picks up the stage's light behind it, so it's redrawn as the page moves under the light
        final int[] lastY = {0};
        scroll.setOnScrollChangeListener((v, x, y, ox, oy) -> {
            if (Math.abs(y - lastY[0]) < ui.dp(6)) return;
            lastY[0] = y;
            Glass.refresh();
        });
        room.addView(scroll, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        room.addView(new StageView.Film(this, 0), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout col = ui.vbox();
        col.setPadding(ui.dp(16), ui.dp(8), ui.dp(16), ui.dp(28));
        scroll.addView(col, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- the lotteries: swipe through their emblems, the refresh button in the top corner ----
        String[] subs = {T.t("Chance · 3 draws a day"), T.t("5 of 43 + superbalota"), T.t("5 of 39 · 4 draws a week")};
        int[] colors = {0xFFF2B230, 0xFFEB9438, 0xFFE08A33};
        carousel = new LogoCarousel(this, Game.LOTTERIES, subs, colors,
                java.util.Arrays.asList(Game.LOTTERIES).indexOf(lottery), i -> {
                    lottery = Game.LOTTERIES[i];
                    gameIdx = 0; // always the lottery's first draw (Mañana, Baloto...)
                    limit = 40;
                    buildSelectors();
                    render();
                });
        FrameLayout top = new FrameLayout(this);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.topMargin = ui.dp(8);
        top.addView(carousel, cp);
        // small, in the corner above the next lottery's emblem peeking in at the side
        refresh = ui.iconButton(Icon.Kind.REFRESH, T.t("Fetch the newest results"), 34, v -> fetch(true));
        top.addView(refresh, new FrameLayout.LayoutParams(ui.dp(34), ui.dp(34), Gravity.TOP | Gravity.END));
        col.addView(top, ui.matchWrap(0));

        drawBox = ui.vbox();
        col.addView(drawBox, ui.matchWrap(2));
        tabBox = ui.vbox();
        col.addView(tabBox, ui.matchWrap(6));
        body = ui.vbox();
        col.addView(body, ui.matchWrap(0));
        setContentView(room);

        // Load the history off the main thread (the bundled files hold several thousand draws).
        body.addView(loading(T.t("Loading the draw history…")));
        worker.execute(() -> {
            History loaded;
            try {
                loaded = Data.history(this);
            } catch (Throwable e) {
                android.util.Log.e("LotoLab", "couldn't load the history", e);
                loaded = new History();
            }
            final History h = loaded;
            runOnUiThread(() -> {
                history = h;
                buildSelectors();
                render();
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
        gameIdx = Math.max(0, idx); // the draw announced, or the lottery's first draw
        tab = SUMMARY;
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
        Data.sp(this).edit().putString("lottery", lottery).putInt("game", gameIdx).apply();
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
        spin();
        new Thread(() -> {
            Fetcher.Status got = null;
            try {
                got = Fetcher.refresh(getApplicationContext(), history, everything);
                Notifier.markSeen(getApplicationContext(), got.fresh); // shown here, no need to announce them later
                Notifier.schedule(getApplicationContext());
            } catch (Throwable e) {
                android.util.Log.e("LotoLab", "refresh failed", e); // keep what's stored; try again later
            }
            final Fetcher.Status st = got;
            runOnUiThread(() -> {
                fetching = false;
                if (st == null || st.added > 0 || tab == HISTORY || tab == SUMMARY) render(true);
            });
        }).start();
    }

    /** The refresh button turns while the newest results are being fetched. */
    private void spin() {
        if (refresh == null) return;
        refresh.animate().rotationBy(360).setDuration(900).setInterpolator(new android.view.animation.LinearInterpolator())
                .withEndAction(() -> {
                    if (fetching) spin();
                    else refresh.setRotation(0);
                }).start();
    }

    // ------------------------------------------------------------------ choices

    List<Game> chosenGames() {
        List<Game> all = Game.of(lottery);
        List<Game> one = new ArrayList<>();
        one.add(all.get(Math.max(0, Math.min(all.size() - 1, gameIdx))));
        return one;
    }

    void buildSelectors() {
        // the lottery's draws, then the sections: two segmented controls of the same size
        drawBox.removeAllViews();
        List<Game> games = Game.of(lottery);
        if (games.size() > 1) {
            String[] names = new String[games.size()];
            for (int i = 0; i < names.length; i++) names[i] = games.get(i).shortName();
            ui.segmented(drawBox, names, gameIdx, idx -> {
                gameIdx = idx;
                buildSelectors();
                render();
            }, 4);
        }
        tabBox.removeAllViews();
        String[] tabs = new String[STATS + 1];
        for (int i = 0; i < tabs.length; i++) tabs[i] = tabName(i);
        ui.segmented(tabBox, tabs, tab, idx -> {
            tab = idx;
            buildSelectors();
            render();
        }, 8);
    }

    Sel snapshot() {
        Sel s = new Sel();
        s.h = history;
        s.lottery = lottery;
        s.games = chosenGames();
        s.days = 0; // every tab looks at the whole history
        s.limit = limit;
        s.checkWager = checkWager;
        s.fetchAt = Data.sp(this).getLong("fetchAt", 0);
        s.fetchLog = Data.sp(this).getString("fetchLog", "");
        s.ctx = getApplicationContext();
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
            body.addView(loading(T.t("Working it out…")));
        }
        worker.execute(() -> {
            Object data;
            try {
                sel.sample = sel.h.sample(sel.games, sel.days);
                switch (t) {
                    case SUMMARY: data = SummaryTab.compute(sel); break;
                    case HISTORY: data = HistoryTab.compute(sel); break;
                    default: data = StatsTab.compute(sel);
                }
            } catch (Throwable e) {
                android.util.Log.e("LotoLab", "couldn't work out tab " + t, e);
                data = e instanceof RuntimeException ? e : new RuntimeException(e);
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
                try {
                    switch (t) {
                        case SUMMARY: SummaryTab.show(this, body, sel, result); break;
                        case HISTORY: HistoryTab.show(this, body, sel, result); break;
                        default: StatsTab.show(this, body, sel, result);
                    }
                } catch (RuntimeException e) {
                    android.util.Log.e("LotoLab", "couldn't show tab " + t, e);
                    body.removeAllViews();
                    LinearLayout c = ui.card(body, T.t("SOMETHING WENT WRONG"));
                    c.addView(ui.body(T.f("This view couldn't be worked out: %s", String.valueOf(e))));
                }
                if (inPlace) scroll.post(() -> scroll.scrollTo(0, y));
            });
        });
    }

    /** Re-renders keeping the scroll position (for choices made inside a tab). */
    void rerenderInPlace() { render(true); }

    private View loading(String msg) {
        LinearLayout c = ui.vbox();
        c.setBackground(Theme.glass(this, Ui.CARD_RADIUS));
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
        c.addView(a.ui.body(T.f("There are no stored results for %s yet. Tap ↻ to fetch the newest results.", label(s))));
    }

    static String label(Sel s) {
        return s.games.get(0).name;
    }

    /** "Dorado Mañana · 1,155 draws (10 Jan 2023 – 18 Nov 2026)". */
    static String scope(Sel s) {
        Sample sm = s.sample;
        String base = T.f("%s · %s draws", label(s), Fmt.num(sm.size()));
        return sm.size() == 0 ? base : base + " (" + Fmt.shortDate(sm.from()) + " – " + Fmt.shortDate(sm.to()) + ")";
    }
}
