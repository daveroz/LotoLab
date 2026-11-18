package app.loto.analysis;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * What matters at a glance, for the chosen lottery and draw: the latest result with its randomness audit, when
 * it plays next, the suggested pick, a checker that sets any number beside that pick, and the notification switch.
 * The other tabs are for going deeper.
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
        /** Today's draw has been held but its result isn't in yet. */
        boolean awaiting;
        int awaitingTime;
    }

    static final class Out {
        final List<Latest> latest = new ArrayList<>();
        Predictor.Result pick;
        Predictor.Backtest backtest;
        Odds.Check mine, theirs;
        double mineModel, theirsModel;
        int draws;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        ZonedDateTime now = ZonedDateTime.now(MainActivity.BOGOTA);
        for (Game g : s.games) {
            Latest l = new Latest();
            l.game = g;
            List<Draw> all = s.h.all(g);
            if (!all.isEmpty()) {
                l.draw = all.get(all.size() - 1);
                List<Draw> pair = new ArrayList<>(all.subList(Math.max(0, all.size() - 2), all.size()));
                List<Game> one = new ArrayList<>();
                one.add(g);
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
            o.latest.add(l);
        }
        Sample sm = s.sample;
        o.draws = sm.size();
        if (sm.size() == 0) return o;
        o.pick = Predictor.suggest(s.model, sm, 1);
        o.backtest = Predictor.backtest(s.model, sm, 1);
        if (s.check != null && !s.check.trim().isEmpty() && o.pick.top != null) {
            Game g = sm.game;
            o.mine = Odds.read(g, s.check);
            if (o.mine.error == null) {
                Odds.measure(g, sm, o.mine);
                o.theirs = new Odds.Check();
                o.theirs.nums = o.pick.top.nums;
                o.theirs.extra = o.mine.extra >= 0 ? o.pick.top.extra : -1; // compare like with like
                Odds.measure(g, sm, o.theirs);
                Predictor.Weights wt = Predictor.weights(s.model, sm);
                o.mineModel = Predictor.probability(g, wt, o.mine.nums, o.mine.extra);
                o.theirsModel = Predictor.probability(g, wt, o.theirs.nums, o.theirs.extra);
                if (s.model == Predictor.Model.BALANCED || s.model == Predictor.Model.UNPOPULAR || s.model == Predictor.Model.RANDOM) {
                    o.mineModel = o.mine.theory;
                    o.theirsModel = o.theirs.theory;
                }
            }
        }
        return o;
    }

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

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        boolean single = o.latest.size() == 1;

        // ---- latest result, its audit, the schedule and the next draw ----
        LinearLayout lc = ui.card(body, single ? T.t("LATEST RESULT") : T.t("LATEST RESULTS"));
        boolean first = true;
        for (Latest l : o.latest) {
            if (!first) lc.addView(ui.divider());
            first = false;
            LinearLayout head = ui.hbox();
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(ui.text(l.game.name, single ? 17 : 15, Theme.uiBold(a), Theme.TEXT),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            head.addView(ui.tag(l.game.slot.label(), l.game.slot == Game.Slot.MORNING ? Theme.AMBER_LIGHT
                    : l.game.slot == Game.Slot.AFTERNOON ? Theme.AMBER : Theme.COOL));
            lc.addView(head);
            if (l.draw == null) {
                lc.addView(ui.body(T.t("No results stored yet.")));
            } else {
                int time = l.game.usualTime(l.draw.date);
                String when = Fmt.dayHeading(l.draw.date) + (time >= 0 ? " · " + Fmt.time(time) : "")
                        + (l.draw.drawNo.isEmpty() ? "" : " · " + T.f("draw #%s", l.draw.drawNo));
                TextView w = ui.note(when);
                w.setPadding(0, ui.dp(2), 0, ui.dp(10));
                lc.addView(w);
                lc.addView(ui.balls(l.game, l.draw.nums, l.draw.extra, single ? 42 : 32));
                if (l.audit != null) {
                    LinearLayout ar = ui.hbox();
                    ar.setGravity(Gravity.CENTER_VERTICAL);
                    ar.addView(ui.tag(T.f("%s · %d/100", l.audit.label(), l.audit.score), auditColor(l.audit.score)));
                    TextView why = ui.note(String.join(" ", l.audit.reasons));
                    why.setPadding(ui.dp(10), 0, 0, 0);
                    ar.addView(why, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    lc.addView(ar, ui.matchWrap(10));
                }
            }
            String next;
            if (l.awaiting) next = T.f("Today's draw was at %s: the result should be out soon.", Fmt.time(l.awaitingTime));
            else if (l.nextDate != null) next = T.f("Next draw: %s · %s (%s)", Fmt.dayMonth(l.nextDate), Fmt.time(l.nextTime), inTime(l.minutesToNext));
            else next = T.t("No draw scheduled in the next week.");
            TextView nx = ui.body(next);
            nx.setTextColor(l.awaiting ? Theme.AMBER : Theme.TEXT_SOFT);
            nx.setPadding(0, ui.dp(10), 0, 0);
            lc.addView(nx);
            TextView sch = ui.note(T.f("Plays %s", l.game.scheduleText()));
            sch.setPadding(0, ui.dp(2), 0, 0);
            lc.addView(sch);
        }

        if (o.pick == null || o.pick.top == null) {
            MainActivity.empty(a, body, s);
            notifications(a, body, s);
            return;
        }
        Game g = s.sample.game;

        // ---- the suggested pick ----
        LinearLayout pc = ui.card(body, T.t("SUGGESTED PICK"));
        pc.addView(ui.note(T.f("Model: %s · from %s draws of %s", s.model.title(), Fmt.num(o.draws), MainActivity.label(s))));
        pc.addView(ui.balls(g, o.pick.top.nums, o.pick.top.extra, 40), ui.matchWrap(12));
        double ratio = o.pick.top.modelP / o.pick.top.theoryP;
        pc.addView(ui.kv(T.t("Theoretical probability"), Combi.oneIn(o.pick.top.theoryP), Theme.TEXT, null), ui.matchWrap(10));
        pc.addView(ui.kv(T.t("Model's estimate"), Combi.oneIn(o.pick.top.modelP), Math.abs(ratio - 1) < 0.005 ? Theme.TEXT : Theme.AMBER_LIGHT,
                Math.abs(ratio - 1) < 0.005 ? T.t("Same as theory: this model treats every combination alike.")
                        : T.f("%.2f× the theoretical, if the model's assumption were true.", ratio)));
        int shown = 0;
        for (String line : o.pick.top.reasons) {
            if (shown++ >= 2 && !line.equals(o.pick.top.reasons.get(o.pick.top.reasons.size() - 1))) continue;
            TextView t = ui.note("·  " + line);
            t.setPadding(0, ui.dp(3), 0, 0);
            pc.addView(t);
        }
        TextView verdict = ui.note(o.pick.verdict);
        verdict.setPadding(0, ui.dp(8), 0, 0);
        pc.addView(verdict);
        if (o.backtest != null && o.backtest.draws > 0) {
            pc.addView(StatsTab.verdict(a, o.backtest.p < 0.05 ? 0.01 : 0.5, o.backtest.summary));
        }
        pc.addView(ui.note(T.t("In a fair draw every combination has the same chance; the model only reflects past "
                + "frequencies. Other models, alternatives and the reasoning are in Picks.")), ui.matchWrap(8));
        pc.addView(ui.secondaryButton(T.t("Open Picks"), v -> {
            a.tab = MainActivity.PICKS;
            a.buildSelectors();
            a.render();
        }), ui.matchWrap(10));

        // ---- check a number against the pick ----
        LinearLayout ck = ui.card(body, g.digits() ? T.t("CHECK A NUMBER") : T.t("CHECK A COMBINATION"));
        ck.addView(ui.note(g.digits() ? T.t("Type a 4-digit number to see its odds and how it compares with the suggested pick.")
                : g.extraMax > 0 ? T.t("Type 5 numbers (and optionally + the superbalota) to compare them with the suggested pick, e.g. 5 13 19 25 29 + 7.")
                : T.t("Type 5 numbers to compare them with the suggested pick, e.g. 5 13 19 25 29.")));
        LinearLayout row = ui.hbox();
        final EditText in = ui.input(g.digits() ? "2820" : (g.extraMax > 0 ? "5 13 19 25 29 + 7" : "5 13 19 25 29"), g.digits());
        if (s.check != null) in.setText(s.check);
        ui.addWeighted(row, in, 0);
        row.addView(ui.primaryButton(T.t("Check"), v -> {
            a.check = in.getText().toString();
            a.rerenderInPlace();
        }), ui.wrap(8));
        ck.addView(row, ui.matchWrap(10));
        if (o.mine != null && o.mine.error != null) {
            TextView e = ui.body(o.mine.error);
            e.setTextColor(Theme.AMBER);
            ck.addView(e, ui.matchWrap(10));
        } else if (o.mine != null) {
            compare(a, ck, g, o);
        }

        notifications(a, body, s);
    }

    /** Your number beside the suggested pick, figure by figure. */
    private static void compare(MainActivity a, LinearLayout ck, Game g, Out o) {
        Ui ui = a.ui;
        LinearLayout heads = ui.hbox();
        heads.addView(ui.label("", Theme.DIM), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.25f));
        heads.addView(colHead(a, T.t("Yours")), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        heads.addView(colHead(a, T.t("Suggested")), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        ck.addView(heads, ui.matchWrap(14));
        LinearLayout nums = ui.hbox();
        nums.setGravity(Gravity.CENTER_VERTICAL);
        nums.addView(ui.label("", Theme.DIM), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.25f));
        nums.addView(ui.text(numbers(g, o.mine), 13, Theme.monoBold(a), Theme.LED), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        nums.addView(ui.text(numbers(g, o.theirs), 13, Theme.monoBold(a), Theme.AMBER_LIGHT), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        ck.addView(nums, ui.matchWrap(4));
        ck.addView(ui.divider());
        ck.addView(row3(a, T.t("Theoretical probability"), Combi.oneIn(o.mine.theory), Combi.oneIn(o.theirs.theory)));
        ck.addView(row3(a, T.t("Model's estimate"), Combi.oneIn(o.mineModel), Combi.oneIn(o.theirsModel)));
        ck.addView(row3(a, T.f("Times drawn (in %s)", Fmt.num(o.mine.draws)), Fmt.num(o.mine.exact), Fmt.num(o.theirs.exact)));
        if (g.digits()) {
            ck.addView(row3(a, T.t("Last three digits seen"), Fmt.num(o.mine.lastThree), Fmt.num(o.theirs.lastThree)));
            ck.addView(row3(a, T.t("Last two digits seen"), Fmt.num(o.mine.lastTwo), Fmt.num(o.theirs.lastTwo)));
            ck.addView(row3(a, T.t("Same digits, any order"), Fmt.num(o.mine.anyOrder), Fmt.num(o.theirs.anyOrder)));
        } else {
            ck.addView(row3(a, T.t("Draws matching 3+ of them"), Fmt.num(o.mine.matched[3] + o.mine.matched[4] + o.mine.matched[5]),
                    Fmt.num(o.theirs.matched[3] + o.theirs.matched[4] + o.theirs.matched[5])));
            ck.addView(row3(a, T.t("Draws matching 4+ of them"), Fmt.num(o.mine.matched[4] + o.mine.matched[5]),
                    Fmt.num(o.theirs.matched[4] + o.theirs.matched[5])));
        }
        ck.addView(row3(a, T.t("Fair draws with this shape"), Combi.percent(o.mine.shapeShare), Combi.percent(o.theirs.shapeShare)));
        double r = o.mineModel > 0 ? o.theirsModel / o.mineModel : 1;
        String verdict = T.f("Both have exactly the same chance in a fair draw: %s.", Combi.oneIn(o.mine.theory));
        if (Math.abs(r - 1) >= 0.005) {
            verdict += " " + T.f("The model rates the suggested pick %.2f× yours, from past frequencies only; that isn't a real "
                    + "difference in odds unless the draw is biased.", r);
        }
        TextView v = ui.body(verdict);
        v.setPadding(0, ui.dp(12), 0, 0);
        ck.addView(v);
    }

    private static String numbers(Game g, Odds.Check c) {
        Draw d = new Draw(g, null, "", c.nums, c.extra);
        return d.numbersText() + (c.extra > 0 && !g.digits() ? " + " + d.extraText() : "");
    }

    private static TextView colHead(MainActivity a, String s) {
        return a.ui.label(s, Theme.DIM);
    }

    private static LinearLayout row3(MainActivity a, String label, String mine, String theirs) {
        Ui ui = a.ui;
        LinearLayout r = ui.hbox();
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, ui.dp(5), 0, ui.dp(5));
        TextView l = ui.text(label, 12, Theme.uiMedium(a), Theme.TEXT_SOFT);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.25f));
        r.addView(ui.text(mine, 12, Theme.monoSemi(a), Theme.TEXT), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        r.addView(ui.text(theirs, 12, Theme.monoSemi(a), Theme.TEXT), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return r;
    }

    /** The switch for this lottery's notifications, and a way to allow them if the system blocks them. */
    private static void notifications(MainActivity a, LinearLayout body, MainActivity.Sel s) {
        Ui ui = a.ui;
        LinearLayout nc = ui.card(body, T.t("NOTIFICATIONS"));
        boolean on = Notifier.enabled(a, s.lottery);
        boolean allowed = Notifier.allowed(a);
        nc.addView(ui.body(T.f("A notification as soon as each %s result is announced, with its numbers and whether it was an "
                + "ordinary or unusual draw.", s.lottery)));
        LinearLayout row = ui.hbox();
        row.addView(ui.chip(T.t("On"), on, v -> {
            Notifier.setEnabled(a, s.lottery, true);
            a.rerenderInPlace();
        }));
        row.addView(ui.chip(T.t("Off"), !on, v -> {
            Notifier.setEnabled(a, s.lottery, false);
            a.rerenderInPlace();
        }), ui.wrap(8));
        nc.addView(row, ui.matchWrap(10));
        if (on && !allowed) {
            TextView w = ui.body(T.t("Android is blocking notifications for LotoLab."));
            w.setTextColor(Theme.AMBER);
            nc.addView(w, ui.matchWrap(10));
            nc.addView(ui.secondaryButton(T.t("Allow notifications"), v -> {
                if (Build.VERSION.SDK_INT >= 33 && a.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    a.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 3);
                } else {
                    a.startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, a.getPackageName()));
                }
            }), ui.matchWrap(8));
        }
    }
}
