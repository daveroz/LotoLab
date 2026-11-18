package app.loto.analysis;

import android.widget.LinearLayout;

/**
 * The mathematics of winning, from the rules alone: the top prize, every tier, every combination, a year of
 * play. (Checking a particular number is on the Summary, next to the suggested pick.)
 */
final class OddsTab {
    private OddsTab() {}

    static Object compute(MainActivity.Sel s) {
        return s.games.get(0);
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Ui ui = a.ui;
        Game g = (Game) data;

        LinearLayout jp = ui.card(body, T.f("CHANCE OF THE TOP PRIZE · %s", s.lottery.toUpperCase(T.L)));
        double p = Odds.jackpot(g);
        jp.addView(ui.text(Combi.oneIn(p), 28, Theme.monoBold(a), Theme.LED));
        jp.addView(ui.note(g.digits() ? T.f("%s per ticket · 4 cifras: the four digits in order", Combi.percent(p))
                : g.extraMax > 0 ? T.f("%s per ticket · %d numbers from 1–%d plus the superbalota (1–%d)", Combi.percent(p), g.count, g.max, g.extraMax)
                : T.f("%s per ticket · %d numbers from 1–%d", Combi.percent(p), g.count, g.max)));
        if (g == Game.BALOTO || g == Game.REVANCHA) {
            jp.addView(ui.body(T.t("Revancha is a second draw for the same ticket, with the same odds: playing both gives two "
                    + "independent chances, about 1 in 7,700,784 of at least one jackpot.")), ui.matchWrap(8));
        }

        LinearLayout tiers = ui.card(body, T.t("EVERY PRIZE TIER"));
        for (Odds.Tier t : Odds.tiers(g)) {
            tiers.addView(ui.kv(t.name, Combi.oneIn(t.p), Theme.AMBER_LIGHT, Combi.percent(t.p) + (t.note.isEmpty() ? "" : " · " + t.note)));
        }
        tiers.addView(ui.note(g.digits() ? T.t("Each bet is placed separately (4, 3, 2 or 1 cifra, combinado). Prize amounts vary by "
                + "operator and are not shown.") : T.f("Chance of winning any prize: %s.", Combi.oneIn(Odds.anyPrize(g)))), ui.matchWrap(8));

        LinearLayout every = ui.card(body, T.t("EVERY POSSIBLE COMBINATION"));
        double combos = 1 / Predictor.theory(g);
        every.addView(ui.body(g.digits()
                ? T.f("There are %s possible 4-digit numbers (0000–9999). Each one has exactly the same probability, %s (%s), in every draw.",
                Fmt.num(combos), Combi.oneIn(1 / combos), Combi.percent(1 / combos))
                : T.f("There are %s possible combinations. Each one has exactly the same probability, %s (%s), in every draw.",
                Fmt.num(combos), Combi.oneIn(1 / combos), Combi.percent(1 / combos))));
        every.addView(ui.body(g.digits()
                ? T.t("0000 is exactly as likely as 4817. Numbers \"due\" to come out, or that came out yesterday, have the same chance as any other.")
                : T.t("01 02 03 04 05 is exactly as likely as any random-looking combination; it only looks special. What "
                + "differs between combinations is how many other people play them, which matters if a jackpot is shared.")),
                ui.matchWrap(8));

        LinearLayout yr = ui.card(body, T.t("PLAYING EVERY DRAW FOR A YEAR"));
        int per = Odds.drawsPerYear(g);
        double year = Odds.perYear(g, p);
        yr.addView(ui.kv(T.t("Draws in a year"), String.valueOf(per), Theme.TEXT, g.scheduleText()));
        yr.addView(ui.kv(T.t("Top prize at least once"), Combi.percent(year), Theme.AMBER_LIGHT,
                T.f("With one ticket per draw. On average that's one win every %s years.", Fmt.num(1 / (p * per)))));
        if (g.digits()) {
            yr.addView(ui.kv(T.t("2 cifras at least once"), Combi.percent(Odds.perYear(g, 0.01)), Theme.TEXT, T.t("Last two digits, one bet per draw.")));
        } else {
            yr.addView(ui.kv(T.t("Any prize at least once"), Combi.percent(Odds.perYear(g, Odds.anyPrize(g))), Theme.TEXT, null));
        }
    }
}
