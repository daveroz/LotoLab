package app.loto.analysis;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Only what matters at a glance, for the chosen draw: the latest result, the next draw, today's recommendation, for
 * El Dorado what each bet wins, and the notifications switch. Everything else is in the other tabs.
 */
final class SummaryTab {
    private SummaryTab() {}

    static final class Latest {
        Game game;
        Draw draw;
        Randomness.DrawScore audit;
        LocalDate nextDate;
        int nextTime = -1;
        long minutesToNext;
        boolean awaiting;
        int awaitingTime;
        /** The latest scheduled draw whose result should be out by now but isn't stored. */
        LocalDate missing;
        Recommender.Rec rec;
        long generatedAt;
    }

    static final class Out {
        final List<Latest> latest = new ArrayList<>();
        long fetchAt;
        int failedSources;
        boolean stale;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        ZonedDateTime now = ZonedDateTime.now(MainActivity.BOGOTA);
        o.fetchAt = s.fetchAt;
        if (s.fetchLog != null) for (String line : s.fetchLog.split("\n")) if (line.contains("\tfail\t")) o.failedSources++;
        o.stale = s.fetchAt == 0 || System.currentTimeMillis() - s.fetchAt > 6 * 3600_000L;
        for (Game g : s.games) {
            Latest l = new Latest();
            l.game = g;
            List<Game> one = new ArrayList<>();
            one.add(g);
            Sample full = s.h.sample(one, 0);
            if (full.size() > 0) {
                l.draw = full.draws.get(full.size() - 1);
                List<Draw> pair = new ArrayList<>(full.draws.subList(Math.max(0, full.size() - 2), full.size()));
                List<Randomness.DrawScore> sc = Randomness.drawScores(new Sample(one, pair));
                l.audit = sc.get(sc.size() - 1);
            }
            for (int i = 0; i <= 8; i++) {
                LocalDate d = now.toLocalDate().plusDays(i);
                int t = g.timeOn(d);
                if (t < 0) continue;
                ZonedDateTime at = ZonedDateTime.of(d, LocalTime.of(t / 60, t % 60), MainActivity.BOGOTA);
                if (at.isAfter(now)) {
                    l.nextDate = d;
                    l.nextTime = t;
                    l.minutesToNext = java.time.Duration.between(now, at).toMinutes();
                    break;
                }
                if (i == 0 && s.h.on(g, d) == null && java.time.Duration.between(at, now).toMinutes() < 240) {
                    l.awaiting = true;
                    l.awaitingTime = t;
                }
            }
            if (!l.awaiting) l.missing = missing(s.h, g, now);
            if (full.size() >= Recommender.WARMUP) {
                // always a draw still to come, so the recommendation can be played
                LocalDate target = l.nextDate != null ? l.nextDate : now.toLocalDate();
                l.rec = Recommender.recommend(full, target, true);
                l.generatedAt = s.ctx == null ? System.currentTimeMillis() : Data.logRec(s.ctx, l.rec, System.currentTimeMillis());
            }
            o.latest.add(l);
        }
        return o;
    }

    /** The date of the game's next draw still to come (today if it hasn't been held yet), or null within a week. */
    static LocalDate nextDraw(Game g, ZonedDateTime now) {
        for (int i = 0; i <= 8; i++) {
            LocalDate d = now.toLocalDate().plusDays(i);
            int t = g.timeOn(d);
            if (t >= 0 && ZonedDateTime.of(d, LocalTime.of(t / 60, t % 60), MainActivity.BOGOTA).isAfter(now)) return d;
        }
        return null;
    }

    /**
     * The latest scheduled draw (in the past week) whose result should have been published by now (4 hours after the
     * draw) but isn't stored, or null when the history is up to date.
     */
    static LocalDate missing(History h, Game g, ZonedDateTime now) {
        for (int i = 0; i <= 7; i++) {
            LocalDate d = now.toLocalDate().minusDays(i);
            int t = g.timeOn(d);
            if (t < 0) continue;
            ZonedDateTime at = ZonedDateTime.of(d, LocalTime.of(t / 60, t % 60), MainActivity.BOGOTA);
            if (at.plusHours(4).isAfter(now)) continue; // not due yet
            return h.on(g, d) == null && h.size(g) > 0 && d.isAfter(h.last(g)) ? d : null;
        }
        return null;
    }

    // ------------------------------------------------------------------ helpers shared with other tabs

    static String inTime(long minutes) {
        if (minutes < 60) return T.f("in %d min", minutes);
        if (minutes < 48 * 60) return T.f("in %d h %d min", minutes / 60, minutes % 60);
        return T.f("in %d days", (minutes + 12 * 60) / (24 * 60));
    }

    static int auditColor(int score) {
        if (score >= 50) return Theme.GREEN;
        if (score >= 10) return Theme.AMBER_LIGHT;
        if (score >= 1) return Theme.AMBER;
        return Theme.CORAL;
    }

    /** "Evens 2 · Odds 2 · Sum 12 · Pata 20": the figures of a result in one line. */
    static String figures(Game g, int[] nums, int extra) {
        int ev = 0, sum = 0;
        for (int v : nums) {
            sum += v;
            if (v % 2 == 0) ev++;
        }
        String s = T.f("Evens %d · Odds %d · Sum %d", ev, nums.length - ev, sum);
        if (g.digits()) {
            s += " · " + T.f("Pata %d%d", nums[nums.length - 2], nums[nums.length - 1]);
            if (extra >= 0) s += " · " + T.f("Quinta %d", extra);
        }
        return s;
    }

    static String when(long millis) {
        ZonedDateTime z = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), java.time.ZoneId.systemDefault());
        return Fmt.shortDate(z.toLocalDate()) + ", " + String.format(java.util.Locale.US, "%02d:%02d", z.getHour(), z.getMinute());
    }

    /**
     * A mean log score as how much more (or less) probability the forecasts gave the actual results than a fair
     * guess, in percent: e^score − 1. Decimals adapt so tiny differences don't all read as 0.
     */
    static String score(double v) {
        if (Double.isNaN(v)) return "–";
        double pct = (Math.exp(v) - 1) * 100;
        double a = Math.abs(pct);
        int dec = a == 0 || a >= 1 ? 1 : a >= 0.1 ? 2 : a >= 0.01 ? 3 : 4;
        String s = String.format(T.L, "%+." + dec + "f%%", pct);
        return s.replace("-", "−");
    }

    // ------------------------------------------------------------------ display

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        boolean single = o.latest.size() == 1;

        // ---- latest result ----
        LinearLayout lc = ui.card(body, single ? T.t("LATEST RESULT") : T.t("LATEST RESULTS"));
        boolean first = true;
        for (Latest l : o.latest) {
            if (!first) lc.addView(ui.divider());
            first = false;
            LinearLayout head = ui.hbox();
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(ui.text(l.game.name, single ? 18 : 16, Theme.uiBold(a), Theme.TEXT),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            if (l.audit != null) head.addView(ui.tag(T.f("%s · %d/100", l.audit.label(), l.audit.score), auditColor(l.audit.score)));
            lc.addView(head);
            if (l.draw == null) {
                lc.addView(ui.body(T.t("No results stored yet.")));
                continue;
            }
            int time = l.game.usualTime(l.draw.date);
            TextView w = ui.note(Fmt.dayHeading(l.draw.date) + (time >= 0 ? " · " + Fmt.time(time) : ""));
            w.setPadding(0, ui.dp(2), 0, ui.dp(10));
            lc.addView(w);
            lc.addView(ui.balls(l.game, l.draw.nums, l.draw.extra, single ? Ui.NUM_L : Ui.NUM_M));
            TextView fig = ui.text(figures(l.game, l.draw.nums, l.draw.extra), 14, Theme.monoMedium(a), Theme.TEXT);
            fig.setPadding(0, ui.dp(10), 0, 0);
            lc.addView(fig);
            String next = l.awaiting ? T.f("Today's %s draw: result soon.", Fmt.time(l.awaitingTime))
                    : l.nextDate != null ? T.f("Next draw: %s · %s (%s)", Fmt.dayMonthInline(l.nextDate), Fmt.time(l.nextTime), inTime(l.minutesToNext))
                    : T.t("No draw scheduled in the next week.");
            TextView nx = ui.note(next);
            nx.setTextColor(l.awaiting ? Theme.AMBER : Theme.TEXT_SOFT);
            nx.setPadding(0, ui.dp(8), 0, 0);
            lc.addView(nx);
            if (l.missing != null) warn(a, lc, T.f("Result of %s missing; checking again soon.", Fmt.dayMonthInline(l.missing)));
        }
        // only say something about the data when something is wrong with it
        if (o.fetchAt == 0) warn(a, lc, T.t("Not updated yet: showing the bundled history."));
        else if (o.stale) warn(a, lc, T.f("Last updated %s.", when(o.fetchAt)));
        if (o.failedSources > 0) warn(a, lc, T.f("%d source(s) didn't answer (see History).", o.failedSources));

        // ---- today's recommendation ----
        for (Latest l : o.latest) if (l.rec != null && l.rec.nums != null) recCard(a, body, l, single);

        if (s.games.get(0).digits()) prizes(a, body, s);
        notifications(a, body, s);
    }

    private static void warn(MainActivity a, LinearLayout c, String text) {
        TextView t = a.ui.note(text);
        t.setTextColor(Theme.AMBER);
        t.setPadding(0, a.ui.dp(8), 0, 0);
        c.addView(t);
    }

    private static void recCard(MainActivity a, LinearLayout body, Latest l, boolean single) {
        Ui ui = a.ui;
        Recommender.Rec r = l.rec;
        Game g = l.game;
        LinearLayout c = ui.card(body, single ? T.t("TODAY'S RECOMMENDATION") : T.f("RECOMMENDATION · %s", g.shortName().toUpperCase(T.L)));
        int t = r.target == null ? -1 : g.timeOn(r.target);
        if (r.target != null) c.addView(ui.note(T.f("For %s", Fmt.dayMonthInline(r.target) + (t >= 0 ? " · " + Fmt.time(t) : ""))));
        c.addView(ui.balls(g, r.nums, r.extra, single ? Ui.NUM_L : Ui.NUM_M), ui.matchWrap(10));
        TextView fig = ui.text(figures(g, r.nums, -1), 14, Theme.monoMedium(a), Theme.TEXT);
        fig.setPadding(0, ui.dp(10), 0, 0);
        c.addView(fig);
        c.addView(ui.kv(T.t("Probability"), Combi.oneIn(r.theoryP), Theme.AMBER_LIGHT,
                r.advantage ? T.t("The model beats chance in the tests (see Stats).") : T.t("Same as any other number: no model beats chance.")),
                ui.matchWrap(8));
        c.addView(ui.note(T.f("%s · %s", r.model.shortTitle(), when(l.generatedAt))), ui.matchWrap(2));
    }

    /** El Dorado: what each bet wins, net, for the amount chosen. */
    private static void prizes(MainActivity a, LinearLayout body, MainActivity.Sel s) {
        Ui ui = a.ui;
        LinearLayout c = ui.card(body, T.t("WHAT YOU WIN"));
        // the five amounts as equal cells across the card
        final double[] amounts = {200, 500, 1000, 2000, 5000};
        String[] labels = new String[amounts.length];
        int chosen = -1;
        for (int i = 0; i < amounts.length; i++) {
            labels[i] = Prizes.cop(amounts[i]);
            if (Math.abs(s.checkWager - amounts[i]) < 0.5) chosen = i;
        }
        ui.chips(c, labels, chosen, i -> {
            a.checkWager = amounts[i];
            a.rerenderInPlace();
        }, 0);
        int year = ZonedDateTime.now(MainActivity.BOGOTA).getYear();
        Prizes.Bet[] bets = {Prizes.Bet.FOUR, Prizes.Bet.THREE, Prizes.Bet.TWO, Prizes.Bet.ONE, Prizes.Bet.FOUR_ANY, Prizes.Bet.THREE_ANY};
        LinearLayout list = ui.vbox();
        for (Prizes.Bet b : bets) {
            list.addView(ui.kv(b.label(), Prizes.cop(Prizes.payout(b, s.checkWager, year).net), b == Prizes.Bet.FOUR ? Theme.GREEN : Theme.TEXT, null));
        }
        c.addView(list, ui.matchWrap(8));
        c.addView(ui.note(T.t("Net prize, after withholding where it applies.")), ui.matchWrap(2));
    }

    /**
     * Notifications: just a switch beside the section's title. It shows on only while results can really be
     * announced; turning it on when Android blocks LotoLab's notifications asks to allow them (or opens the
     * settings where that's done).
     */
    private static void notifications(MainActivity a, LinearLayout body, MainActivity.Sel s) {
        boolean wanted = Notifier.enabled(a, s.lottery);
        boolean allowed = Notifier.allowed(a);
        a.ui.switchRow(body, T.t("NOTIFICATIONS"), T.f("%s results", s.lottery), wanted && allowed, v -> {
            boolean on = !(wanted && allowed);
            Notifier.setEnabled(a, s.lottery, on);
            if (on && !allowed) {
                if (Build.VERSION.SDK_INT >= 33 && a.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    a.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 3);
                } else {
                    a.startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, a.getPackageName()));
                }
            }
            a.rerenderInPlace();
        });
    }
}
