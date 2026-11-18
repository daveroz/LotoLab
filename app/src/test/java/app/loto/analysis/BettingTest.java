package app.loto.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/** El Dorado's official prize plan and the betting pack, against figures worked out by hand. */
public class BettingTest {

    private static Draw result(String digits) {
        int[] n = new int[4];
        for (int i = 0; i < 4; i++) n[i] = digits.charAt(i) - '0';
        return new Draw(Game.DORADO_MANANA, LocalDate.of(2026, 11, 17), "", n, -1);
    }

    @Test
    public void fourDigitPrizeFollowsThePlan() {
        // $1.000 paid = $840,34 bet + IVA; × 4.500 = $3.781.513 gross; above 48 UVT, so 20% withheld
        Prizes.Payout p = Prizes.payout(Prizes.Bet.FOUR, 1000, 2026);
        assertEquals(840.336, p.bet, 0.001);
        assertEquals(159.664, p.iva, 0.001);
        assertEquals(3781512.6, p.gross, 0.1);
        assertTrue(p.taxed);
        assertEquals(756302.5, p.withheld, 0.1);
        assertEquals(3025210.1, p.net, 0.1);
        assertEquals(2513952, Prizes.threshold(2026), 0);
    }

    @Test
    public void smallPrizesAreNotWithheld() {
        Prizes.Payout pata = Prizes.payout(Prizes.Bet.TWO, 1000, 2026);
        assertFalse(pata.taxed);
        assertEquals(42016.8, pata.net, 0.1);
        assertEquals(336134.5, Prizes.payout(Prizes.Bet.THREE, 1000, 2026).net, 0.1);
        assertEquals(4201.7, Prizes.payout(Prizes.Bet.ONE, 1000, 2026).net, 0.1);
        assertEquals(174789.9, Prizes.payout(Prizes.Bet.FOUR_ANY, 1000, 2026).net, 0.1);
        assertEquals(69747.9, Prizes.payout(Prizes.Bet.THREE_ANY, 1000, 2026).net, 0.1);
        // the threshold moves with the UVT: $2.000 on 3 cifras is $672.269 gross, still below 48 UVT
        assertFalse(Prizes.payout(Prizes.Bet.THREE, 2000, 2026).taxed);
        assertTrue(Prizes.payout(Prizes.Bet.THREE, 10000, 2026).taxed);
    }

    @Test
    public void betsLookAtTheLastDigits() {
        Draw d = result("4817");
        assertTrue(Prizes.wins(Prizes.Bet.FOUR, new int[]{4, 8, 1, 7}, d));
        assertTrue(Prizes.wins(Prizes.Bet.THREE, new int[]{8, 1, 7}, d));
        assertTrue(Prizes.wins(Prizes.Bet.TWO, new int[]{1, 7}, d));
        assertTrue(Prizes.wins(Prizes.Bet.ONE, new int[]{7}, d));
        assertFalse(Prizes.wins(Prizes.Bet.TWO, new int[]{7, 1}, d));
        assertTrue(Prizes.wins(Prizes.Bet.FOUR_ANY, new int[]{7, 1, 8, 4}, d));
        assertTrue(Prizes.wins(Prizes.Bet.THREE_ANY, new int[]{7, 8, 1}, d));
        assertFalse(Prizes.wins(Prizes.Bet.THREE_ANY, new int[]{4, 8, 1}, d));
    }

    @Test
    public void anyOrderOddsCountTheDistinctOrders() {
        assertEquals(24, Prizes.orders(new int[]{1, 2, 3, 4}));
        assertEquals(12, Prizes.orders(new int[]{1, 1, 2, 3}));
        assertEquals(6, Prizes.orders(new int[]{1, 1, 2, 2}));
        assertEquals(4, Prizes.orders(new int[]{1, 1, 1, 2}));
        assertEquals(1, Prizes.orders(new int[]{7, 7, 7, 7}));
        assertEquals(6, Prizes.orders(new int[]{1, 2, 3}));
        assertEquals(24e-4, Prizes.probability(Prizes.Bet.FOUR_ANY, new int[]{1, 2, 3, 4}), 1e-12);
        assertEquals(1e-2, Prizes.probability(Prizes.Bet.TWO, new int[]{4, 2}), 1e-12);
        // every bet returns less than it costs on average
        for (Prizes.Bet b : Prizes.Bet.values()) {
            int[] digits = b.digits == 4 ? new int[]{1, 2, 3, 4} : b.digits == 3 ? new int[]{1, 2, 3} : b.digits == 2 ? new int[]{1, 2} : new int[]{1};
            assertTrue(b.name(), Prizes.expectedNet(b, digits, 1000, 2026) < 0);
        }
    }

    // ------------------------------------------------------------ pack

    private static List<Pack.Draw0> chance() {
        LocalDate d = LocalDate.of(2026, 11, 19);
        List<Pack.Draw0> l = new ArrayList<>();
        l.add(new Pack.Draw0(Game.DORADO_MANANA, d, 11 * 60));
        l.add(new Pack.Draw0(Game.DORADO_TARDE, d, 15 * 60 + 30));
        l.add(new Pack.Draw0(Game.DORADO_NOCHE, d, 22 * 60 + 30));
        return l;
    }

    private static double recombine(Pack.Result r) {
        double miss = 1;
        for (Pack.Item it : r.items) miss *= 1 - it.p;
        return 1 - miss;
    }

    @Test
    public void halfAChanceOfAnyPrizeIsFiveUnas() {
        Pack.Result r = Pack.solve(Pack.Objective.ANY, 0.5, 1000, chance(), null, null, 2026);
        assertTrue(r.reachable);
        assertEquals(5, r.bets);
        assertEquals(5000, r.cost, 0.01);
        assertEquals(0.5, r.probability, 1e-12);
        assertEquals(1, r.items.size());
        assertEquals(Prizes.Bet.ONE, r.items.get(0).bet);
        // five different last digits in one draw can't both win: 5 × 1/10, no double counting
        assertEquals(0.5, r.items.get(0).p, 1e-12);
        assertTrue(r.expectedNet < 0);
    }

    @Test
    public void packProbabilityIsExactAcrossDraws() {
        LocalDate d = LocalDate.of(2026, 11, 21);
        Pack.Draw0 baloto = new Pack.Draw0(Game.BALOTO, d, 23 * 60), miloto = new Pack.Draw0(Game.MILOTO, d, 22 * 60);
        for (Pack.Objective o : Pack.Objective.values()) {
            for (double t : new double[]{0.1, 0.25, 0.5, 0.75, 0.9}) {
                Pack.Result r = Pack.solve(o, t, 1000, chance(), baloto, miloto, 2026);
                assertFalse(r.items.isEmpty());
                if (o != Pack.Objective.BREAK_EVEN) assertEquals(o + " " + t, recombine(r), r.probability, 1e-12);
                double cost = 0;
                int bets = 0;
                for (Pack.Item it : r.items) {
                    cost += it.cost;
                    bets += it.count;
                    assertTrue(it.p <= 1);
                }
                assertEquals(cost, r.cost, 0.01);
                assertEquals(bets, r.bets);
                if (r.reachable) assertTrue(r.probability >= t - 1e-12);
            }
        }
    }

    @Test
    public void winningBackTheCostHasACeiling() {
        // fixed prizes return about 42% of the stake at best, so a high chance of breaking even can't be bought
        Pack.Result r = Pack.solve(Pack.Objective.BREAK_EVEN, 0.5, 1000, chance(), null, null, 2026);
        assertFalse(r.reachable);
        assertTrue(r.probability > 0.3 && r.probability < 0.5);
        Pack.Result low = Pack.solve(Pack.Objective.BREAK_EVEN, 0.1, 1000, chance(), null, null, 2026);
        assertTrue(low.reachable);
    }

    @Test
    public void topPrizePackUsesFourDigitBets() {
        Pack.Result r = Pack.solve(Pack.Objective.TOP, 0.1, 1000, chance(), null, null, 2026);
        assertTrue(r.reachable);
        for (Pack.Item it : r.items) assertEquals(Prizes.Bet.FOUR, it.bet);
        assertEquals(1000, r.bets); // 1,000 different 4-digit numbers in one draw: exactly 10%
    }
}
