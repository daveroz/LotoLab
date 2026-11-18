package app.loto.analysis;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

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
        // per weekday: how many draws and what came up most
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
                sb.append(String.format(Locale.US, "%02d", idx[i])).append(" ×").append(cnt[w][idx[i]]);
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
        LinearLayout hot = ui.card(body, g.digits() ? "HOT & COLD DIGITS" : "HOT & COLD NUMBERS");
        hot.addView(ui.note(MainActivity.scope(s)));
        if (g.digits()) {
            TextView t = ui.body("How often each digit came up in each position. A fair draw gives every cell "
                    + Fmt.num(D / 10.0) + ". Warm cells came up more, cool ones less.");
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
            TextView t = ui.body("Times each number was drawn. The dashed line is a fair draw's expectation ("
                    + Fmt.num(o.freq.values.get(0).expected) + " each).");
            t.setPadding(0, ui.dp(8), 0, ui.dp(8));
            hot.addView(t);
            hot.addView(new Charts.Bars(a, labels, obs, exp, z, 1), ui.matchWrap(0));
        }
        LinearLayout cols = ui.hbox();
        cols.addView(cellList(a, "Most frequent", o.freq.hot(5)), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        rp.leftMargin = ui.dp(12);
        cols.addView(cellList(a, "Least frequent", o.freq.cold(5)), rp);
        hot.addView(cols, ui.matchWrap(12));
        LinearLayout od = ui.vbox();
        TextView odl = ui.label("Longest absent", Theme.DIM);
        odl.setPadding(0, ui.dp(12), 0, ui.dp(4));
        od.addView(odl);
        StringBuilder ab = new StringBuilder();
        for (Analysis.Cell c : o.freq.overdue(5)) {
            if (ab.length() > 0) ab.append("   ");
            ab.append(c.label).append(": ").append(c.gap);
        }
        od.addView(ui.body(ab + "  draws  (a value waits " + Fmt.num(o.freq.meanGap) + " draws on average)"));
        hot.addView(od);
        hot.addView(verdict(a, o.freq.p, String.format(Locale.US, "Are these differences real? Overall chi-square p = %s: %s "
                        + "The single most extreme value, allowing for the %d values compared: p = %s.", Combi.pText(o.freq.p),
                o.freq.p >= 0.05 ? "the spread is what fair draws produce." : "more spread than fair draws usually produce.",
                g.digits() ? 40 : g.max, Combi.pText(o.freq.extremeP()))));

        if (g.digits()) {
            LinearLayout endc = ui.card(body, "TWO-DIGIT ENDINGS (2 CIFRAS)");
            endc.addView(ui.note("Each of the 100 endings is expected " + Fmt.num(D / 100.0) + " times."));
            for (Analysis.Combo c : o.combos.endings2) endc.addView(ui.kv(c.label, c.count + "×", Theme.AMBER_LIGHT, null));
            endc.addView(verdict(a, o.combos.ending2MaxP, "Most frequent ending, allowing for 100 endings: p = "
                    + Combi.pText(o.combos.ending2MaxP) + "."));
        }

        if (o.freq.extraDraws > 0) {
            LinearLayout ex = ui.card(body, g.extraName.toUpperCase(Locale.US));
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
            ex.addView(ui.note(o.freq.extraDraws + " draws with a " + g.extraName.toLowerCase(Locale.US) + ". Fair: "
                    + Fmt.num(exp[0]) + " each."));
            ex.addView(new Charts.Bars(a, labels, obs, exp, z, 1), ui.matchWrap(8));
            ex.addView(verdict(a, o.freq.extraP, "Chi-square p = " + Combi.pText(o.freq.extraP) + "."));
        }

        // ---- repeats ----
        LinearLayout rep = ui.card(body, "REPEATED NUMBERS");
        if (g.digits()) {
            rep.addView(sub(a, "Inside one number"));
            for (int i = 0; i < 5; i++) {
                rep.addView(ui.kv(Analysis.PATTERNS[i], Fmt.num(o.patterns.observed[i]), Theme.TEXT,
                        Fmt.num(o.patterns.expected[i]) + " expected (" + Combi.percent(Analysis.PATTERN_P[i]) + " of fair draws)"));
            }
            rep.addView(verdict(a, o.patterns.p, "Poker test p = " + Combi.pText(o.patterns.p) + "."));
        } else {
            Analysis.Combo cons = o.combos.shapes.get(0);
            rep.addView(sub(a, "Inside one draw"));
            rep.addView(ui.kv(cons.label, cons.count + " draws", Theme.TEXT, Fmt.num(cons.expected) + " expected"));
        }
        rep.addView(sub(a, "From one draw to the next"));
        for (int m = 0; m < o.previous.observed.length; m++) {
            if (o.previous.expected[m] < 0.05 && o.previous.observed[m] == 0) continue;
            rep.addView(ui.kv(o.previous.labels[m], Fmt.num(o.previous.observed[m]), Theme.TEXT, Fmt.num(o.previous.expected[m]) + " expected"));
        }
        if (g.digits()) {
            rep.addView(ui.kv("Same last two digits as the draw before", String.valueOf(o.dups.endingAgain), Theme.TEXT,
                    Fmt.num(o.dups.expectedEndingAgain) + " expected · p = " + Combi.pText(o.dups.endingP)));
        }
        rep.addView(verdict(a, o.previous.p, "Draw-to-draw repeats, p = " + Combi.pText(o.previous.p) + "."));
        if (!o.sameDay.isEmpty()) {
            rep.addView(sub(a, "Between draws on the same day"));
            for (Analysis.SameDay sd : o.sameDay) {
                double obs = 0, exp = 0;
                for (int m = 1; m < sd.shared.observed.length; m++) {
                    obs += m * sd.shared.observed[m];
                    exp += m * sd.shared.expected[m];
                }
                String note = String.format(Locale.US, "%d days · %s in common, %s expected · p = %s", sd.days, Fmt.num(obs),
                        Fmt.num(exp), Combi.pText(sd.shared.p));
                if (g.digits()) note += String.format(Locale.US, " · same ending %d× (%s expected)", sd.sameEnding, Fmt.num(sd.expectedSameEnding));
                rep.addView(ui.kv(sd.pair, sd.identical + " identical", sd.identical > 0 ? Theme.AMBER : Theme.TEXT, note));
                for (int i = 0; i < Math.min(3, sd.examples.size()); i++) {
                    TextView ex = ui.text(sd.examples.get(i), 11, Theme.monoMedium(a), Theme.MUTED);
                    ex.setPadding(ui.dp(10), ui.dp(1), 0, ui.dp(1));
                    rep.addView(ex);
                }
            }
        }
        rep.addView(sub(a, "The same result more than once"));
        rep.addView(ui.kv("Pairs of identical results", String.valueOf(o.dups.coincidingPairs), Theme.TEXT,
                Fmt.num(o.dups.expectedPairs) + " expected by chance (the birthday problem) · p = " + Combi.pText(o.dups.p)));
        for (String line : o.dups.lines) {
            TextView t = ui.text(line, 11, Theme.monoMedium(a), Theme.MUTED);
            t.setPadding(ui.dp(10), ui.dp(2), 0, ui.dp(2));
            rep.addView(t);
        }

        // ---- even / odd ----
        LinearLayout eo = ui.card(body, "EVEN & ODD");
        String[] eoLabels = new String[o.evenOdd.labels.length];
        for (int i = 0; i < eoLabels.length; i++) eoLabels[i] = i + "E";
        eo.addView(ui.note("Draws by number of even " + (g.digits() ? "digits" : "numbers") + " (0E = all odd). Bars: observed; "
                + "dots: a fair draw."));
        eo.addView(new Charts.Dist(a, eoLabels, o.evenOdd.observed, o.evenOdd.expected), ui.matchWrap(8));
        for (int i = 0; i < o.evenOdd.labels.length; i++) {
            eo.addView(ui.kv(o.evenOdd.labels[i], Fmt.num(o.evenOdd.observed[i]), Theme.TEXT, Fmt.num(o.evenOdd.expected[i]) + " expected"));
        }
        double evenShare = o.evenOdd.obsMean / g.count, fairShare = o.evenOdd.expMean / g.count;
        eo.addView(verdict(a, o.evenOdd.p, String.format(Locale.US, "%s of all %s were even (fair: %s). Chi-square p = %s.",
                Fmt.pct(evenShare), g.digits() ? "digits" : "numbers", Fmt.pct(fairShare), Combi.pText(o.evenOdd.p))));

        // ---- combinations & patterns ----
        LinearLayout cb = ui.card(body, "RECURRING COMBINATIONS & PATTERNS");
        if (g.digits()) {
            cb.addView(sub(a, "Most frequent three-digit endings (3 cifras)"));
            for (Analysis.Combo c : o.combos.endings3.subList(0, Math.min(6, o.combos.endings3.size()))) {
                cb.addView(ui.kv(c.label, c.count + "×", Theme.AMBER_LIGHT, Fmt.num(c.expected) + " expected"));
            }
            cb.addView(verdict(a, o.combos.ending3MaxP, "Most frequent of the 1,000 endings: p = " + Combi.pText(o.combos.ending3MaxP) + "."));
        } else {
            cb.addView(sub(a, "Pairs drawn together most often"));
            for (Analysis.Combo c : o.combos.pairs.subList(0, Math.min(6, o.combos.pairs.size()))) {
                cb.addView(ui.kv(c.label, c.count + "×", Theme.AMBER_LIGHT, Fmt.num(c.expected) + " expected"));
            }
            cb.addView(verdict(a, o.combos.pairMaxP, String.format(Locale.US, "With %,d possible pairs some are bound to "
                    + "come up often: the top pair's p, allowing for that, is %s.", o.combos.pairCount, Combi.pText(o.combos.pairMaxP))));
            cb.addView(sub(a, "Triples drawn together most often"));
            for (Analysis.Combo c : o.combos.triples.subList(0, Math.min(5, o.combos.triples.size()))) {
                cb.addView(ui.kv(c.label, c.count + "×", Theme.AMBER_LIGHT, Fmt.num(c.expected) + " expected"));
            }
            cb.addView(verdict(a, o.combos.tripleMaxP, String.format(Locale.US, "Out of %,d possible triples: p = %s.",
                    o.combos.tripleCount, Combi.pText(o.combos.tripleMaxP))));
        }
        cb.addView(sub(a, "Shapes"));
        for (int i = 0; i < o.combos.shapes.size(); i++) {
            Analysis.Combo c = o.combos.shapes.get(i);
            cb.addView(ui.kv(c.label, String.valueOf(c.count), Theme.TEXT, Fmt.num(c.expected) + " expected · p = "
                    + Combi.pText(o.combos.shapeP.get(i))));
        }

        // ---- sums ----
        LinearLayout sc = ui.card(body, "SUM OF THE NUMBERS");
        String[] labels = new String[40];
        double[][] rb = Charts.rebin(o.sums.observed, o.sums.expected, g.digits() ? 13 : 12, labels);
        int nb = (int) rb[2][0];
        sc.addView(ui.note("Draws by the total of their " + (g.digits() ? "digits" : "numbers") + "; dots show a fair draw."));
        sc.addView(new Charts.Dist(a, java.util.Arrays.copyOf(labels, nb), rb[0], rb[1]), ui.matchWrap(8));
        int[] mid = Analysis.middleHalf(g);
        sc.addView(verdict(a, o.sums.p, String.format(Locale.US, "Average %.1f (fair %.1f), spread %.1f (fair %.1f). Half of all "
                + "fair draws total %d–%d. Chi-square p = %s.", o.sums.obsMean, o.sums.expMean, o.sums.obsSd, o.sums.expSd,
                mid[0], mid[1], Combi.pText(o.sums.p))));

        // ---- weekdays ----
        LinearLayout wk = ui.card(body, "BY DAY OF THE WEEK");
        for (int w = 0; w < 7; w++) {
            if (o.perDay[w] == 0) continue;
            wk.addView(ui.kv(Game.DAY_NAMES[w], o.perDay[w] + " draws", Theme.TEXT,
                    (g.digits() ? "Top endings: " : "Top numbers: ") + o.dayTop[w]));
        }
        TestResult wt = o.weekdayTest;
        wk.addView(verdict(a, wt.enough ? wt.p : Double.NaN, wt.enough
                ? "Do numbers depend on the weekday? p = " + Combi.pText(wt.p) + (wt.p >= 0.05 ? ": no sign they do." : ".")
                : "Too few draws per weekday to test whether the numbers depend on the day."));
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
        LinearLayout v = a.ui.status(color, Double.isNaN(p) ? "Not enough data" : p < 0.001 ? "Very unlikely by chance"
                : p < 0.05 ? "Unusual (p < 0.05)" : "Within chance", text);
        v.setPadding(0, a.ui.dp(10), 0, 0);
        return v;
    }
}
