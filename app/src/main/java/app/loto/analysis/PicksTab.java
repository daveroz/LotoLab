package app.loto.analysis;

import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The models in depth: choose one, see its most probable combination, alternatives, the reasoning number by
 * number, its estimate next to the theoretical probability, and a backtest of whether it would have helped.
 * (Summary shows the chosen model's top pick at a glance.)
 */
final class PicksTab {
    private PicksTab() {}

    static final class Out {
        Predictor.Result result;
        Predictor.Backtest backtest;
        double theory;
        double freqP = Double.NaN;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        o.theory = Predictor.theory(s.games.get(0));
        if (s.sample.size() == 0) return o;
        o.result = Predictor.suggest(s.model, s.sample, s.seed);
        o.backtest = Predictor.backtest(s.model, s.sample, s.seed);
        o.freqP = Analysis.frequency(s.sample).p;
        return o;
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        Game g = s.games.get(0);

        LinearLayout mr = ui.chipRow(body, 14);
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
        LinearLayout mc = ui.card(body, s.model.title().toUpperCase(T.L));
        mc.addView(ui.note(s.model.description()));
        mc.addView(ui.note(MainActivity.scope(s)), ui.matchWrap(4));
        if (r.top != null) {
            TextView h = ui.label(r.hasTop ? T.t("Highest estimated probability under this model") : T.t("Suggested combination"), Theme.AMBER_LIGHT);
            h.setPadding(0, ui.dp(14), 0, ui.dp(8));
            mc.addView(h);
            mc.addView(ui.balls(g, r.top.nums, r.top.extra, 38));
            mc.addView(odds(a, r.top), ui.matchWrap(10));
            for (String line : r.top.reasons) {
                TextView t = ui.body("·  " + line);
                t.setTextSize(12);
                t.setPadding(0, ui.dp(3), 0, 0);
                mc.addView(t);
            }
        }
        boolean freqModel = s.model == Predictor.Model.FREQUENCY || s.model == Predictor.Model.RECENT || s.model == Predictor.Model.BAYES;
        mc.addView(StatsTab.verdict(a, freqModel ? o.freqP : Double.NaN, r.verdict));

        if (!r.alternatives.isEmpty()) {
            LinearLayout alt = ui.card(body, T.t("MORE FROM THIS MODEL"));
            boolean first = true;
            for (Predictor.Suggestion sg : r.alternatives) {
                if (!first) alt.addView(ui.divider());
                first = false;
                alt.addView(ui.balls(g, sg.nums, sg.extra, 32), ui.matchWrap(4));
                alt.addView(odds(a, sg), ui.matchWrap(6));
                int shown = 0;
                for (int i = 0; i < sg.reasons.size(); i++) {
                    boolean last = i == sg.reasons.size() - 1;
                    if (shown++ >= 3 && !last) continue;
                    TextView t = ui.note("·  " + sg.reasons.get(i));
                    t.setPadding(0, ui.dp(2), 0, 0);
                    alt.addView(t);
                }
            }
            alt.addView(ui.secondaryButton(T.t("New suggestions"), v -> {
                a.seed++;
                a.rerenderInPlace();
            }), ui.matchWrap(14));
        }

        if (o.backtest != null) {
            LinearLayout bt = ui.card(body, T.t("BACKTEST"));
            bt.addView(StatsTab.verdict(a, o.backtest.draws == 0 ? Double.NaN : o.backtest.p < 0.05 ? 0.01 : 0.5, o.backtest.summary));
            bt.addView(ui.note(T.t("A model that has found a real bias should match more than chance here, draw after draw. In a "
                    + "fair lottery every model, including quick pick, averages the same.")), ui.matchWrap(8));
        }

        LinearLayout th = ui.card(body, T.t("THEORY VS. HISTORY"));
        th.addView(ui.body(T.f("Every single combination has a theoretical probability of %s in every draw, fixed by the rules. "
                + "The models only reweight numbers by past frequencies: if the draw is fair, the past doesn't change the next "
                + "result and no model beats the theoretical odds. The expected return of every ticket is negative; the "
                + "suggestions are for curiosity, not a way to improve your odds.", Combi.oneIn(o.theory))));
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
