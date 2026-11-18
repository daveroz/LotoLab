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

        // ---- at a glance: every test's verdict in one card ----
        LinearLayout gl = ui.card(body, T.t("AT A GLANCE"), T.t("descriptive"));
        gl.addView(ui.note(MainActivity.scope(s)));
        glance(a, gl, g.digits() ? T.t("Digit frequencies") : T.t("Number frequencies"), o.freq.p);
        glance(a, gl, T.t("Even & odd"), o.evenOdd.p);
        glance(a, gl, T.t("Sums"), o.sums.p);
        glance(a, gl, T.t("Repeats from draw to draw"), o.previous.p);
        if (g.digits()) glance(a, gl, T.t("Repeated digits in a number"), o.patterns.p);
        glance(a, gl, T.t("Day of the week"), o.weekdayTest.enough ? o.weekdayTest.p : Double.NaN);
        gl.addView(ui.note(T.t("Descriptive: what happened in these draws, compared with what fair draws produce. With this many "
                + "checks, one \"unusual\" now and then is expected by chance; the Randomness and Integrity tabs weigh them together.")),
                ui.matchWrap(6));

        // ---- hot & cold ----
        LinearLayout hot = ui.card(body, g.digits() ? T.t("HOT & COLD DIGITS") : T.t("HOT & COLD NUMBERS"), T.t("descriptive"));
        if (g.digits()) {
            hot.addView(ui.note(T.f("How often each digit came up in each position. A fair draw gives every cell %s; warm cells came "
                    + "up more, cool ones less.", Fmt.num(D / 10.0))));
            hot.addView(new Charts.Heat(a, o.freq.positions), ui.matchWrap(8));
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
            hot.addView(ui.note(T.f("Times each number was drawn. The dashed line is a fair draw's expectation (%s each).",
                    Fmt.num(o.freq.values.get(0).expected))));
            hot.addView(new Charts.Bars(a, labels, obs, exp, z, 1), ui.matchWrap(8));
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

        if (g.digits()) {
            LinearLayout endc = ui.card(body, T.t("TWO-DIGIT ENDINGS (LA PATA)"), T.t("descriptive"));
            endc.addView(ui.note(T.f("Each of the 100 endings is expected %s times.", Fmt.num(D / 100.0))));
            List<Analysis.Combo> e2 = o.combos.endings2;
            for (int i = 0; i < Math.min(3, e2.size()); i++) endc.addView(ui.kv(e2.get(i).label, e2.get(i).count + "×", Theme.AMBER_LIGHT, null));
            if (e2.size() > 3) {
                LinearLayout more = ui.details(endc, T.f("%d more", e2.size() - 3));
                for (int i = 3; i < e2.size(); i++) more.addView(ui.kv(e2.get(i).label, e2.get(i).count + "×", Theme.AMBER_LIGHT, null));
            }
            endc.addView(verdict(a, o.combos.ending2MaxP, T.f("Most frequent ending, allowing for 100 endings: p = %s.",
                    Combi.pText(o.combos.ending2MaxP))));
        }

        if (o.freq.extraDraws > 0) {
            LinearLayout ex = ui.card(body, g.extraName.toUpperCase(T.L), T.t("descriptive"));
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

        // ---- even / odd ----
        LinearLayout eo = ui.card(body, T.t("EVEN & ODD"), T.t("descriptive"));
        String[] eoLabels = new String[o.evenOdd.labels.length];
        for (int i = 0; i < eoLabels.length; i++) eoLabels[i] = T.f("%dE", i);
        eo.addView(ui.note(g.digits() ? T.t("Draws by number of even digits (0E = all odd). Bars: observed; dots: a fair draw.")
                : T.t("Draws by number of even numbers (0E = all odd). Bars: observed; dots: a fair draw.")));
        eo.addView(new Charts.Dist(a, eoLabels, o.evenOdd.observed, o.evenOdd.expected), ui.matchWrap(8));
        double evenShare = o.evenOdd.obsMean / g.count, fairShare = o.evenOdd.expMean / g.count;
        eo.addView(verdict(a, o.evenOdd.p, g.digits()
                ? T.f("%s of all digits were even and %s odd (fair: %s each). Chi-square p = %s.", Fmt.pct(evenShare), Fmt.pct(1 - evenShare),
                Fmt.pct(fairShare), Combi.pText(o.evenOdd.p))
                : T.f("%s of all numbers were even and %s odd (fair: %s even). Chi-square p = %s.", Fmt.pct(evenShare), Fmt.pct(1 - evenShare),
                Fmt.pct(fairShare), Combi.pText(o.evenOdd.p))));
        LinearLayout eod = ui.details(eo, T.t("Draw by draw counts"));
        for (int i = 0; i < o.evenOdd.labels.length; i++) {
            eod.addView(ui.kv(o.evenOdd.labels[i], Fmt.num(o.evenOdd.observed[i]), Theme.TEXT, T.f("%s expected", Fmt.num(o.evenOdd.expected[i]))));
        }

        // ---- sums ----
        LinearLayout sc = ui.card(body, T.t("SUM OF THE NUMBERS"), T.t("descriptive"));
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

        // ---- repeats ----
        LinearLayout rep = ui.card(body, T.t("REPEATED NUMBERS"), T.t("descriptive"));
        rep.addView(ui.kv(T.t("Pairs of identical results"), String.valueOf(o.dups.coincidingPairs), o.dups.coincidingPairs > 0 ? Theme.AMBER_LIGHT : Theme.TEXT,
                T.f("%s expected by chance (the birthday problem) · p = %s", Fmt.num(o.dups.expectedPairs), Combi.pText(o.dups.p))));
        if (g.digits()) {
            rep.addView(ui.kv(T.t("Same pata as the draw before"), String.valueOf(o.dups.endingAgain), Theme.TEXT,
                    T.f("%s expected · p = %s", Fmt.num(o.dups.expectedEndingAgain), Combi.pText(o.dups.endingP))));
        }
        rep.addView(verdict(a, o.previous.p, T.f("Numbers repeated from one draw to the next: p = %s.", Combi.pText(o.previous.p))));
        LinearLayout rd = ui.details(rep, T.t("Inside a draw, draw to draw, same day"));
        if (g.digits()) {
            rd.addView(sub(a, T.t("Inside one number")));
            for (int i = 0; i < 5; i++) {
                rd.addView(ui.kv(Analysis.patternName(i), Fmt.num(o.patterns.observed[i]), Theme.TEXT,
                        T.f("%s expected (%s of fair draws)", Fmt.num(o.patterns.expected[i]), Combi.percent(Analysis.PATTERN_P[i]))));
            }
            rd.addView(verdict(a, o.patterns.p, T.f("Poker test p = %s.", Combi.pText(o.patterns.p))));
        } else {
            Analysis.Combo cons = o.combos.shapes.get(0);
            rd.addView(sub(a, T.t("Inside one draw")));
            rd.addView(ui.kv(cons.label, T.f("%d draws", cons.count), Theme.TEXT, T.f("%s expected", Fmt.num(cons.expected))));
        }
        rd.addView(sub(a, T.t("From one draw to the next")));
        for (int m = 0; m < o.previous.observed.length; m++) {
            if (o.previous.expected[m] < 0.05 && o.previous.observed[m] == 0) continue;
            rd.addView(ui.kv(o.previous.labels[m], Fmt.num(o.previous.observed[m]), Theme.TEXT, T.f("%s expected", Fmt.num(o.previous.expected[m]))));
        }
        if (!o.sameDay.isEmpty()) {
            rd.addView(sub(a, T.t("Between draws on the same day")));
            for (Analysis.SameDay sd : o.sameDay) {
                double obs = 0, exp = 0;
                for (int m = 1; m < sd.shared.observed.length; m++) {
                    obs += m * sd.shared.observed[m];
                    exp += m * sd.shared.expected[m];
                }
                String note = T.f("%d days · %s in common, %s expected · p = %s", sd.days, Fmt.num(obs), Fmt.num(exp), Combi.pText(sd.shared.p));
                if (g.digits()) note += " · " + T.f("same ending %d× (%s expected)", sd.sameEnding, Fmt.num(sd.expectedSameEnding));
                rd.addView(ui.kv(sd.pair, T.f("%d identical", sd.identical), sd.identical > 0 ? Theme.AMBER : Theme.TEXT, note));
                for (int i = 0; i < Math.min(3, sd.examples.size()); i++) {
                    TextView ex = ui.text(sd.examples.get(i), Ui.SMALL, Theme.monoMedium(a), Theme.MUTED);
                    ex.setPadding(ui.dp(10), ui.dp(1), 0, ui.dp(1));
                    rd.addView(ex);
                }
            }
        }
        if (!o.dups.lines.isEmpty()) {
            rd.addView(sub(a, T.t("The same result more than once")));
            for (String line : o.dups.lines) {
                TextView t = ui.text(line, Ui.SMALL, Theme.monoMedium(a), Theme.MUTED);
                t.setPadding(ui.dp(10), ui.dp(2), 0, ui.dp(2));
                rd.addView(t);
            }
        }

        // ---- combinations & patterns ----
        LinearLayout cb = ui.card(body, T.t("RECURRING COMBINATIONS & PATTERNS"), T.t("descriptive"));
        if (g.digits()) {
            cb.addView(sub(a, T.t("Most frequent last three digits")));
            List<Analysis.Combo> e3 = o.combos.endings3;
            for (int i = 0; i < Math.min(3, e3.size()); i++) cb.addView(ui.kv(e3.get(i).label, e3.get(i).count + "×", Theme.AMBER_LIGHT, T.f("%s expected", Fmt.num(e3.get(i).expected))));
            cb.addView(verdict(a, o.combos.ending3MaxP, T.f("Most frequent of the 1,000 endings: p = %s.", Combi.pText(o.combos.ending3MaxP))));
            if (e3.size() > 3) {
                LinearLayout more = ui.details(cb, T.f("%d more", Math.min(6, e3.size()) - 3));
                for (int i = 3; i < Math.min(6, e3.size()); i++) more.addView(ui.kv(e3.get(i).label, e3.get(i).count + "×", Theme.AMBER_LIGHT, T.f("%s expected", Fmt.num(e3.get(i).expected))));
            }
        } else {
            cb.addView(sub(a, T.t("Pairs drawn together most often")));
            List<Analysis.Combo> pairs = o.combos.pairs;
            for (int i = 0; i < Math.min(3, pairs.size()); i++) cb.addView(ui.kv(pairs.get(i).label, pairs.get(i).count + "×", Theme.AMBER_LIGHT, T.f("%s expected", Fmt.num(pairs.get(i).expected))));
            cb.addView(verdict(a, o.combos.pairMaxP, T.f("With %s possible pairs some are bound to come up often: the top "
                    + "pair's p, allowing for that, is %s.", Fmt.num(o.combos.pairCount), Combi.pText(o.combos.pairMaxP))));
            LinearLayout more = ui.details(cb, T.t("More pairs and triples"));
            for (int i = 3; i < Math.min(6, pairs.size()); i++) more.addView(ui.kv(pairs.get(i).label, pairs.get(i).count + "×", Theme.AMBER_LIGHT, T.f("%s expected", Fmt.num(pairs.get(i).expected))));
            more.addView(sub(a, T.t("Triples drawn together most often")));
            for (Analysis.Combo c : o.combos.triples.subList(0, Math.min(5, o.combos.triples.size()))) {
                more.addView(ui.kv(c.label, c.count + "×", Theme.AMBER_LIGHT, T.f("%s expected", Fmt.num(c.expected))));
            }
            more.addView(verdict(a, o.combos.tripleMaxP, T.f("Out of %s possible triples: p = %s.", Fmt.num(o.combos.tripleCount),
                    Combi.pText(o.combos.tripleMaxP))));
        }
        LinearLayout sh = ui.details(cb, T.t("Shapes"));
        for (int i = 0; i < o.combos.shapes.size(); i++) {
            Analysis.Combo c = o.combos.shapes.get(i);
            sh.addView(ui.kv(c.label, String.valueOf(c.count), Theme.TEXT, T.f("%s expected · p = %s", Fmt.num(c.expected),
                    Combi.pText(o.combos.shapeP.get(i)))));
        }

        // ---- weekdays ----
        LinearLayout wk = ui.card(body, T.t("BY DAY OF THE WEEK"), T.t("descriptive"));
        TestResult wt = o.weekdayTest;
        wk.addView(verdict(a, wt.enough ? wt.p : Double.NaN, !wt.enough
                ? T.t("Too few draws per weekday to test whether the numbers depend on the day.")
                : wt.p >= 0.05 ? T.f("Do numbers depend on the weekday? p = %s: no sign they do.", Combi.pText(wt.p))
                : T.f("Do numbers depend on the weekday? p = %s.", Combi.pText(wt.p))));
        LinearLayout wd = ui.details(wk, T.t("Each day"));
        for (int w = 0; w < 7; w++) {
            if (o.perDay[w] == 0) continue;
            wd.addView(ui.kv(Fmt.day(w), T.f("%d draws", o.perDay[w]), Theme.TEXT,
                    g.digits() ? T.f("Top endings: %s", o.dayTop[w]) : T.f("Top numbers: %s", o.dayTop[w])));
        }
    }

    /** One line of the at-a-glance card: the check's name, its verdict colour and p-value. */
    private static void glance(MainActivity a, LinearLayout c, String name, double p) {
        Ui ui = a.ui;
        int color = Double.isNaN(p) ? Theme.DIM : p < 0.001 ? Theme.CORAL : p < 0.05 ? Theme.AMBER : Theme.GREEN;
        String verdict = Double.isNaN(p) ? T.t("Not enough data") : p < 0.001 ? T.t("Very unlikely by chance")
                : p < 0.05 ? T.t("Unusual (p < 0.05)") : T.t("Within chance");
        LinearLayout row = ui.hbox();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, ui.dp(5), 0, ui.dp(5));
        TextView dot = ui.text("●", 12, android.graphics.Typeface.DEFAULT, color);
        dot.setPadding(0, 0, ui.dp(10), 0);
        row.addView(dot);
        row.addView(ui.text(name, 14, Theme.uiMedium(a), Theme.TEXT), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(ui.text(verdict + (Double.isNaN(p) ? "" : " · p " + Combi.pText(p)), Ui.SMALL, Theme.monoMedium(a), color));
        c.addView(row);
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
