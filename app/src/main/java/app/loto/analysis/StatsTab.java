package app.loto.analysis;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Hot and cold numbers, repeats (inside a draw, from draw to draw, between draws on the same day, identical
 * results), even/odd, recurring combinations, sums and weekdays: always with what a fair draw would give.
 */
final class StatsTab {
    private StatsTab() {}

    static final class Out {
        Analysis.Freq freq;
        Analysis.Dist evenOdd, sums, patterns, previous;
        Analysis.Duplicates dups;
        List<Analysis.SameDay> sameDay;
        Analysis.Combos combos;
        TestResult weekdayTest;
        int[] perDay = new int[7];
        String[] dayTop = new String[7];
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        Sample sm = s.sample;
        if (sm.size() == 0) return o;
        o.freq = Analysis.frequency(sm);
        o.evenOdd = Analysis.evenOdd(sm);
        o.sums = Analysis.sums(sm);
        if (sm.digits()) o.patterns = Analysis.patterns(sm);
        o.previous = Analysis.fromPrevious(sm);
        o.dups = Analysis.duplicates(sm);
        o.sameDay = Analysis.sameDay(s.h, Game.of(s.lottery), s.days);
        o.combos = Analysis.combos(sm);
        o.weekdayTest = Randomness.weekday(sm);
        Game g = sm.game;
        int values = g.digits() ? 100 : g.max + 1;
        int[][] cnt = new int[7][values];
        for (Draw d : sm.draws) {
            int w = d.date.getDayOfWeek().getValue() - 1;
            o.perDay[w]++;
            if (g.digits()) cnt[w][d.lastTwo()]++;
            else for (int v : d.nums) if (v <= g.max) cnt[w][v]++;
        }
        for (int w = 0; w < 7; w++) {
            if (o.perDay[w] == 0) continue;
            Integer[] idx = new Integer[values];
            for (int i = 0; i < values; i++) idx[i] = i;
            final int ww = w;
            java.util.Arrays.sort(idx, (x, y) -> cnt[ww][y] - cnt[ww][x]);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                if (cnt[w][idx[i]] == 0) break;
                if (sb.length() > 0) sb.append(",  ");
                sb.append(String.format(java.util.Locale.US, "%02d", idx[i])).append(" ×").append(cnt[w][idx[i]]);
            }
            o.dayTop[w] = sb.toString();
        }
        return o;
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        if (o.freq == null) {
            MainActivity.empty(a, body, s);
            return;
        }
        Game g = s.sample.game;
        int D = s.sample.size();

        // ---- hot & cold ----
        LinearLayout hot = ui.card(body, g.digits() ? T.t("HOT & COLD DIGITS") : T.t("HOT & COLD NUMBERS"));
        hot.addView(ui.note(MainActivity.scope(s)));
        if (g.digits()) {
            TextView t = ui.body(T.f("How often each digit came up in each position. A fair draw gives every cell %s. "
                    + "Warm cells came up more, cool ones less.", Fmt.num(D / 10.0)));
            t.setPadding(0, ui.dp(8), 0, ui.dp(8));
            hot.addView(t);
            hot.addView(new Charts.Heat(a, o.freq.positions), ui.matchWrap(0));
        } else {
            int n = o.freq.values.size();
            String[] labels = new String[n];
            double[] obs = new double[n], exp = new double[n], z = new double[n];
            for (int i = 0; i < n; i++) {
                Analysis.Cell c = o.freq.values.get(i);
                labels[i] = c.value == 1 || c.value % 5 == 0 ? String.valueOf(c.value) : "";
                obs[i] = c.observed;
                exp[i] = c.expected;
                z[i] = c.z;
            }
            TextView t = ui.body(T.f("Times each number was drawn. The dashed line is a fair draw's expectation (%s each).",
                    Fmt.num(o.freq.values.get(0).expected)));
            t.setPadding(0, ui.dp(8), 0, ui.dp(8));
            hot.addView(t);
            hot.addView(new Charts.Bars(a, labels, obs, exp, z, 1), ui.matchWrap(0));
        }
        LinearLayout cols = ui.hbox();
        cols.addView(cellList(a, T.t("Most frequent"), o.freq.hot(5)), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        rp.leftMargin = ui.dp(12);
        cols.addView(cellList(a, T.t("Least frequent"), o.freq.cold(5)), rp);
        hot.addView(cols, ui.matchWrap(12));
        TextView odl = ui.label(T.t("Longest absent"), Theme.DIM);
        odl.setPadding(0, ui.dp(12), 0, ui.dp(4));
        hot.addView(odl);
        StringBuilder ab = new StringBuilder();
        for (Analysis.Cell c : o.freq.overdue(5)) {
            if (ab.length() > 0) ab.append("   ");
            ab.append(c.label).append(": ").append(c.gap);
        }
        hot.addView(ui.body(T.f("%s  draws  (a value waits %s draws on average)", ab, Fmt.num(o.freq.meanGap))));
        hot.addView(verdict(a, o.freq.p, T.f("Are these differences real? Overall chi-square p = %s: %s The single most "
                        + "extreme value, allowing for the %d values compared: p = %s.", Combi.pText(o.freq.p),
                o.freq.p >= 0.05 ? T.t("the spread is what fair draws produce.") : T.t("more spread than fair draws usually produce."),
                g.digits() ? 40 : g.max, Combi.pText(o.freq.extremeP()))));

        if (g.digits()) {
            LinearLayout endc = ui.card(body, T.t("TWO-DIGIT ENDINGS (2 CIFRAS)"));
            endc.addView(ui.note(T.f("Each of the 100 endings is expected %s times.", Fmt.num(D / 100.0))));
            for (Analysis.Combo c : o.combos.endings2) endc.addView(ui.kv(c.label, c.count + "×", Theme.AMBER_LIGHT, null));
            endc.addView(verdict(a, o.combos.ending2MaxP, T.f("Most frequent ending, allowing for 100 endings: p = %s.",
                    Combi.pText(o.combos.ending2MaxP))));
        }

        if (o.freq.extraDraws > 0) {
            LinearLayout ex = ui.card(body, g.extraName.toUpperCase(T.L));
            int n = o.freq.extra.size();
            String[] labels = new String[n];
            double[] obs = new double[n], exp = new double[n], z = new double[n];
            for (int i = 0; i < n; i++) {
                Analysis.Cell c = o.freq.extra.get(i);
                labels[i] = c.label;
                obs[i] = c.observed;
                exp[i] = c.expected;
                z[i] = c.z;
            }
            ex.addView(ui.note(T.f("%d draws with a %s. Fair: %s each.", o.freq.extraDraws, g.extraName.toLowerCase(T.L), Fmt.num(exp[0]))));
            ex.addView(new Charts.Bars(a, labels, obs, exp, z, 1), ui.matchWrap(8));
            ex.addView(verdict(a, o.freq.extraP, T.f("Chi-square p = %s.", Combi.pText(o.freq.extraP))));
        }

        // ---- repeats ----
        LinearLayout rep = ui.card(body, T.t("REPEATED NUMBERS"));
        if (g.digits()) {
            rep.addView(sub(a, T.t("Inside one number")));
            for (int i = 0; i < 5; i++) {
                rep.addView(ui.kv(Analysis.patternName(i), Fmt.num(o.patterns.observed[i]), Theme.TEXT,
                        T.f("%s expected (%s of fair draws)", Fmt.num(o.patterns.expected[i]), Combi.percent(Analysis.PATTERN_P[i]))));
            }
            rep.addView(verdict(a, o.patterns.p, T.f("Poker test p = %s.", Combi.pText(o.patterns.p))));
        } else {
            Analysis.Combo cons = o.combos.shapes.get(0);
            rep.addView(sub(a, T.t("Inside one draw")));
            rep.addView(ui.kv(cons.label, T.f("%d draws", cons.count), Theme.TEXT, T.f("%s expected", Fmt.num(cons.expected))));
        }
        rep.addView(sub(a, T.t("From one draw to the next")));
        for (int m = 0; m < o.previous.observed.length; m++) {
            if (o.previous.expected[m] < 0.05 && o.previous.observed[m] == 0) continue;
            rep.addView(ui.kv(o.previous.labels[m], Fmt.num(o.previous.observed[m]), Theme.TEXT, T.f("%s expected", Fmt.num(o.previous.expected[m]))));
        }
        if (g.digits()) {
            rep.addView(ui.kv(T.t("Same last two digits as the draw before"), String.valueOf(o.dups.endingAgain), Theme.TEXT,
                    T.f("%s expected · p = %s", Fmt.num(o.dups.expectedEndingAgain), Combi.pText(o.dups.endingP))));
        }
        rep.addView(verdict(a, o.previous.p, T.f("Draw-to-draw repeats, p = %s.", Combi.pText(o.previous.p))));
        if (!o.sameDay.isEmpty()) {
            rep.addView(sub(a, T.t("Between draws on the same day")));
            for (Analysis.SameDay sd : o.sameDay) {
                double obs = 0, exp = 0;
                for (int m = 1; m < sd.shared.observed.length; m++) {
                    obs += m * sd.shared.observed[m];
                    exp += m * sd.shared.expected[m];
                }
                String note = T.f("%d days · %s in common, %s expected · p = %s", sd.days, Fmt.num(obs), Fmt.num(exp), Combi.pText(sd.shared.p));
                if (g.digits()) note += " · " + T.f("same ending %d× (%s expected)", sd.sameEnding, Fmt.num(sd.expectedSameEnding));
                rep.addView(ui.kv(sd.pair, T.f("%d identical", sd.identical), sd.identical > 0 ? Theme.AMBER : Theme.TEXT, note));
                for (int i = 0; i < Math.min(3, sd.examples.size()); i++) {
                    TextView ex = ui.text(sd.examples.get(i), 11, Theme.monoMedium(a), Theme.MUTED);
                    ex.setPadding(ui.dp(10), ui.dp(1), 0, ui.dp(1));
                    rep.addView(ex);
                }
            }
        }
        rep.addView(sub(a, T.t("The same result more than once")));
        rep.addView(ui.kv(T.t("Pairs of identical results"), String.valueOf(o.dups.coincidingPairs), Theme.TEXT,
                T.f("%s expected by chance (the birthday problem) · p = %s", Fmt.num(o.dups.expectedPairs), Combi.pText(o.dups.p))));
        for (String line : o.dups.lines) {
            TextView t = ui.text(line, 11, Theme.monoMedium(a), Theme.MUTED);
            t.setPadding(ui.dp(10), ui.dp(2), 0, ui.dp(2));
            rep.addView(t);
        }

        // ---- even / odd ----
        LinearLayout eo = ui.card(body, T.t("EVEN & ODD"));
        String[] eoLabels = new String[o.evenOdd.labels.length];
        for (int i = 0; i < eoLabels.length; i++) eoLabels[i] = T.f("%dE", i);
        eo.addView(ui.note(g.digits() ? T.t("Draws by number of even digits (0E = all odd). Bars: observed; dots: a fair draw.")
                : T.t("Draws by number of even numbers (0E = all odd). Bars: observed; dots: a fair draw.")));
        eo.addView(new Charts.Dist(a, eoLabels, o.evenOdd.observed, o.evenOdd.expected), ui.matchWrap(8));
        for (int i = 0; i < o.evenOdd.labels.length; i++) {
            eo.addView(ui.kv(o.evenOdd.labels[i], Fmt.num(o.evenOdd.observed[i]), Theme.TEXT, T.f("%s expected", Fmt.num(o.evenOdd.expected[i]))));
        }
        double evenShare = o.evenOdd.obsMean / g.count, fairShare = o.evenOdd.expMean / g.count;
        eo.addView(verdict(a, o.evenOdd.p, g.digits()
                ? T.f("%s of all digits were even (fair: %s). Chi-square p = %s.", Fmt.pct(evenShare), Fmt.pct(fairShare), Combi.pText(o.evenOdd.p))
                : T.f("%s of all numbers were even (fair: %s). Chi-square p = %s.", Fmt.pct(evenShare), Fmt.pct(fairShare), Combi.pText(o.evenOdd.p))));

        // ---- combinations & patterns ----
        LinearLayout cb = ui.card(body, T.t("RECURRING COMBINATIONS & PATTERNS"));
        if (g.digits()) {
            cb.addView(sub(a, T.t("Most frequent three-digit endings (3 cifras)")));
            for (Analysis.Combo c : o.combos.endings3.subList(0, Math.min(6, o.combos.endings3.size()))) {
                cb.addView(ui.kv(c.label, c.count + "×", Theme.AMBER_LIGHT, T.f("%s expected", Fmt.num(c.expected))));
            }
            cb.addView(verdict(a, o.combos.ending3MaxP, T.f("Most frequent of the 1,000 endings: p = %s.", Combi.pText(o.combos.ending3MaxP))));
        } else {
            cb.addView(sub(a, T.t("Pairs drawn together most often")));
            for (Analysis.Combo c : o.combos.pairs.subList(0, Math.min(6, o.combos.pairs.size()))) {
                cb.addView(ui.kv(c.label, c.count + "×", Theme.AMBER_LIGHT, T.f("%s expected", Fmt.num(c.expected))));
            }
            cb.addView(verdict(a, o.combos.pairMaxP, T.f("With %s possible pairs some are bound to come up often: the top "
                    + "pair's p, allowing for that, is %s.", Fmt.num(o.combos.pairCount), Combi.pText(o.combos.pairMaxP))));
            cb.addView(sub(a, T.t("Triples drawn together most often")));
            for (Analysis.Combo c : o.combos.triples.subList(0, Math.min(5, o.combos.triples.size()))) {
                cb.addView(ui.kv(c.label, c.count + "×", Theme.AMBER_LIGHT, T.f("%s expected", Fmt.num(c.expected))));
            }
            cb.addView(verdict(a, o.combos.tripleMaxP, T.f("Out of %s possible triples: p = %s.", Fmt.num(o.combos.tripleCount),
                    Combi.pText(o.combos.tripleMaxP))));
        }
        cb.addView(sub(a, T.t("Shapes")));
        for (int i = 0; i < o.combos.shapes.size(); i++) {
            Analysis.Combo c = o.combos.shapes.get(i);
            cb.addView(ui.kv(c.label, String.valueOf(c.count), Theme.TEXT, T.f("%s expected · p = %s", Fmt.num(c.expected),
                    Combi.pText(o.combos.shapeP.get(i)))));
        }

        // ---- sums ----
        LinearLayout sc = ui.card(body, T.t("SUM OF THE NUMBERS"));
        String[] labels = new String[40];
        double[][] rb = Charts.rebin(o.sums.observed, o.sums.expected, g.digits() ? 13 : 12, labels);
        int nb = (int) rb[2][0];
        sc.addView(ui.note(g.digits() ? T.t("Draws by the total of their digits; dots show a fair draw.")
                : T.t("Draws by the total of their numbers; dots show a fair draw.")));
        sc.addView(new Charts.Dist(a, java.util.Arrays.copyOf(labels, nb), rb[0], rb[1]), ui.matchWrap(8));
        int[] mid = Analysis.middleHalf(g);
        sc.addView(verdict(a, o.sums.p, T.f("Average %.1f (fair %.1f), spread %.1f (fair %.1f). Half of all fair draws total "
                + "%d–%d. Chi-square p = %s.", o.sums.obsMean, o.sums.expMean, o.sums.obsSd, o.sums.expSd, mid[0], mid[1],
                Combi.pText(o.sums.p))));

        // ---- weekdays ----
        LinearLayout wk = ui.card(body, T.t("BY DAY OF THE WEEK"));
        for (int w = 0; w < 7; w++) {
            if (o.perDay[w] == 0) continue;
            wk.addView(ui.kv(Fmt.day(w), T.f("%d draws", o.perDay[w]), Theme.TEXT,
                    g.digits() ? T.f("Top endings: %s", o.dayTop[w]) : T.f("Top numbers: %s", o.dayTop[w])));
        }
        TestResult wt = o.weekdayTest;
        wk.addView(verdict(a, wt.enough ? wt.p : Double.NaN, !wt.enough
                ? T.t("Too few draws per weekday to test whether the numbers depend on the day.")
                : wt.p >= 0.05 ? T.f("Do numbers depend on the weekday? p = %s: no sign they do.", Combi.pText(wt.p))
                : T.f("Do numbers depend on the weekday? p = %s.", Combi.pText(wt.p))));
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
            row.addView(ui.text(Fmt.num(c.observed) + "  " + Fmt.signed(c.z) + "σ", 11, Theme.monoMedium(a), Theme.MUTED));
            l.addView(row);
        }
        return l;
    }

    static TextView sub(MainActivity a, String s) {
        TextView t = a.ui.label(s, Theme.DIM);
        t.setPadding(0, a.ui.dp(12), 0, a.ui.dp(4));
        return t;
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
