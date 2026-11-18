package app.loto.analysis;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Hot and cold numbers, la pata (two-digit endings), the quinta / superbalota, even and odd, and sums: always with
 * what a fair draw would give.
 */
final class StatsTab {
    private StatsTab() {}

    static final class Out {
        Analysis.Freq freq;
        Analysis.Dist evenOdd, sums;
        Analysis.Combos combos;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        Sample sm = s.sample;
        if (sm.size() == 0) return o;
        o.freq = Analysis.frequency(sm);
        o.evenOdd = Analysis.evenOdd(sm);
        o.sums = Analysis.sums(sm);
        if (sm.digits()) o.combos = Analysis.combos(sm);
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
        LinearLayout hot = ui.card(body, g.digits() ? T.t("HOT & COLD DIGITS") : T.t("HOT & COLD NUMBERS"), T.t("descriptive"));
        hot.addView(ui.note(MainActivity.scope(s)));
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
