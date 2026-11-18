package app.loto.analysis;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * The randomness score, the tests behind it, each draw series' own score, and the unusual draws of the period
 * against how many chance predicts. (Every single draw's audit is shown next to it in History.)
 */
final class RandomTab {
    private RandomTab() {}

    static final class Out {
        Randomness.Report report;
        final List<String[]> perGame = new ArrayList<>(); // {name, score, label, draws}
        final List<Randomness.DrawScore> unusual = new ArrayList<>();
        int below1, below10;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        if (s.sample.size() == 0) return o;
        o.report = Randomness.run(s.sample);
        List<Game> all = Game.of(s.lottery);
        for (Game g : all) {
            List<Game> one = new ArrayList<>();
            one.add(g);
            Sample sm = s.h.sample(one, s.days);
            Randomness.Report r = sm.size() > 0 ? Randomness.run(sm) : null;
            int score = r == null ? -1 : r.score;
            o.perGame.add(new String[]{g.name, String.valueOf(score), Randomness.Report.label(score), String.valueOf(sm.size())});
        }
        if (all.size() > 1) {
            Sample sm = s.h.sample(all, s.days);
            Randomness.Report r = Randomness.run(sm);
            o.perGame.add(new String[]{T.f("All %s draws together", s.lottery), String.valueOf(r.score),
                    Randomness.Report.label(r.score), String.valueOf(sm.size())});
        }
        List<Randomness.DrawScore> ds = new ArrayList<>(o.report.draws);
        for (Randomness.DrawScore d : ds) {
            if (d.score < 1) o.below1++;
            if (d.score < 10) o.below10++;
        }
        ds.sort((x, y) -> Double.compare(x.rarity, y.rarity));
        for (int i = 0; i < Math.min(6, ds.size()); i++) o.unusual.add(ds.get(i));
        return o;
    }

    static int scoreColor(int score) {
        if (score < 0) return Theme.DIM;
        if (score >= 90) return Theme.GREEN;
        if (score >= 70) return Theme.AMBER_LIGHT;
        if (score >= 40) return Theme.AMBER;
        return Theme.CORAL;
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        if (o.report == null) {
            MainActivity.empty(a, body, s);
            return;
        }
        Randomness.Report r = o.report;

        LinearLayout sc = ui.card(body, T.t("RANDOMNESS SCORE"));
        sc.addView(ui.note(MainActivity.scope(s)));
        sc.addView(new Charts.Gauge(a, r.score, scoreColor(r.score)), ui.matchWrap(6));
        TextView lab = ui.text(r.label(), 16, Theme.uiBold(a), scoreColor(r.score));
        lab.setGravity(Gravity.CENTER);
        sc.addView(lab, ui.matchWrap(0));
        TextView sum = ui.body(r.summary());
        sum.setPadding(0, ui.dp(10), 0, 0);
        sc.addView(sum);
        sc.addView(ui.note(T.t("How it's scored: each test counts fully unless its corrected p-value is below 0.05 (0.6), 0.01 "
                + "(0.3) or 0.001 (0). A fair lottery scores 90–100 most of the time; the score measures how well the "
                + "results fit a fair draw, not whether the lottery is honest.")), ui.matchWrap(8));

        LinearLayout pg = ui.card(body, T.f("SCORE BY DRAW · %s", s.lottery.toUpperCase(T.L)));
        for (String[] row : o.perGame) {
            int score = Integer.parseInt(row[1]);
            pg.addView(ui.kv(row[0], score < 0 ? "–" : T.f("%d / 100", score), scoreColor(score),
                    T.f("%s · %s draws", row[2], Fmt.num(Integer.parseInt(row[3])))));
        }

        LinearLayout tc = ui.card(body, T.t("THE TESTS"));
        boolean first = true;
        for (TestResult t : r.tests) {
            if (!first) tc.addView(ui.divider());
            first = false;
            LinearLayout head = ui.hbox();
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(ui.text(t.name, 14, Theme.uiSemi(a), Theme.TEXT), new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            head.addView(ui.tag(t.verdict(), Ui.pColor(t)));
            tc.addView(head);
            TextView q = ui.note(t.question);
            q.setPadding(0, ui.dp(3), 0, 0);
            tc.addView(q);
            if (!t.detail.isEmpty()) {
                TextView d = ui.body(t.detail);
                d.setTextSize(12);
                d.setPadding(0, ui.dp(4), 0, 0);
                tc.addView(d);
            }
            if (t.enough) {
                String p = T.f("p = %s · corrected %s", Combi.pText(t.p), Combi.pText(t.pAdj));
                if (t.df > 0) p += " · " + T.f("χ² %.1f, df %d", t.stat, t.df);
                TextView pv = ui.text(p, 11, Theme.monoMedium(a), Theme.MUTED);
                pv.setPadding(0, ui.dp(4), 0, 0);
                tc.addView(pv);
            }
        }

        LinearLayout sd = ui.card(body, T.t("UNUSUAL DRAWS"));
        int n = r.draws.size();
        sd.addView(ui.body(T.f("Unusual draws are expected: in %s fair draws about %s would score below 10 and %s below 1. "
                + "Here: %d and %d.", Fmt.num(n), Fmt.num(n * 0.1), Fmt.num(n * 0.01), o.below10, o.below1)));
        sd.addView(StatsTab.verdict(a, n >= 50 ? Combi.binomTwoSided(n, 0.1, o.below10) : Double.NaN,
                T.t("Are there more unusual draws than chance gives? Binomial test on the draws scoring below 10.")));
        sd.addView(StatsTab.sub(a, T.t("Most unusual in this period")));
        for (Randomness.DrawScore d : o.unusual) sd.addView(drawRow(a, d, s.games.size() > 1));

        LinearLayout ex2 = ui.card(body, T.t("PATTERN OR COINCIDENCE?"));
        ex2.addView(ui.body(T.t("A p-value is how often a fair draw would produce a result at least this far from expectation. "
                + "At p = 0.05 that's 1 time in 20, so running a dozen tests on a perfectly fair lottery flags something about "
                + "half the time. That's why the score uses corrected p-values (Holm–Bonferroni), and why a single low "
                + "p-value is a coincidence until it repeats.")));
        ex2.addView(ui.body(T.t("A pattern is worth taking seriously when it survives the correction, is large enough to matter, "
                + "and shows up again in new draws. The Integrity tab checks exactly that, split by period.")), ui.matchWrap(8));
    }

    private static LinearLayout drawRow(MainActivity a, Randomness.DrawScore d, boolean showGame) {
        Ui ui = a.ui;
        LinearLayout box = ui.vbox();
        box.setPadding(0, ui.dp(6), 0, ui.dp(6));
        LinearLayout row = ui.hbox();
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout who = ui.vbox();
        who.addView(ui.text(Fmt.date(d.draw.date), 12, Theme.uiSemi(a), Theme.TEXT));
        if (showGame) who.addView(ui.text(d.draw.game.shortName(), 10, Theme.monoMedium(a), Theme.DIM));
        row.addView(who, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(ui.balls(d.draw.game, d.draw.nums, -1, d.draw.game.digits() ? 24 : 26));
        TextView sc = ui.text(String.valueOf(d.score), 15, Theme.monoBold(a), SummaryTab.auditColor(d.score));
        sc.setGravity(Gravity.END);
        row.addView(sc, new LinearLayout.LayoutParams(ui.dp(40), ViewGroup.LayoutParams.WRAP_CONTENT));
        box.addView(row);
        TextView why = ui.note(d.label() + ". " + String.join(" ", d.reasons));
        why.setPadding(0, ui.dp(2), 0, 0);
        box.addView(why);
        return box;
    }
}
