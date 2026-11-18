package app.loto.analysis;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * The numbers behind the draw, in three cards: the chance of the top prize (from the rules alone), the hot and cold
 * digits or numbers (always beside what a fair draw would give), and today's model ranking.
 */
final class StatsTab {
    private StatsTab() {}

    static final class Out {
        Game game;
        double jackpot;
        Analysis.Freq freq;
        Recommender.Rec rec;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        o.game = s.games.get(0);
        o.jackpot = Odds.jackpot(o.game);
        Sample sm = s.sample;
        if (sm.size() == 0) return o;
        o.freq = Analysis.frequency(sm);
        if (sm.size() >= Recommender.WARMUP) {
            ZonedDateTime now = ZonedDateTime.now(MainActivity.BOGOTA);
            LocalDate target = SummaryTab.nextDraw(o.game, now);
            o.rec = Recommender.recommend(sm, target != null ? target : now.toLocalDate(), true);
        }
        return o;
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        odds(a, body, s, o);
        if (o.freq == null) {
            MainActivity.empty(a, body, s);
            return;
        }
        hotCold(a, body, s, o);
        if (o.rec != null && !o.rec.ranking.isEmpty()) ranking(a, body, o.rec);
    }

    // ------------------------------------------------------------------ the top prize

    private static void odds(MainActivity a, LinearLayout body, MainActivity.Sel s, Out o) {
        Ui ui = a.ui;
        Game g = o.game;
        double p = o.jackpot;
        LinearLayout c = ui.card(body, T.f("CHANCE OF THE TOP PRIZE · %s", s.lottery.toUpperCase(T.L)), T.t("theoretical"));
        c.addView(ui.text(Combi.oneIn(p), 28, Theme.monoBold(a), Theme.LED));
        c.addView(ui.note(g.digits() ? T.f("%s per bet · 4 cifras: the four digits in order", Combi.percent(p))
                : g.extraMax > 0 ? T.f("%s per ticket · %d numbers from 1–%d plus the superbalota (1–%d)", Combi.percent(p), g.count, g.max, g.extraMax)
                : T.f("%s per ticket · %d numbers from 1–%d", Combi.percent(p), g.count, g.max)));
    }

    // ------------------------------------------------------------------ hot & cold

    private static void hotCold(MainActivity a, LinearLayout body, MainActivity.Sel s, Out o) {
        Ui ui = a.ui;
        Game g = o.game;
        int D = s.sample.size();
        LinearLayout hot = ui.card(body, g.digits() ? T.t("HOT & COLD DIGITS") : T.t("HOT & COLD NUMBERS"), T.t("descriptive"));
        hot.addView(ui.note(MainActivity.scope(s)));
        if (g.digits()) {
            hot.addView(ui.note(T.f("How often each digit came up in each position. A fair draw gives every cell %s; warm cells came "
                    + "up more, cool ones less.", Fmt.num(D / 10.0))));
            hot.addView(new Charts.Heat(a, o.freq.positions), ui.matchWrap(8));
        } else {
            hot.addView(ui.note(T.f("Times each number was drawn; a fair draw gives each %s.", Fmt.num(o.freq.values.get(0).expected))));
        }
        LinearLayout cols = ui.hbox();
        cols.addView(cellList(a, T.t("Most frequent"), o.freq.hot(5)), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        rp.leftMargin = ui.dp(12);
        cols.addView(cellList(a, T.t("Least frequent"), o.freq.cold(5)), rp);
        hot.addView(cols, ui.matchWrap(12));
        hot.addView(verdict(a, o.freq.p, T.f("Overall chi-square p = %s: %s", Combi.pText(o.freq.p),
                o.freq.p >= 0.05 ? T.t("the spread is what fair draws produce.") : T.t("more spread than fair draws usually produce."))));
        LinearLayout hd = ui.details(hot, T.t("Longest absent and the most extreme value"));
        StringBuilder ab = new StringBuilder();
        for (Analysis.Cell c : o.freq.overdue(5)) {
            if (ab.length() > 0) ab.append("   ");
            ab.append(c.label).append(": ").append(c.gap);
        }
        hd.addView(ui.body(T.f("%s  draws  (a value waits %s draws on average)", ab, Fmt.num(o.freq.meanGap))));
        hd.addView(ui.note(T.f("The single most extreme value, allowing for the %d values compared: p = %s. Being absent for a long "
                + "time doesn't make a value more likely next.", g.digits() ? 40 : g.max, Combi.pText(o.freq.extremeP()))), ui.matchWrap(6));
    }

    private static LinearLayout cellList(MainActivity a, String title, List<Analysis.Cell> cells) {
        Ui ui = a.ui;
        LinearLayout l = ui.vbox();
        l.addView(ui.label(title, Theme.DIM));
        for (Analysis.Cell c : cells) {
            LinearLayout row = ui.hbox();
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, ui.dp(5), 0, 0);
            TextView name = ui.text(c.label, 14, Theme.monoBold(a), Ui.zColor(c.z));
            row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(ui.text(Fmt.num(c.observed) + "  " + Fmt.signed(c.z) + "σ", Ui.SMALL, Theme.monoMedium(a), Theme.MUTED));
            l.addView(row);
        }
        return l;
    }

    // ------------------------------------------------------------------ model ranking

    private static void ranking(MainActivity a, LinearLayout body, Recommender.Rec r) {
        Ui ui = a.ui;
        Game g = r.game;
        LinearLayout c = ui.card(body, T.t("MODEL RANKING"), T.t("predictive evidence"));
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
        c.addView(verdict(a, r.advantage ? 0.01 : 0.5, evidence));
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

    /** A verdict line coloured by its p-value: green within chance, amber below 0.05, coral below 0.001. */
    static LinearLayout verdict(MainActivity a, double p, String text) {
        int color = Double.isNaN(p) ? Theme.DIM : p < 0.001 ? Theme.CORAL : p < 0.05 ? Theme.AMBER : Theme.GREEN;
        LinearLayout v = a.ui.status(color, Double.isNaN(p) ? T.t("Not enough data") : p < 0.001 ? T.t("Very unlikely by chance")
                : p < 0.05 ? T.t("Unusual (p < 0.05)") : T.t("Within chance"), text);
        v.setPadding(0, a.ui.dp(10), 0, 0);
        return v;
    }
}
