package app.loto.analysis;

import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The recommendation engine in depth: how the models rank today and why, the track record of the daily
 * recommendation and of each model (replayed over the whole history, against random selection), the log of
 * recommendations the app has shown, and a model explorer with alternatives and number-by-number reasons.
 */
final class PicksTab {
    private PicksTab() {}

    static final class GameOut {
        Game game;
        Recommender.Rec rec;
        /** The daily recommendation's record per period (30 days, 90 days, a year, all). */
        final Recommender.Stats[] periods = new Recommender.Stats[MainActivity.PERIODS.length];
        /** Each model's record over the chosen period, in ranking order. */
        final List<Object[]> perModel = new ArrayList<>(); // {Model, Stats}
        /** Logged recommendations: {generated, target, model, numbers, outcome or null, "1" if it hit something}. */
        final List<String[]> log = new ArrayList<>();
        int logHits, logPartial, logSettled;
    }

    static final class Out {
        final List<GameOut> games = new ArrayList<>();
        Predictor.Result result;
        Predictor.Backtest backtest;
        double theory;
        double freqP = Double.NaN;
        int days;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        o.days = s.days;
        o.theory = Predictor.theory(s.games.get(0));
        LocalDate today = ZonedDateTime.now(MainActivity.BOGOTA).toLocalDate();
        LocalDate from = s.days > 0 ? today.minusDays(s.days) : null;
        List<String[]> log = s.ctx == null ? new ArrayList<>() : Data.recLog(s.ctx);
        for (Game g : s.games) {
            List<Game> one = new ArrayList<>();
            one.add(g);
            Sample full = s.h.sample(one, 0);
            if (full.size() < Recommender.WARMUP) continue;
            GameOut go = new GameOut();
            go.game = g;
            LocalDate target = SummaryTab.nextDraw(g, ZonedDateTime.now(MainActivity.BOGOTA));
            go.rec = Recommender.recommend(full, target != null ? target : today, true);
            Recommender.Track tr = Recommender.track(full);
            for (int i = 0; i < MainActivity.PERIODS.length; i++) {
                int days = MainActivity.PERIODS[i];
                go.periods[i] = Recommender.stats(g, tr.best, days > 0 ? today.minusDays(days) : null);
            }
            for (Recommender.Rank k : go.rec.ranking) {
                go.perModel.add(new Object[]{k.model, Recommender.stats(g, tr.perModel.get(k.model), from)});
            }
            // the log, newest first, with the result when the draw has happened; for each draw the last
            // recommendation shown before it (new results can change it during the day)
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (int i = log.size() - 1; i >= 0 && go.log.size() < 12; i--) {
                String[] row = log.get(i);
                if (!row[1].equals(g.id) || !seen.add(row[2])) continue;
                LocalDate d;
                try {
                    d = LocalDate.parse(row[2]);
                } catch (Exception e) {
                    continue;
                }
                Draw actual = s.h.on(g, d);
                String outcome = null, hit = "0";
                if (actual != null) {
                    int[][] parsed = parse(g, row[4]);
                    if (parsed != null) {
                        Recommender.Outcome oc = Recommender.outcome(g, actual, Predictor.Model.FREQUENCY, parsed[0], parsed[1][0]);
                        outcome = outcomeText(g, oc);
                        if (oc.exact || oc.last3 || oc.last2 || oc.last1 || oc.extraHit || (!g.digits() && oc.matched >= 2)) hit = "1";
                        go.logSettled++;
                        if (oc.exact) go.logHits++;
                        else if (g.digits() ? oc.last2 : oc.matched >= 2) go.logPartial++;
                    }
                }
                go.log.add(new String[]{row[0], row[2], row[3], row[4], outcome, hit});
            }
            o.games.add(go);
        }
        if (s.sample.size() == 0) return o;
        o.result = Predictor.suggest(s.model, s.sample, s.seed);
        boolean ranked = false;
        for (Predictor.Model m : Models.RANKED) ranked |= m == s.model;
        if (!ranked) o.backtest = Predictor.backtest(s.model, s.sample, s.seed);
        o.freqP = Analysis.frequency(s.sample).p;
        return o;
    }

    /** Numbers as logged ("1234" or "03 11 20 31 40+7") back to {nums, {extra}}. */
    static int[][] parse(Game g, String text) {
        try {
            int extra = -1;
            String main = text;
            int plus = text.indexOf('+');
            if (plus >= 0) {
                extra = Integer.parseInt(text.substring(plus + 1).trim());
                main = text.substring(0, plus);
            }
            int[] nums;
            if (g.digits()) {
                String d = main.replaceAll("[^0-9]", "");
                if (d.length() != g.count) return null;
                nums = new int[g.count];
                for (int i = 0; i < nums.length; i++) nums[i] = d.charAt(i) - '0';
            } else {
                String[] p = main.trim().split("[^0-9]+");
                if (p.length != g.count) return null;
                nums = new int[g.count];
                for (int i = 0; i < nums.length; i++) nums[i] = Integer.parseInt(p[i]);
            }
            return new int[][]{nums, {extra}};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String outcomeText(Game g, Recommender.Outcome o) {
        if (g.digits()) {
            if (o.exact) return T.t("All four digits");
            if (o.last3) return T.t("Last three digits");
            if (o.last2) return T.t("La pata (last two)");
            if (o.last1) return T.t("La uña (last digit)");
            return T.f("No hit (%s)", o.actual.numbersText());
        }
        String s = T.f("%d of %d", o.matched, g.count);
        if (o.extraHit) s += " + " + T.t("superbalota");
        return s + " (" + o.actual.numbersText() + (o.actual.extra > 0 ? " + " + o.actual.extraText() : "") + ")";
    }

    // ------------------------------------------------------------------ display

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        Game g0 = s.games.get(0);

        for (GameOut go : o.games) ranking(a, body, s, go, o.games.size() > 1);
        for (GameOut go : o.games) record(a, body, s, go, o);
        for (GameOut go : o.games) log(a, body, go);

        // ---- model explorer ----
        LinearLayout ex = ui.card(body, T.t("EXPLORE A MODEL"), T.t("model ranking"));
        ex.addView(ui.note(T.t("Every model's own top pick, its reasons and alternatives.")));
        LinearLayout mr = ui.chipRow(ex, 10);
        for (Predictor.Model m : Predictor.Model.values()) {
            mr.addView(ui.chip(m.title(), m == s.model, v -> {
                a.model = m;
                a.rerenderInPlace();
            }), ui.wrap(mr.getChildCount() == 0 ? 0 : 8));
        }
        ui.reveal(mr, s.model.ordinal());
        if (o.result == null) {
            MainActivity.empty(a, body, s);
            return;
        }
        Predictor.Result r = o.result;
        TextView mt = ui.text(s.model.title(), 16, Theme.uiBold(a), Theme.TEXT);
        mt.setPadding(0, ui.dp(12), 0, ui.dp(2));
        ex.addView(mt);
        ex.addView(ui.note(s.model.description()));
        ex.addView(ui.note(MainActivity.scope(s)), ui.matchWrap(4));
        if (r.top != null) {
            TextView h = ui.label(r.hasTop ? T.t("Highest estimated probability under this model") : T.t("Suggested combination"), Theme.AMBER_LIGHT);
            h.setPadding(0, ui.dp(12), 0, ui.dp(8));
            ex.addView(h);
            ex.addView(ui.balls(g0, r.top.nums, r.top.extra, Ui.NUM_M));
            ex.addView(odds(a, r.top), ui.matchWrap(10));
            LinearLayout why = ui.details(ex, T.t("Why these numbers"));
            for (String line : r.top.reasons) {
                TextView t = ui.note("·  " + line);
                t.setPadding(0, ui.dp(3), 0, 0);
                why.addView(t);
            }
        }
        boolean freqModel = s.model == Predictor.Model.FREQUENCY || s.model == Predictor.Model.RECENT || s.model == Predictor.Model.BAYES;
        ex.addView(StatsTab.verdict(a, freqModel ? o.freqP : Double.NaN, r.verdict));
        if (!r.alternatives.isEmpty()) {
            LinearLayout alt = ui.details(ex, T.f("%d more from this model", r.alternatives.size()));
            boolean first = true;
            for (Predictor.Suggestion sg : r.alternatives) {
                if (!first) alt.addView(ui.divider());
                first = false;
                alt.addView(ui.balls(g0, sg.nums, sg.extra, Ui.NUM_S), ui.matchWrap(6));
                alt.addView(odds(a, sg), ui.matchWrap(6));
            }
            alt.addView(ui.secondaryButton(T.t("New suggestions"), v -> {
                a.seed++;
                a.rerenderInPlace();
            }), ui.matchWrap(12));
        }
        if (o.backtest != null) {
            LinearLayout bt = ui.details(ex, T.t("Backtest"));
            bt.addView(StatsTab.verdict(a, o.backtest.draws == 0 ? Double.NaN : o.backtest.p < 0.05 ? 0.01 : 0.5, o.backtest.summary));
        }

        LinearLayout th = ui.card(body, T.t("THEORY VS. HISTORY"), T.t("theoretical"));
        th.addView(ui.body(T.f("Every combination has a theoretical probability of %s in every draw, fixed by the rules. The models "
                + "only reweight numbers by past results: in a fair draw no model beats the theoretical odds, and a recommendation "
                + "that changes from day to day doesn't make any number more likely. Every ticket's expected return is negative.",
                Combi.oneIn(o.theory))));
    }

    private static void ranking(MainActivity a, LinearLayout body, MainActivity.Sel s, GameOut go, boolean many) {
        Ui ui = a.ui;
        Recommender.Rec r = go.rec;
        Game g = go.game;
        LinearLayout c = ui.card(body, many ? T.f("MODEL RANKING · %s", g.shortName().toUpperCase(T.L)) : T.t("MODEL RANKING"),
                T.t("predictive evidence"));
        Recommender.Rank best = r.ranking.get(0);
        c.addView(ui.note(T.f("Each model forecasts every draw from the earlier ones only. Score: how much more probability its "
                + "forecasts gave the actual results than a fair guess, over the last %d draws (above 0 = better).", best.n)));
        float[] w = {2.2f, 1.4f, 2.4f};
        c.addView(ui.row(new String[]{T.t("Model"), T.t("Score"), T.t("CI (95%)")}, w, true, null), ui.matchWrap(10));
        int i = 0;
        for (Recommender.Rank k : r.ranking) {
            i++;
            boolean top = i == 1;
            int col = !Double.isNaN(k.lo) && k.lo > 0 ? Theme.GREEN : !Double.isNaN(k.hi) && k.hi < 0 ? Theme.MUTED : Theme.TEXT;
            c.addView(ui.row(new String[]{(top ? "★ " : i + ". ") + k.model.shortTitle(), SummaryTab.score(k.mean),
                    SummaryTab.score(k.lo) + " … " + SummaryTab.score(k.hi)}, w, false,
                    new int[]{top ? Theme.AMBER_LIGHT : 0, col, 0}));
        }
        String evidence = Double.isNaN(r.mcP) ? T.t("Too few draws to test for an advantage.")
                : r.advantage ? T.f("The top model beats chance (Monte Carlo p = %s, allowing for picking the best of %d).", Combi.pText(r.mcP), r.ranking.size())
                : T.f("No real advantage: in simulated fair draws the best of %d models scores this well %s of the time (p = %s). "
                + "The ranking only decides which candidate is shown.", r.ranking.size(), Combi.percent(r.mcP), Combi.pText(r.mcP));
        c.addView(StatsTab.verdict(a, r.advantage ? 0.01 : 0.5, evidence));
        LinearLayout cand = ui.details(c, T.t("Each model's candidate today"));
        for (Recommender.Rank k : r.ranking) {
            TextView name = ui.text(k.model.title(), 13, Theme.uiSemi(a), k == best ? Theme.AMBER_LIGHT : Theme.TEXT_SOFT);
            name.setPadding(0, ui.dp(10), 0, ui.dp(6));
            cand.addView(name);
            cand.addView(ui.balls(g, k.nums, k.extra, Ui.NUM_S));
            cand.addView(ui.note(T.f("Model's estimate %s · theoretical %s", Combi.oneIn(k.modelP), Combi.oneIn(r.theoryP))), ui.matchWrap(4));
        }
        LinearLayout how = ui.details(c, T.t("How the ranking works"));
        how.addView(ui.note(T.t("The log score is a proper scoring rule: a model can only score above 0 on average by putting more "
                + "probability on what actually comes out. The Ensemble mixes the four forecasters by Bayesian model averaging. "
                + "The Monte Carlo test runs the same models on 99 simulated fair histories of the same length, so picking the best "
                + "of several doesn't pass for skill. Ties are broken with a seed made from the lottery and the draw date, so the "
                + "same data always gives the same recommendation.")));
    }

    private static void record(MainActivity a, LinearLayout body, MainActivity.Sel s, GameOut go, Out o) {
        Ui ui = a.ui;
        Game g = go.game;
        LinearLayout c = ui.card(body, o.games.size() > 1 ? T.f("TRACK RECORD · %s", g.shortName().toUpperCase(T.L)) : T.t("TRACK RECORD"),
                T.t("predictive evidence"));
        c.addView(ui.note(T.t("The daily recommendation replayed: before every past draw, what it would have chosen from the earlier "
                + "results only, against what came out. Expected = what random picks average.")));
        float[] w = {1.6f, 1.2f, 1.4f, 1.4f, 1.2f};
        String hitName = g.digits() ? T.t("Pata") : T.t("2+ hit");
        c.addView(ui.row(new String[]{T.t("Period"), T.t("Draws"), hitName, T.t("Expected"), "p"}, w, true, null), ui.matchWrap(10));
        for (int i = 0; i < go.periods.length; i++) {
            Recommender.Stats st = go.periods[i];
            int hits = g.digits() ? st.last2 : st.two;
            double exp = g.digits() ? st.expLast2 : st.expTwo;
            double p = g.digits() ? st.pLast2 : st.pTwo;
            c.addView(ui.row(new String[]{MainActivity.periodName(i), Fmt.num(st.n), String.valueOf(hits), Fmt.num(exp),
                    st.n == 0 ? "–" : Combi.pText(p)}, w, false, new int[]{0, 0, 0, 0, st.n > 0 && p < 0.05 ? Theme.AMBER : 0}));
        }
        Recommender.Stats all = go.periods[go.periods.length - 1];
        String verdict;
        double vp;
        if (g.digits()) {
            double[] ci = all.ciLast2;
            verdict = T.f("All four digits %d times (%s expected), last three %d (%s), la pata %d (%s), la uña %d (%s). Pata hit rate "
                            + "%s, 95%% CI %s–%s; random: 1%%.", all.exact, Fmt.num(all.expExact), all.last3, Fmt.num(all.expLast3), all.last2,
                    Fmt.num(all.expLast2), all.last1, Fmt.num(all.expLast1), Combi.percent(all.n == 0 ? 0 : all.last2 / (double) all.n),
                    Combi.percent(ci[0]), Combi.percent(ci[1]));
            vp = all.pLast2;
        } else {
            double[] ci = all.ciTwo;
            verdict = T.f("%s numbers matched in all (%s expected); 2 or more in %d draws (%s expected), 3 or more in %d. "
                            + "2+ rate %s, 95%% CI %s–%s; random: %s.", Fmt.num(all.matched), Fmt.num(all.expMatched), all.two,
                    Fmt.num(all.expTwo), all.three, Combi.percent(all.n == 0 ? 0 : all.two / (double) all.n), Combi.percent(ci[0]),
                    Combi.percent(ci[1]), Combi.percent(all.n == 0 ? 0 : all.expTwo / all.n));
            if (g.extraMax > 0 && all.extraN > 0) verdict += " " + T.f("Superbalota right %d of %d (%s expected).", all.extraHits, all.extraN,
                    Fmt.num(all.extraN / (double) g.extraMax));
            vp = all.pMatched;
        }
        c.addView(ui.body(verdict), ui.matchWrap(8));
        c.addView(StatsTab.verdict(a, vp < 0.01 ? 0.01 : 0.5, vp < 0.01
                ? T.t("More hits than chance over the whole history. With several periods and models compared, confirm it holds in new draws.")
                : T.t("No better than random selection: the hits are what chance alone gives.")));

        LinearLayout bm = ui.details(c, T.f("By model · %s", o.days > 0 ? T.f("last %d days", o.days) : T.t("all history")));
        float[] w2 = {2.4f, 1.1f, 1.2f, 1.3f, 1.1f};
        bm.addView(ui.row(new String[]{T.t("Model"), T.t("Draws"), hitName, T.t("Expected"), "p"}, w2, true, null), ui.matchWrap(4));
        for (Object[] row : go.perModel) {
            Predictor.Model m = (Predictor.Model) row[0];
            Recommender.Stats st = (Recommender.Stats) row[1];
            int hits = g.digits() ? st.last2 : st.two;
            double exp = g.digits() ? st.expLast2 : st.expTwo;
            double p = g.digits() ? st.pLast2 : st.pTwo;
            bm.addView(ui.row(new String[]{m.shortTitle(), Fmt.num(st.n), String.valueOf(hits), Fmt.num(exp), st.n == 0 ? "–" : Combi.pText(p)},
                    w2, false, new int[]{0, 0, 0, 0, st.n > 0 && p < 0.05 ? Theme.AMBER : 0}));
        }
        bm.addView(ui.note(T.t("With 5 models and 4 periods, a p below 0.05 somewhere is expected by chance about once in a while; "
                + "only a result that keeps holding in new draws would point to a real edge.")), ui.matchWrap(6));
    }

    private static void log(MainActivity a, LinearLayout body, GameOut go) {
        Ui ui = a.ui;
        Game g = go.game;
        LinearLayout c = ui.card(body, T.f("RECOMMENDATIONS SHOWN · %s", g.shortName().toUpperCase(T.L)));
        if (go.log.isEmpty()) {
            c.addView(ui.note(T.t("The log starts with today's recommendation: every one the app shows is kept here with its result.")));
            return;
        }
        if (go.logSettled > 0) {
            c.addView(ui.body(g.digits()
                    ? T.f("%d settled: %d with all four digits, %d more with at least la pata.", go.logSettled, go.logHits, go.logPartial)
                    : T.f("%d settled: %d jackpots, %d more with 2 or more numbers.", go.logSettled, go.logHits, go.logPartial)));
        }
        boolean first = true;
        for (String[] row : go.log) {
            if (!first) c.addView(ui.divider());
            first = false;
            int[][] parsed = parse(g, row[3]);
            LocalDate d = LocalDate.parse(row[1]);
            Predictor.Model m;
            try {
                m = Predictor.Model.valueOf(row[2]);
            } catch (IllegalArgumentException e) {
                m = null;
            }
            long at;
            try {
                at = Long.parseLong(row[0]);
            } catch (NumberFormatException e) {
                at = 0;
            }
            TextView h = ui.text(T.f("Draw of %s", Fmt.dayMonthInline(d)), 13, Theme.uiSemi(a), Theme.TEXT);
            h.setPadding(0, ui.dp(8), 0, ui.dp(6));
            c.addView(h);
            if (parsed != null) c.addView(ui.balls(g, parsed[0], parsed[1][0], Ui.NUM_S));
            String meta = (m == null ? row[2] : m.title()) + (at > 0 ? " · " + T.f("generated %s", SummaryTab.when(at)) : "");
            c.addView(ui.note(meta), ui.matchWrap(4));
            TextView res = ui.note(row[4] == null ? T.t("Waiting for the result") : T.f("Result: %s", row[4]));
            if ("1".equals(row[5])) res.setTextColor(Theme.GREEN);
            c.addView(res);
        }
    }

    private static LinearLayout odds(MainActivity a, Predictor.Suggestion sg) {
        Ui ui = a.ui;
        LinearLayout l = ui.vbox();
        double ratio = sg.theoryP > 0 ? sg.modelP / sg.theoryP : 1;
        l.addView(ui.kv(T.t("Theoretical probability"), Combi.oneIn(sg.theoryP), Theme.TEXT, null));
        l.addView(ui.kv(T.t("Model's estimate"), Combi.oneIn(sg.modelP), Math.abs(ratio - 1) < 0.005 ? Theme.TEXT : Theme.AMBER_LIGHT,
                Math.abs(ratio - 1) < 0.005 ? T.t("Same as theory: this model treats every combination alike.")
                        : T.f("%.2f× the theoretical, if the model's assumption were true.", ratio)));
        return l;
    }
}
