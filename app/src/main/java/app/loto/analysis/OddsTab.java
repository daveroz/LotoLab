package app.loto.analysis;

import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The mathematics of winning, from the rules alone: the top prize, every tier and (for El Dorado) the official
 * prize plan in pesos, then the probability-optimized betting pack across every lottery, and a year of play.
 * (Checking a particular number is on the Summary.)
 */
final class OddsTab {
    private OddsTab() {}

    static final class Out {
        Game game;
        int year;
        Pack.Result pack;
        /** Numbers for each pack item, in order (chance endings or a lotto ticket). */
        final List<List<String>> numbers = new ArrayList<>();
        /** Lotto tickets: {nums, {extra}} per item, null for chance items. */
        final List<int[][]> tickets = new ArrayList<>();
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        o.game = s.games.get(0);
        ZonedDateTime now = ZonedDateTime.now(MainActivity.BOGOTA);
        o.year = now.getYear();
        List<Pack.Draw0> chance = new ArrayList<>();
        for (int i = 0; i <= 8 && chance.size() < 3; i++) {
            LocalDate d = now.toLocalDate().plusDays(i);
            List<Pack.Draw0> day = new ArrayList<>();
            for (Game g : Game.of("Dorado")) {
                int t = g.timeOn(d);
                if (t >= 0 && ZonedDateTime.of(d, LocalTime.of(t / 60, t % 60), MainActivity.BOGOTA).isAfter(now)) day.add(new Pack.Draw0(g, d, t));
            }
            day.sort((x, y) -> Integer.compare(x.minutes, y.minutes));
            for (Pack.Draw0 x : day) if (chance.size() < 3) chance.add(x);
        }
        Pack.Draw0 baloto = next(Game.BALOTO, now), miloto = next(Game.MILOTO, now);
        o.pack = Pack.solve(s.packObjective, s.packTarget, s.packStake, chance, baloto, miloto, o.year);
        for (Pack.Item it : o.pack.items) {
            List<Game> one = new ArrayList<>();
            one.add(it.draw.game);
            Sample full = s.h.sample(one, 0);
            List<String> nums = new ArrayList<>();
            int[][] ticket = null;
            if (full.size() >= Recommender.WARMUP) {
                Recommender.Rec r = Recommender.recommend(full, it.draw.date, false);
                Predictor.Weights w = Models.over(full, r.model == Predictor.Model.ENSEMBLE).weights(r.model);
                if (it.bet != null) nums = endings(w, it.bet.digits, it.count, Recommender.seed(it.draw.game, it.draw.date));
                else ticket = new int[][]{r.nums, {r.extra}};
            } else if (it.bet != null) {
                for (int k = 0; k < Math.min(it.count, 8); k++) nums.add(String.format(java.util.Locale.US, "%0" + it.bet.digits + "d", k));
            }
            o.numbers.add(nums);
            o.tickets.add(ticket);
        }
        return o;
    }

    private static Pack.Draw0 next(Game g, ZonedDateTime now) {
        for (int i = 0; i <= 8; i++) {
            LocalDate d = now.toLocalDate().plusDays(i);
            int t = g.timeOn(d);
            if (t >= 0 && ZonedDateTime.of(d, LocalTime.of(t / 60, t % 60), MainActivity.BOGOTA).isAfter(now)) return new Pack.Draw0(g, d, t);
        }
        return null;
    }

    /**
     * The k endings of `digits` digits to bet on, in the order of the draw's recommended model (they all have the
     * same probability; the order only makes the list reproducible). At most the first eight are returned.
     */
    static List<String> endings(Predictor.Weights w, int digits, int k, long seed) {
        int n = (int) Math.pow(10, digits);
        double[] score = new double[n];
        double[] jitter = new double[n];
        java.util.Random r = new java.util.Random(seed);
        for (int v = 0; v < n; v++) {
            double p = 1;
            int x = v;
            for (int pos = 3; pos >= 4 - digits; pos--) {
                double[] row = w.w[pos];
                double sum = 0;
                for (double q : row) sum += q;
                p *= row[x % 10] / sum;
                x /= 10;
            }
            score[v] = p;
            jitter[v] = r.nextDouble();
        }
        Integer[] idx = new Integer[n];
        for (int v = 0; v < n; v++) idx[v] = v;
        Arrays.sort(idx, (a, b) -> score[a] != score[b] ? Double.compare(score[b], score[a]) : Double.compare(jitter[a], jitter[b]));
        List<String> out = new ArrayList<>();
        for (int i = 0; i < Math.min(k, 8); i++) out.add(String.format(java.util.Locale.US, "%0" + digits + "d", idx[i]));
        return out;
    }

    // ------------------------------------------------------------------ display

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        Game g = o.game;

        // ---- top prize ----
        LinearLayout jp = ui.card(body, T.f("CHANCE OF THE TOP PRIZE · %s", s.lottery.toUpperCase(T.L)), T.t("theoretical"));
        double p = Odds.jackpot(g);
        jp.addView(ui.text(Combi.oneIn(p), 28, Theme.monoBold(a), Theme.LED));
        jp.addView(ui.note(g.digits() ? T.f("%s per bet · 4 cifras: the four digits in order", Combi.percent(p))
                : g.extraMax > 0 ? T.f("%s per ticket · %d numbers from 1–%d plus the superbalota (1–%d)", Combi.percent(p), g.count, g.max, g.extraMax)
                : T.f("%s per ticket · %d numbers from 1–%d", Combi.percent(p), g.count, g.max)));
        double combos = 1 / Predictor.theory(g);
        jp.addView(ui.body(g.digits()
                ? T.f("All %s numbers from 0000 to 9999 have exactly this probability in every draw: 0000 is as likely as 4817, and "
                + "a number that came out yesterday or is \"due\" is no different.", Fmt.num(combos))
                : T.f("All %s combinations have exactly this probability in every draw: 01 02 03 04 05 is as likely as any other. "
                + "What differs is how many people play them, which matters only if a jackpot is shared.", Fmt.num(combos))), ui.matchWrap(8));
        if (g == Game.BALOTO || g == Game.REVANCHA) {
            jp.addView(ui.note(T.t("Revancha is a second draw for the same ticket with the same odds: playing both gives two independent "
                    + "chances, about 1 in 7,700,784 of at least one jackpot.")), ui.matchWrap(6));
        }

        if (g.digits()) prizePlan(a, body, s, o);
        else tiers(a, body, g);

        pack(a, body, s, o);

        // ---- a year of play ----
        LinearLayout yr = ui.card(body, T.t("PLAYING EVERY DRAW FOR A YEAR"), T.t("theoretical"));
        int per = Odds.drawsPerYear(g);
        double year = Odds.perYear(g, p);
        yr.addView(ui.kv(T.t("Draws in a year"), String.valueOf(per), Theme.TEXT, g.scheduleText()));
        yr.addView(ui.kv(T.t("Top prize at least once"), Combi.percent(year), Theme.AMBER_LIGHT,
                T.f("With one ticket per draw. On average that's one win every %s years.", Fmt.num(1 / (p * per)))));
        if (g.digits()) {
            yr.addView(ui.kv(T.t("La pata at least once"), Combi.percent(Odds.perYear(g, 0.01)), Theme.TEXT, T.t("Last two digits, one bet per draw.")));
        } else {
            yr.addView(ui.kv(T.t("Any prize at least once"), Combi.percent(Odds.perYear(g, Odds.anyPrize(g))), Theme.TEXT, null));
        }
    }

    /** El Dorado's official prize plan, with what $1.000 wins in each category. */
    private static void prizePlan(MainActivity a, LinearLayout body, MainActivity.Sel s, Out o) {
        Ui ui = a.ui;
        LinearLayout c = ui.card(body, T.t("EL DORADO PRIZE PLAN"), T.t("official rules"));
        double paid = 1000;
        c.addView(ui.note(T.f("What a bet paid at %s wins, by category. Probability and average return are per bet.", Prizes.cop(paid))));
        Prizes.Bet[] bets = {Prizes.Bet.FOUR, Prizes.Bet.THREE, Prizes.Bet.TWO, Prizes.Bet.ONE, Prizes.Bet.FOUR_ANY, Prizes.Bet.THREE_ANY};
        for (Prizes.Bet b : bets) {
            Prizes.Payout pay = Prizes.payout(b, paid, o.year);
            int[] sample = b.digits == 4 ? new int[]{1, 2, 3, 4} : new int[]{1, 2, 3};
            double pr = b.anyOrder ? Prizes.probability(b, sample) : Math.pow(10, -b.digits);
            String odds = Combi.oneIn(pr) + (b.anyOrder ? " " + T.t("(all digits different)") : "");
            c.addView(ui.kv(b.label(), Prizes.cop(pay.net), b == Prizes.Bet.FOUR ? Theme.AMBER_LIGHT : Theme.TEXT,
                    T.f("%s · × %s · %s Average return %s.", odds, Fmt.num(b.multiplier), b.rule(), Fmt.pct(pr * pay.net / paid))));
        }
        Prizes.Payout four = Prizes.payout(Prizes.Bet.FOUR, paid, o.year);
        LinearLayout how = ui.details(c, T.t("How the prize is worked out"));
        how.addView(ui.kv(T.t("Paid (IVA included)"), Prizes.cop(paid), Theme.TEXT, null));
        how.addView(ui.kv(T.t("Bet value before IVA (19%)"), Prizes.copCents(four.bet), Theme.TEXT, null));
        how.addView(ui.kv(T.t("× 4,500 (4 cifras)"), Prizes.cop(four.gross), Theme.TEXT, T.t("Gross prize")));
        how.addView(ui.kv(T.t("Withholding 20%"), "−" + Prizes.cop(four.withheld), Theme.TEXT,
                T.f("Only on prizes above 48 UVT = %s (UVT %d: %s).", Prizes.cop(Prizes.threshold(o.year)), o.year, Prizes.cop(Prizes.uvt(o.year)))));
        how.addView(ui.kv(T.t("Net prize"), Prizes.cop(four.net), Theme.GREEN, null));
        how.addView(ui.note(T.t("Only the highest category hit is paid. The 4, 3, 2 and 1-digit bets look at the last digits of the "
                + "result: for 4817, la pata is 17 and the last three are 817. Sources: Decreto 1068 de 2015 (chance prize plan) as "
                + "modified by Decreto 1390 de 2024; Estatuto Tributario on withholding; DIAN for the UVT.")), ui.matchWrap(6));
    }

    private static void tiers(MainActivity a, LinearLayout body, Game g) {
        Ui ui = a.ui;
        LinearLayout c = ui.card(body, T.t("EVERY PRIZE TIER"), T.t("theoretical"));
        double price = g == Game.MILOTO ? Prizes.MILOTO_PRICE : Prizes.BALOTO_PRICE;
        c.addView(ui.note(g == Game.MILOTO ? T.f("Ticket %s (IVA included).", Prizes.cop(price))
                : T.f("Ticket %s; Revancha %s more (IVA included).", Prizes.cop(Prizes.BALOTO_PRICE), Prizes.cop(Prizes.REVANCHA_PRICE))));
        for (Odds.Tier t : Odds.tiers(g)) {
            c.addView(ui.kv(t.name, Combi.oneIn(t.p), Theme.AMBER_LIGHT, Combi.percent(t.p) + (t.note.isEmpty() ? "" : " · " + t.note)));
        }
        c.addView(ui.note(T.f("Chance of winning any prize: %s. Prize amounts come from the pool of each draw and aren't fixed, so "
                + "they aren't estimated here.", Combi.oneIn(Odds.anyPrize(g)))), ui.matchWrap(8));
    }

    // ------------------------------------------------------------------ the betting pack

    static String objectiveName(Pack.Objective o) {
        switch (o) {
            case ANY: return T.t("Any prize");
            case BREAK_EVEN: return T.t("Win back the cost");
            default: return T.t("Top prize");
        }
    }

    private static void pack(MainActivity a, LinearLayout body, MainActivity.Sel s, Out o) {
        Ui ui = a.ui;
        Pack.Result r = o.pack;
        LinearLayout c = ui.card(body, T.t("BETTING PACK"), T.t("theoretical"));
        c.addView(ui.note(T.t("The fewest bets, across the next draws of every lottery, that reach the chance you choose.")));
        TextView l1 = ui.label(T.t("Goal"), Theme.DIM);
        l1.setPadding(0, ui.dp(10), 0, 0);
        c.addView(l1);
        LinearLayout objs = ui.chipRow(c, 6);
        for (Pack.Objective ob : Pack.Objective.values()) {
            objs.addView(ui.chip(objectiveName(ob), ob == s.packObjective, v -> {
                a.packObjective = ob;
                a.rerenderInPlace();
            }), ui.wrap(objs.getChildCount() == 0 ? 0 : 6));
        }
        TextView l2 = ui.label(T.t("Chance to reach"), Theme.DIM);
        l2.setPadding(0, ui.dp(10), 0, 0);
        c.addView(l2);
        LinearLayout targets = ui.chipRow(c, 6);
        double[] ts = {0.10, 0.25, 0.50, 0.75, 0.90};
        for (double t : ts) {
            targets.addView(ui.chip(Math.round(t * 100) + "%", Math.abs(t - s.packTarget) < 1e-9, v -> {
                a.packTarget = t;
                a.rerenderInPlace();
            }), ui.wrap(targets.getChildCount() == 0 ? 0 : 6));
        }
        TextView l3 = ui.label(T.t("Each chance bet"), Theme.DIM);
        l3.setPadding(0, ui.dp(10), 0, 0);
        c.addView(l3);
        LinearLayout stakes = ui.chipRow(c, 6);
        double[] ss = {500, 1000, 2000, 5000};
        for (double st : ss) {
            stakes.addView(ui.chip(Prizes.cop(st), Math.abs(st - s.packStake) < 0.5, v -> {
                a.packStake = st;
                a.rerenderInPlace();
            }), ui.wrap(stakes.getChildCount() == 0 ? 0 : 6));
        }
        if (r.items.isEmpty()) {
            c.addView(ui.body(T.t("No upcoming draws found in the next week.")), ui.matchWrap(10));
            return;
        }
        c.addView(ui.divider());
        LinearLayout head = ui.hbox();
        head.setGravity(Gravity.BOTTOM);
        head.addView(ui.text(Combi.percent(r.probability), 28, Theme.monoBold(a), r.reachable ? Theme.LED : Theme.AMBER),
                new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(ui.text(Prizes.cop(r.cost), 20, Theme.monoSemi(a), Theme.TEXT));
        c.addView(head);
        c.addView(ui.note(T.f("%s: %s · %d bets · total cost", objectiveName(r.objective), Combi.oneIn(r.probability), r.bets)));
        if (!r.reachable) {
            TextView w = ui.body(r.objective == Pack.Objective.BREAK_EVEN
                    ? T.f("%s isn't reachable for this goal: more bets cost more than the fixed prizes return, so this is the highest chance of winning back the cost.", Fmt.pct(r.target))
                    : T.f("%s isn't reachable within three chance draws and the next lotto draws; this is the closest.", Fmt.pct(r.target)));
            w.setTextColor(Theme.AMBER);
            c.addView(w, ui.matchWrap(6));
        }
        double bestSingle = 0;
        for (int i = 0; i < r.items.size(); i++) {
            Pack.Item it = r.items.get(i);
            bestSingle = Math.max(bestSingle, it.unitP);
            c.addView(ui.divider());
            String when = Fmt.dayMonth(it.draw.date) + " · " + Fmt.time(it.draw.minutes);
            LinearLayout row = ui.hbox();
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(ui.text(it.draw.game.name, 14.5f, Theme.uiSemi(a), Theme.TEXT),
                    new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(ui.text(Prizes.cop(it.cost), 14, Theme.monoSemi(a), Theme.TEXT));
            c.addView(row);
            String what = it.bet != null
                    ? T.f("%d × %s at %s · each %s · this draw %s", it.count, it.bet.label(), Prizes.cop(it.unitCost), Combi.oneIn(it.unitP), Combi.percent(it.p))
                    : T.f("1 ticket · %s %s", r.objective == Pack.Objective.TOP ? T.t("jackpot") : T.t("any prize"), Combi.oneIn(it.p));
            c.addView(ui.note(when + " · " + what));
            List<String> nums = o.numbers.get(i);
            int[][] ticket = o.tickets.get(i);
            if (ticket != null && ticket[0] != null) {
                c.addView(ui.balls(it.draw.game, ticket[0], ticket[1][0], Ui.NUM_S), ui.matchWrap(6));
            } else if (!nums.isEmpty()) {
                String list = String.join("  ", nums) + (it.count > nums.size() ? "  " + T.f("+ %d more different endings", it.count - nums.size()) : "");
                TextView nv = ui.text(list, 14, Theme.monoMedium(a), Theme.AMBER_LIGHT);
                nv.setPadding(0, ui.dp(4), 0, 0);
                c.addView(nv);
            }
            if (it.bet != null) c.addView(ui.note(T.f("A winning bet pays %s net.", Prizes.cop(it.prizeNet))));
        }
        c.addView(ui.divider());
        c.addView(ui.kv(T.t("Best single bet"), Combi.oneIn(bestSingle), Theme.TEXT, null));
        c.addView(ui.kv(T.t("Whole pack"), Combi.oneIn(r.probability), Theme.AMBER_LIGHT, Combi.percent(r.probability)));
        c.addView(ui.kv(T.t("Total cost"), Prizes.cop(r.cost), Theme.TEXT, null));
        if (r.expectedPrize > 0) {
            c.addView(ui.kv(r.hasLotto ? T.t("Expected chance prizes") : T.t("Expected prizes"), Prizes.cop(r.expectedPrize), Theme.TEXT,
                    r.hasLotto ? T.t("Lotto prizes depend on the pool and aren't included.") : null));
            if (!r.hasLotto) c.addView(ui.kv(T.t("Expected result"), "−" + Prizes.cop(-r.expectedNet), Theme.CORAL,
                    T.f("On average the pack returns %s of its cost.", Fmt.pct(r.expectedPrize / r.cost))));
        }
        LinearLayout how = ui.details(c, T.t("How the pack is worked out"));
        how.addView(ui.note(T.t("Bets on different numbers in the same draw can't both win, so their chances add (3 different patas: "
                + "3 in 100). Different draws are independent, so the pack misses only if every part misses: 1 − (1 − p₁)(1 − p₂)…. "
                + "One bet type per draw keeps that exact, without counting any overlap twice. Revancha uses the same numbers as "
                + "Baloto but is a separate draw. Chance prizes follow the official plan (IVA and withholding included); lotto "
                + "prizes depend on the pool, so lotto tickets don't count towards winning back the cost.")));
        how.addView(ui.note(T.t("The numbers come from each draw's recommended model, only so the list is concrete: every number "
                + "has the same chance. No pack changes the odds of a single bet, nothing is guaranteed, and every pack loses money "
                + "on average.")), ui.matchWrap(6));
    }
}
