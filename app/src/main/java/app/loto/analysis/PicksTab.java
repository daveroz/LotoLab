package app.loto.analysis;

import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/** Suggested combinations by model, with the reasoning, the model's estimate next to the theoretical probability,
 *  and a backtest of whether the model would actually have helped. */
final class PicksTab {
    private PicksTab() {}

    static final class Out {
        Predictor.Result result;
        Predictor.Backtest backtest;
        double theory;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        o.theory = Predictor.theory(s.games.get(0));
        if (s.sample.size() == 0) return o;
        o.result = Predictor.suggest(s.model, s.sample, s.seed);
        o.backtest = Predictor.backtest(s.model, s.sample, s.seed);
        return o;
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        Game g = s.games.get(0);

        LinearLayout th = ui.card(body, "THEORY VS. HISTORY");
        TextView big = ui.text(Combi.oneIn(o.theory), 24, Theme.monoBold(a), Theme.LED);
        th.addView(big);
        th.addView(ui.body("Theoretical probability of any one " + (g.digits() ? "4-digit number" : "combination"
                + (Predictor.hasExtra(g) ? " with its " + g.extraName.toLowerCase(Locale.US) : "")) + ". It's fixed by the "
                + "rules and identical for every combination in every draw."), ui.matchWrap(4));
        th.addView(ui.body("The models below estimate from past frequencies instead. Those estimates only describe the past: "
                + "if the draw is fair, the past doesn't change the next result, and no model beats the theoretical odds. "
                + "Each model's backtest shows how it would really have done."), ui.matchWrap(8));
        if (o.result == null) {
            MainActivity.empty(a, body, s);
            return;
        }

        LinearLayout mr = ui.chipRow(body, 14);
        for (Predictor.Model m : Predictor.Model.values()) {
            mr.addView(ui.chip(m.title, m == s.model, v -> {
                a.model = m;
                a.rerenderInPlace();
            }), ui.wrap(mr.getChildCount() == 0 ? 0 : 8));
        }
        ui.reveal(mr, s.model.ordinal());

        Predictor.Result r = o.result;
        LinearLayout mc = ui.card(body, s.model.title.toUpperCase(Locale.US));
        mc.addView(ui.note(s.model.description));
        mc.addView(ui.note(MainActivity.scope(s)), ui.matchWrap(4));
        if (r.top != null) {
            TextView h = ui.label(r.hasTop ? "Highest estimated probability under this model" : "Suggested combination", Theme.AMBER_LIGHT);
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
        mc.addView(StatsTab.verdict(a, s.model == Predictor.Model.FREQUENCY || s.model == Predictor.Model.RECENT
                || s.model == Predictor.Model.BAYES ? Analysis.frequency(s.sample).p : Double.NaN, r.verdict));

        if (!r.alternatives.isEmpty()) {
            LinearLayout alt = ui.card(body, "MORE FROM THIS MODEL");
            boolean first = true;
            for (Predictor.Suggestion sg : r.alternatives) {
                if (!first) alt.addView(ui.divider());
                first = false;
                alt.addView(ui.balls(g, sg.nums, sg.extra, 32), ui.matchWrap(4));
                alt.addView(odds(a, sg), ui.matchWrap(6));
                int shown = 0;
                for (String line : sg.reasons) {
                    if (shown++ >= 3 && !line.startsWith("Shape")) continue;
                    TextView t = ui.note("·  " + line);
                    t.setPadding(0, ui.dp(2), 0, 0);
                    alt.addView(t);
                }
            }
            alt.addView(ui.secondaryButton("New suggestions", v -> {
                a.seed++;
                a.rerenderInPlace();
            }), ui.matchWrap(14));
        }

        if (o.backtest != null) {
            LinearLayout bt = ui.card(body, "BACKTEST");
            bt.addView(StatsTab.verdict(a, o.backtest.draws == 0 ? Double.NaN : o.backtest.p < 0.05 ? 0.01 : 0.5, o.backtest.summary));
            bt.addView(ui.note("A model that has found a real bias should match more than chance here, draw after draw. In a "
                    + "fair lottery every model, including quick pick, averages the same."), ui.matchWrap(8));
        }

        LinearLayout rg = ui.card(body, "PLAY RESPONSIBLY");
        rg.addView(ui.body("The expected return of every lottery ticket is negative: on average, more is paid in than paid "
                + "out. Suggestions here are for curiosity, not a way to improve your odds. If betting stops being fun, "
                + "take a break; Baloto's own self-exclusion programme is at baloto.com (\"Juego responsable\")."));
    }

    private static LinearLayout odds(MainActivity a, Predictor.Suggestion sg) {
        Ui ui = a.ui;
        LinearLayout l = ui.vbox();
        double ratio = sg.theoryP > 0 ? sg.modelP / sg.theoryP : 1;
        l.addView(ui.kv("Theoretical probability", Combi.oneIn(sg.theoryP), Theme.TEXT, null));
        l.addView(ui.kv("Model's estimate", Combi.oneIn(sg.modelP), Math.abs(ratio - 1) < 0.005 ? Theme.TEXT : Theme.AMBER_LIGHT,
                Math.abs(ratio - 1) < 0.005 ? "Same as theory: this model treats every combination alike."
                        : String.format(Locale.US, "%.2f× the theoretical, if the model's assumption were true.", ratio)));
        return l;
    }
}
