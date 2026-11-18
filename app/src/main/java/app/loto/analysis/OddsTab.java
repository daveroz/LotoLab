package app.loto.analysis;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/** The mathematics of winning, from the rules alone, and a checker for any combination. */
final class OddsTab {
    private OddsTab() {}

    static final class Out {
        Odds.Check check;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        if (s.check != null && !s.check.trim().isEmpty()) o.check = Odds.check(s.games.get(0), s.sample, s.check);
        return o;
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        Game g = s.games.get(0);

        LinearLayout jp = ui.card(body, "CHANCE OF THE TOP PRIZE · " + s.lottery.toUpperCase(Locale.US));
        double p = Odds.jackpot(g);
        jp.addView(ui.text(Combi.oneIn(p), 28, Theme.monoBold(a), Theme.LED));
        jp.addView(ui.note(Combi.percent(p) + " per ticket · " + (g.digits()
                ? "4 cifras: the four digits in order"
                : g.count + " numbers from 1–" + g.max + (g.extraMax > 0 ? " plus the " + g.extraName.toLowerCase(Locale.US) + " (1–" + g.extraMax + ")" : ""))));
        if (g == Game.BALOTO || g == Game.REVANCHA) {
            jp.addView(ui.body("Revancha is a second draw for the same ticket, with the same odds: playing both gives two "
                    + "independent chances, about 1 in 7,700,784 of at least one jackpot."), ui.matchWrap(8));
        }

        LinearLayout tiers = ui.card(body, "EVERY PRIZE TIER");
        for (Odds.Tier t : Odds.tiers(g)) {
            tiers.addView(ui.kv(t.name, Combi.oneIn(t.p), Theme.AMBER_LIGHT, Combi.percent(t.p) + (t.note.isEmpty() ? "" : " · " + t.note)));
        }
        if (!g.digits()) {
            tiers.addView(ui.note("Chance of winning any prize: " + Combi.oneIn(Odds.anyPrize(g)) + "."), ui.matchWrap(8));
        } else {
            tiers.addView(ui.note("Each bet is placed separately (4, 3, 2 or 1 cifra, combinado). Prize amounts vary by "
                    + "operator and are not shown."), ui.matchWrap(8));
        }

        LinearLayout every = ui.card(body, "EVERY POSSIBLE COMBINATION");
        double combos = 1 / Predictor.theory(g);
        every.addView(ui.body(String.format(Locale.US, "There are %,d possible %s. Each one has exactly the same probability, "
                        + "%s (%s), in every draw.", Math.round(combos), g.digits() ? "4-digit numbers (0000–9999)" : "combinations"
                        + (Predictor.hasExtra(g) ? " with the " + g.extraName.toLowerCase(Locale.US) : ""),
                Combi.oneIn(1 / combos), Combi.percent(1 / combos))));
        every.addView(ui.body(g.digits()
                ? "0000 is exactly as likely as 4817. Numbers \"due\" to come out, or that came out yesterday, have the same chance as any other."
                : "01 02 03 04 05 is exactly as likely as any random-looking combination; it only looks special. What "
                + "differs between combinations is how many other people play them, which matters if a jackpot is shared."),
                ui.matchWrap(8));

        LinearLayout yr = ui.card(body, "PLAYING EVERY DRAW FOR A YEAR");
        int per = Odds.drawsPerYear(g);
        double year = Odds.perYear(g, p);
        yr.addView(ui.kv("Draws in a year", String.valueOf(per), Theme.TEXT, g.scheduleText()));
        yr.addView(ui.kv("Top prize at least once", Combi.percent(year), Theme.AMBER_LIGHT,
                "With one ticket per draw. On average that's one win every " + Fmt.num(1 / (p * per)) + " years."));
        if (g.digits()) {
            yr.addView(ui.kv("2 cifras at least once", Combi.percent(Odds.perYear(g, 0.01)), Theme.TEXT, "Last two digits, one bet per draw."));
        } else {
            yr.addView(ui.kv("Any prize at least once", Combi.percent(Odds.perYear(g, Odds.anyPrize(g))), Theme.TEXT, null));
        }

        LinearLayout ck = ui.card(body, "CHECK A COMBINATION");
        ck.addView(ui.note(g.digits() ? "Type a 4-digit number to see its probability and how it has done in " + MainActivity.label(s) + "."
                : "Type " + g.count + " numbers" + (g.extraMax > 0 ? " and optionally + " + g.extraName.toLowerCase(Locale.US) : "")
                + ", e.g. " + (g.extraMax > 0 ? "5 13 19 25 29 + 7" : "5 13 19 25 29") + "."));
        LinearLayout row = ui.hbox();
        final EditText in = ui.input(g.digits() ? "2820" : (g.extraMax > 0 ? "5 13 19 25 29 + 7" : "5 13 19 25 29"), g.digits());
        if (s.check != null) in.setText(s.check);
        ui.addWeighted(row, in, 0);
        TextView go = ui.primaryButton("Check", v -> {
            a.check = in.getText().toString();
            a.rerenderInPlace();
        });
        row.addView(go, ui.wrap(8));
        ck.addView(row, ui.matchWrap(10));
        if (o.check != null) {
            if (o.check.error != null) {
                TextView e = ui.body(o.check.error);
                e.setTextColor(Theme.AMBER);
                ck.addView(e, ui.matchWrap(10));
            } else {
                ck.addView(ui.balls(g, o.check.nums, o.check.extra, 34), ui.matchWrap(12));
                for (String line : o.check.lines) {
                    TextView t = ui.body("·  " + line);
                    t.setTextSize(12);
                    t.setPadding(0, ui.dp(4), 0, 0);
                    ck.addView(t);
                }
                ck.addView(ui.note("Past results never change the probability of the next draw: it stays "
                        + Combi.oneIn(o.check.theory) + "."), ui.matchWrap(8));
            }
        }
    }
}
