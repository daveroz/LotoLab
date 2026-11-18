package app.loto.analysis;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Possible irregularities, graded with care: an anomaly is not evidence of manipulation. */
final class IntegrityTab {
    private IntegrityTab() {}

    static Object compute(MainActivity.Sel s) {
        return Integrity.run(s.h, s.lottery, s.days);
    }

    static int color(Integrity.Level l) {
        switch (l) {
            case PERSISTENT: return Theme.CORAL;
            case ANOMALY: return Theme.AMBER;
            case DATA: return Theme.COOL;
            default: return Theme.GREEN;
        }
    }

    static String tagText(Integrity.Level l) {
        switch (l) {
            case PERSISTENT: return T.t("persistent deviation");
            case ANOMALY: return T.t("anomaly · likely chance");
            case DATA: return T.t("data");
            default: return T.t("clear");
        }
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Integrity.Report r = (Integrity.Report) data;
        Ui ui = a.ui;

        LinearLayout v = ui.card(body, T.f("INTEGRITY CHECK · %s", s.lottery.toUpperCase(T.L)));
        v.addView(ui.note(T.f("Every draw of %s (all its draw times) · %s · %s draws", s.lottery, s.periodLabel, Fmt.num(r.draws))));
        LinearLayout lamp = ui.hbox();
        lamp.setGravity(Gravity.CENTER_VERTICAL);
        lamp.setPadding(0, ui.dp(14), 0, ui.dp(6));
        TextView dot = ui.text("●", 26, android.graphics.Typeface.DEFAULT, color(r.overall));
        dot.setShadowLayer(ui.dp(10), 0, 0, color(r.overall));
        lamp.addView(dot);
        TextView head = ui.text(r.headline, 17, Theme.uiBold(a), Theme.TEXT);
        head.setPadding(ui.dp(12), 0, 0, 0);
        lamp.addView(head, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        v.addView(lamp);
        v.addView(ui.body(r.explanation));
        if (r.usable > 0) {
            v.addView(ui.kv(T.t("Tests run"), String.valueOf(r.usable), Theme.TEXT, null), ui.matchWrap(10));
            v.addView(ui.kv(T.t("Below p = 0.05 on their own"), String.valueOf(r.rawFlags), Theme.TEXT,
                    T.f("%s expected by chance alone · more than chance? p = %s", Fmt.num(r.expectedRawFlags), Combi.pText(r.excessP))));
            v.addView(ui.kv(T.t("Significant after correction"), String.valueOf(r.adjustedFlags),
                    r.adjustedFlags > 0 ? Theme.AMBER : Theme.GREEN, T.f("Holm–Bonferroni over all %d tests", r.usable)));
        }

        if (!r.findings.isEmpty()) {
            LinearLayout f = ui.card(body, T.t("FINDINGS"));
            boolean first = true;
            for (Integrity.Finding x : r.findings) {
                if (!first) f.addView(ui.divider());
                first = false;
                LinearLayout top = ui.hbox();
                top.setGravity(Gravity.CENTER_VERTICAL);
                top.addView(ui.text(x.title, 14, Theme.uiSemi(a), Theme.TEXT), new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                top.addView(ui.tag(tagText(x.level), color(x.level)));
                f.addView(top);
                TextView ev = ui.body(x.evidence);
                ev.setTextSize(12);
                ev.setPadding(0, ui.dp(4), 0, 0);
                f.addView(ev);
                TextView m = ui.note(x.meaning);
                m.setPadding(0, ui.dp(4), 0, 0);
                f.addView(m);
            }
        }

        LinearLayout all = ui.card(body, T.t("EVERY CHECK"));
        all.addView(ui.note(T.t("Each draw series tested on its own, plus same-day comparisons between series and a test for "
                + "results avoiding the numbers people bet most.")));
        String lastGame = null;
        for (TestResult t : r.tests) {
            if (!t.game.equals(lastGame)) {
                TextView gl = ui.label(t.game, Theme.AMBER_LIGHT);
                gl.setPadding(0, ui.dp(12), 0, ui.dp(2));
                all.addView(gl);
                lastGame = t.game;
            }
            LinearLayout row = ui.hbox();
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, ui.dp(4), 0, ui.dp(4));
            row.addView(ui.text(t.name, 12, Theme.uiMedium(a), Theme.TEXT_SOFT), new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            String p = t.enough ? Combi.pText(t.p) + " → " + Combi.pText(t.pAdj) : T.t("n/a");
            TextView pv = ui.text(p, 11, Theme.monoMedium(a), Ui.pColor(t));
            pv.setPadding(ui.dp(8), 0, 0, 0);
            row.addView(pv);
            all.addView(row);
        }
        all.addView(ui.note(T.t("p-value on its own → after correction. Green: as expected · amber: below 0.05 only before "
                + "correction · coral: significant after it · grey: not enough data.")), ui.matchWrap(10));

        LinearLayout ex = ui.card(body, T.t("AN ANOMALY IS NOT EVIDENCE OF MANIPULATION"));
        ex.addView(ui.body(T.t("Statistics can show that results are unlikely under a fair draw. It can't show why. In every long "
                + "series of fair draws some patterns look suspicious, and with enough tests something always comes out "
                + "\"significant\".")));
        ex.addView(ui.body(T.t("Even a persistent deviation has ordinary explanations: balls of slightly different weight or wear, "
                + "a drawing machine that changed, errors or gaps in the published results, or a change in how results are "
                + "recorded. Deliberate interference is one possibility among these, not the default.")), ui.matchWrap(8));
        ex.addView(ui.body(T.t("Real evidence of manipulation comes from outside the numbers: independent audits, inspection of the "
                + "balls and machines, the draw videos and records, witnesses. Concerns go to the game's operator and its "
                + "regulator (Coljuegos for Baloto and MiLoto; the departmental authority that licenses each chance game).")),
                ui.matchWrap(8));

        LinearLayout how = ui.card(body, T.t("HOW THE CHECK WORKS"));
        how.addView(ui.body(T.t("1. Every randomness test runs on every draw series separately.\n"
                + "2. Draws held on the same day are compared: do they share more numbers than chance?\n"
                + "3. Popular-number test: rigged results might avoid what people bet most (birthdays, dates).\n"
                + "4. All p-values are corrected together (Holm–Bonferroni), so running many tests doesn't manufacture findings.\n"
                + "5. Anything that survives is re-tested on the first and second half of the period. A systematic effect "
                + "shows in both; a coincidence rarely does.\n"
                + "6. The data is checked for impossible values and unexplained breaks in the schedule.")));
    }
}
