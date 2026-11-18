package app.loto.analysis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The probability-optimized betting pack: the fewest bets, across the next draws of every lottery, that reach a
 * chosen probability of a chosen objective, with the pack's combined odds and total cost in pesos.
 *
 * Exact, without double counting:
 * - Within one chance draw, bets on different numbers can't both win, so their probabilities add (k bets on
 *   different pata endings win with probability k/100). One bet type per draw keeps it that way.
 * - Different draws are independent, so the pack wins with probability 1 - Π(1 - p) over the draws.
 * - A lotto ticket is one bet per draw (Baloto, Revancha with the same ticket, MiLoto), from its prize tiers.
 *
 * Objectives: any prize; winning back at least what the pack costs (chance prizes are fixed by law, so this is
 * exact; lotto prizes depend on the pool, so lotto tickets aren't counted towards it); the top prize of each game.
 * No pack changes the odds of any single bet, and every pack has a negative expected return.
 */
final class Pack {
    private Pack() {}

    enum Objective { ANY, BREAK_EVEN, TOP }

    /** One draw that can be bet on, with its date. */
    static final class Draw0 {
        Game game;
        LocalDate date;
        int minutes;
        Draw0(Game g, LocalDate d, int m) {
            game = g;
            date = d;
            minutes = m;
        }
    }

    static final class Item {
        Draw0 draw;
        Prizes.Bet bet;     // null for a lotto ticket
        int count;
        double unitCost, cost;
        /** Probability this item wins (any of its bets). */
        double p;
        /** Chance only: net prize of one winning bet. */
        double prizeNet;
        double unitP;
    }

    static final class Result {
        final List<Item> items = new ArrayList<>();
        int bets;
        double probability, cost;
        boolean reachable;
        /** Chance part: expected net prize and expected net result (prize minus everything paid). */
        double expectedPrize, expectedNet;
        boolean hasLotto;
        double target;
        Objective objective;
    }

    private static final Prizes.Bet[] CHANCE = {Prizes.Bet.ONE, Prizes.Bet.TWO, Prizes.Bet.THREE, Prizes.Bet.FOUR};

    /** P(any prize) of one lotto ticket, from the game's tiers. */
    static double lottoAny(Game g) {
        return Odds.anyPrize(g);
    }

    /**
     * @param chanceDraws the next chance draws (any order; the first is used first)
     * @param baloto the next Baloto draw (Revancha is the same draw night), or null
     * @param miloto the next MiLoto draw, or null
     * @param stake what one chance bet costs (IVA included)
     */
    static Result solve(Objective obj, double target, double stake, List<Draw0> chanceDraws, Draw0 baloto, Draw0 miloto, int year) {
        Result best = null;
        if (obj == Objective.BREAK_EVEN) {
            best = breakEven(target, stake, chanceDraws, year);
        } else {
            // lotto tickets that may be part of the pack (Revancha only with Baloto)
            List<List<Item>> lottoSets = new ArrayList<>();
            lottoSets.add(new ArrayList<>());
            List<Item> b = new ArrayList<>(), br = new ArrayList<>(), m = new ArrayList<>(), bm = new ArrayList<>(), brm = new ArrayList<>();
            if (baloto != null) {
                b.add(lotto(baloto, Game.BALOTO, obj, Prizes.BALOTO_PRICE));
                br.add(lotto(baloto, Game.BALOTO, obj, Prizes.BALOTO_PRICE));
                br.add(lotto(new Draw0(Game.REVANCHA, baloto.date, baloto.minutes), Game.REVANCHA, obj, Prizes.REVANCHA_PRICE));
                lottoSets.add(b);
                lottoSets.add(br);
            }
            if (miloto != null) {
                m.add(lotto(miloto, Game.MILOTO, obj, Prizes.MILOTO_PRICE));
                lottoSets.add(m);
                if (baloto != null) {
                    bm.addAll(b);
                    bm.addAll(m);
                    brm.addAll(br);
                    brm.addAll(m);
                    lottoSets.add(bm);
                    lottoSets.add(brm);
                }
            }
            Prizes.Bet[] formats = obj == Objective.TOP ? new Prizes.Bet[]{Prizes.Bet.FOUR} : CHANCE;
            for (List<Item> lot : lottoSets) {
                double miss = 1;
                for (Item it : lot) miss *= 1 - it.p;
                // chance-free pack
                Result r0 = assemble(obj, target, lot, null, 0, 0, null, stake, year);
                best = better(best, r0);
                double need = 1 - (1 - target) / miss; // probability the chance part must reach
                if (need <= 0) continue;
                for (Prizes.Bet f : formats) {
                    double p = Math.pow(10, -f.digits);
                    int cap = (int) Math.round(1 / p) - 1; // never every outcome: that would be a certain loss, not a bet
                    for (int r = 1; r <= Math.min(3, chanceDraws.size()); r++) {
                        // r draws with k bets each: chance part wins with 1 - (1 - kp)^r
                        double perDraw = 1 - Math.pow(1 - need, 1.0 / r);
                        int k = (int) Math.ceil(perDraw / p - 1e-9);
                        if (k < 1) k = 1;
                        if (k > cap) continue;
                        best = better(best, assemble(obj, target, lot, f, k, r, chanceDraws, stake, year));
                    }
                }
            }
        }
        if (best == null) best = new Result();
        best.target = target;
        best.objective = obj;
        return best;
    }

    private static Item lotto(Draw0 d, Game g, Objective obj, double price) {
        Item it = new Item();
        it.draw = new Draw0(g, d.date, d.minutes);
        it.count = 1;
        it.unitCost = price;
        it.cost = price;
        it.p = obj == Objective.TOP ? Predictor.theory(g) : lottoAny(g);
        it.unitP = it.p;
        return it;
    }

    private static Result assemble(Objective obj, double target, List<Item> lot, Prizes.Bet f, int k, int r,
                                   List<Draw0> chanceDraws, double stake, int year) {
        Result res = new Result();
        double miss = 1;
        for (Item it : lot) {
            res.items.add(it);
            res.bets += it.count;
            res.cost += it.cost;
            miss *= 1 - it.p;
            res.hasLotto = true;
        }
        if (f != null) {
            double p = Math.pow(10, -f.digits);
            Prizes.Payout pay = Prizes.payout(f, stake, year);
            for (int i = 0; i < r; i++) {
                Item it = new Item();
                it.draw = chanceDraws.get(i);
                it.bet = f;
                it.count = k;
                it.unitCost = stake;
                it.cost = k * stake;
                it.unitP = p;
                it.p = k * p;
                it.prizeNet = pay.net;
                res.items.add(it);
                res.bets += k;
                res.cost += it.cost;
                miss *= 1 - it.p;
                res.expectedPrize += it.p * pay.net;
            }
        }
        res.probability = 1 - miss;
        res.reachable = res.probability >= target - 1e-12;
        res.expectedNet = res.expectedPrize - res.cost;
        return res;
    }

    /** Fewer bets wins; then lower cost; then higher probability. Unreachable packs only stand in if nothing reaches. */
    private static Result better(Result a, Result b) {
        if (b == null || b.items.isEmpty()) return a;
        if (a == null) return b;
        if (a.reachable != b.reachable) return a.reachable ? a : b;
        if (!a.reachable) return b.probability > a.probability ? b : a;
        if (a.bets != b.bets) return a.bets < b.bets ? a : b;
        if (Math.abs(a.cost - b.cost) > 0.5) return a.cost < b.cost ? a : b;
        return b.probability > a.probability ? b : a;
    }

    /**
     * Winning back at least what the pack costs, chance only (fixed prizes): r draws with k bets of one format each;
     * the pack breaks even when enough draws win, w * prize >= cost, which is a binomial sum over the r draws.
     */
    private static Result breakEven(double target, double stake, List<Draw0> draws, int year) {
        Result best = null;
        for (Prizes.Bet f : CHANCE) {
            double p = Math.pow(10, -f.digits);
            Prizes.Payout pay = Prizes.payout(f, stake, year);
            int cap = (int) Math.round(1 / p) - 1;
            for (int r = 1; r <= Math.min(3, draws.size()); r++) {
                for (int k = 1; k <= cap; k++) {
                    double cost = r * k * stake, q = k * p;
                    int need = (int) Math.ceil(cost / pay.net - 1e-9);
                    if (need > r) break; // more bets per draw only make it harder
                    double prob = 0;
                    for (int w = Math.max(need, 0); w <= r; w++) prob += Combi.choose(r, w) * Math.pow(q, w) * Math.pow(1 - q, r - w);
                    Result res = new Result();
                    for (int i = 0; i < r; i++) {
                        Item it = new Item();
                        it.draw = draws.get(i);
                        it.bet = f;
                        it.count = k;
                        it.unitCost = stake;
                        it.cost = k * stake;
                        it.unitP = p;
                        it.p = q;
                        it.prizeNet = pay.net;
                        res.items.add(it);
                        res.expectedPrize += q * pay.net;
                    }
                    res.bets = r * k;
                    res.cost = cost;
                    res.probability = prob;
                    res.reachable = prob >= target - 1e-12;
                    res.expectedNet = res.expectedPrize - cost;
                    best = better(best, res);
                    if (res.reachable) break; // the smallest k for this format and r
                }
            }
        }
        return best;
    }
}
