package app.loto.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/** Checks the mathematics against known values and simulated fair (and deliberately unfair) draws. */
public class EngineTest {

    // ------------------------------------------------------------ helpers

    private static History simulate(Game g, int draws, long seed, double biasDigit7) {
        History h = new History();
        Random r = new Random(seed);
        LocalDate d = LocalDate.of(2020, 1, 1);
        for (int i = 0; i < draws; i++) {
            int[] n;
            int extra;
            if (g.digits()) {
                n = new int[4];
                for (int p = 0; p < 4; p++) n[p] = r.nextInt(10);
                if (biasDigit7 > 0 && r.nextDouble() < biasDigit7) n[0] = 7;
                extra = r.nextInt(10);
            } else {
                List<Integer> pool = new ArrayList<>();
                for (int v = 1; v <= g.max; v++) pool.add(v);
                Collections.shuffle(pool, r);
                n = new int[g.count];
                for (int k = 0; k < g.count; k++) n[k] = pool.get(k);
                Arrays.sort(n);
                extra = g.extraMax > 0 ? 1 + r.nextInt(g.extraMax) : -1;
            }
            h.put(new Draw(g, d, "", n, extra));
            d = d.plusDays(1);
        }
        return h;
    }

    private static Sample sample(History h, Game g) {
        List<Game> one = new ArrayList<>();
        one.add(g);
        return h.sample(one, 0);
    }

    // ------------------------------------------------------------ odds

    @Test
    public void balotoOdds() {
        assertEquals(962598, Combi.choose(43, 5), 0);
        assertEquals(15401568, Game.BALOTO.combinations(), 0.5);
        assertEquals("1 in 15,401,568", Combi.oneIn(Odds.jackpot(Game.BALOTO)));
        double total = 0;
        for (Odds.Tier t : Odds.tiers(Game.BALOTO)) total += t.p;
        // every tier except "2, 1 or 0 numbers without the superbalota"
        double noPrize = (84360 + 369075 + 501942) * 15.0 / 15401568;
        assertEquals(1 - noPrize, total, 1e-12);
    }

    @Test
    public void milotoOdds() {
        assertEquals(575757, Combi.choose(39, 5), 0);
        List<Odds.Tier> t = Odds.tiers(Game.MILOTO);
        assertEquals(1 / 575757.0, t.get(0).p, 1e-15);
        assertEquals(170 / 575757.0, t.get(1).p, 1e-15);
        assertEquals(5610 / 575757.0, t.get(2).p, 1e-15);
        assertEquals(59840 / 575757.0, t.get(3).p, 1e-15);
    }

    @Test
    public void chanceOdds() {
        assertEquals("1 in 10,000", Combi.oneIn(Odds.jackpot(Game.DORADO_MANANA)));
        assertEquals(24, Odds.permutations(new int[]{1, 2, 3, 4}));
        assertEquals(12, Odds.permutations(new int[]{1, 1, 2, 3}));
        assertEquals(6, Odds.permutations(new int[]{1, 1, 2, 2}));
        assertEquals(4, Odds.permutations(new int[]{1, 1, 1, 2}));
        assertEquals(1, Odds.permutations(new int[]{7, 7, 7, 7}));
    }

    // ------------------------------------------------------------ distributions

    @Test
    public void distributions() {
        assertEquals(0.05, Combi.chiSquareP(3.841, 1), 1e-3);
        assertEquals(0.05, Combi.chiSquareP(16.919, 9), 1e-3);
        assertEquals(0.01, Combi.chiSquareP(63.691, 40), 1e-3);
        assertEquals(0.975, Combi.normalCdf(1.96), 1e-4);
        assertEquals(0.05, Combi.zTwoSided(1.96), 1e-4);
        assertEquals(1 - Math.exp(-2) * (1 + 2 + 2), Combi.poissonUpper(2, 3), 1e-9);
        double[] adj = Combi.holm(new double[]{0.01, 0.04, 0.03});
        assertEquals(0.03, adj[0], 1e-12);
        assertEquals(0.06, adj[2], 1e-12);
        assertEquals(0.06, adj[1], 1e-12);
    }

    @Test
    public void sumDistributions() {
        double[] p = Analysis.sumProbabilities(Game.BALOTO);
        double total = 0, mean = 0;
        for (int s = 0; s < p.length; s++) {
            total += p[s];
            mean += s * p[s];
        }
        assertEquals(1, total, 1e-9);
        assertEquals(5 * 44 / 2.0, mean, 1e-9);
        double[] d = Analysis.sumProbabilities(Game.DORADO_MANANA);
        assertEquals(37, d.length);
        assertEquals(1e-4, d[0], 1e-12);
        assertEquals(1e-4, d[36], 1e-12);
        double sumPat = 0;
        for (double v : Analysis.PATTERN_P) sumPat += v;
        assertEquals(1, sumPat, 1e-12);
    }

    @Test
    public void runs() {
        double atLeastTwo = 1 - Combi.choose(39, 5) / Combi.choose(43, 5);
        assertEquals(atLeastTwo, Randomness.runAtLeast(43, 5, 2), 1e-12);
        assertEquals(39 / Combi.choose(43, 5), Randomness.runAtLeast(43, 5, 5), 1e-15);
    }

    // ------------------------------------------------------------ calendar

    @Test
    public void colombianHolidays() {
        assertEquals(LocalDate.of(2026, 4, 5), Holidays.easter(2026));
        assertTrue(Holidays.isHoliday(LocalDate.of(2026, 4, 3)));   // Good Friday
        assertTrue(Holidays.isHoliday(LocalDate.of(2026, 11, 2)));  // All Saints, moved to Monday
        assertTrue(Holidays.isHoliday(LocalDate.of(2026, 11, 16))); // Cartagena, moved to Monday
        assertFalse(Holidays.isHoliday(LocalDate.of(2026, 11, 17)));
        assertNull(Game.DORADO_MANANA.timeOn(LocalDate.of(2026, 11, 16)));
        assertEquals("11:00 a.m.", Game.DORADO_MANANA.timeOn(LocalDate.of(2026, 11, 17)));
        assertEquals("2:00 p.m.", Game.PAISITA_DIA.timeOn(LocalDate.of(2026, 11, 16)));
        assertEquals(2, Game.DORADO_MANANA.longestRegularGap());
        assertEquals(3, Game.BALOTO.longestRegularGap());
        assertEquals("Mon, Wed, Sat 11:00 p.m.", Game.BALOTO.scheduleText());
    }

    // ------------------------------------------------------------ data

    @Test
    public void csvRoundTrip() {
        History h = new History();
        h.addCsv(Game.BALOTO, "date,draw,numbers,extra\n2026-11-16,2736,02 12 19 25 42,07\n2026-11-14,2735,24 28 38 41 42,05\nbad,row\n");
        h.addCsv(Game.DORADO_MANANA, "date,draw,numbers,extra\n2026-11-17,,2820,2\n2026-11-14,,5415,\n2026-11-13,,065,1\n");
        assertEquals(2, h.size(Game.BALOTO));
        assertEquals(2, h.size(Game.DORADO_MANANA));
        assertEquals(1, h.rejected.size());
        assertEquals(LocalDate.of(2026, 11, 16), h.last(Game.BALOTO));
        String csv = h.toCsv(Game.BALOTO);
        assertTrue(csv.contains("2026-11-16,2736,02 12 19 25 42,07"));
        // a later source adds the quinta to a draw stored without it
        assertTrue(h.addCsv(Game.DORADO_MANANA, "2026-11-14,,5415,0\n") == 1);
        assertEquals(0, h.on(Game.DORADO_MANANA, LocalDate.of(2026, 11, 14)).extra);
        Draw d = h.on(Game.DORADO_MANANA, LocalDate.of(2026, 11, 17));
        assertEquals(20, d.lastTwo());
        assertEquals(820, d.lastThree());
        assertEquals(1, Analysis.pattern(d)); // 2820: one pair
    }

    // ------------------------------------------------------------ randomness and integrity

    /** On fair simulated draws each test should come out below 0.05 only about 5% of the time. */
    @Test
    public void testsAreCalibratedOnFairDraws() {
        for (Game g : new Game[]{Game.BALOTO, Game.DORADO_MANANA}) {
            int runs = 60, flags = 0, tests = 0, scoreSum = 0;
            for (int i = 0; i < runs; i++) {
                Randomness.Report r = Randomness.run(sample(simulate(g, 600, 1000 + i, 0), g));
                for (TestResult t : r.tests) {
                    if (!t.enough) continue;
                    tests++;
                    if (t.p < 0.05) flags++;
                }
                scoreSum += r.score;
            }
            double rate = flags / (double) tests;
            assertTrue(g.name + " false alarm rate " + rate, rate > 0.01 && rate < 0.10);
            assertTrue(g.name + " average score " + scoreSum / runs, scoreSum / runs >= 90);
        }
    }

    @Test
    public void biasIsDetectedAndReplicated() {
        History h = simulate(Game.DORADO_MANANA, 1200, 7, 0.08); // 7 forced into the first digit 8% of the time
        Randomness.Report r = Randomness.run(sample(h, Game.DORADO_MANANA));
        TestResult freq = null;
        for (TestResult t : r.tests) if (t.key.equals("freq")) freq = t;
        assertNotNull(freq);
        assertTrue(freq.pAdj < 0.01);
        assertTrue(r.score < 100);
        Integrity.Report ir = Integrity.run(h, "Dorado", 0);
        assertEquals(Integrity.Level.PERSISTENT, ir.overall);
    }

    @Test
    public void fairDrawsUsuallyLookClean() {
        int clean = 0;
        for (int i = 0; i < 10; i++) {
            Integrity.Report ir = Integrity.run(simulate(Game.BALOTO, 500, 300 + i, 0), "Baloto", 0);
            if (ir.overall == Integrity.Level.CLEAR) clean++;
        }
        assertTrue("clean runs " + clean, clean >= 8);
    }

    @Test
    public void missingDaysFollowTheLocalSchedule() {
        History h = new History();
        Random r = new Random(3);
        // daily for 120 days, then weekends only for 120 days (a schedule change, not missing data),
        // with one real two-week hole in the daily part
        for (LocalDate d = LocalDate.of(2024, 1, 1); d.isBefore(LocalDate.of(2024, 9, 1)); d = d.plusDays(1)) {
            boolean daily = d.isBefore(LocalDate.of(2024, 5, 1));
            boolean weekend = d.getDayOfWeek().getValue() >= 6;
            boolean hole = !d.isBefore(LocalDate.of(2024, 3, 4)) && d.isBefore(LocalDate.of(2024, 3, 18));
            if ((daily || weekend) && !hole) {
                h.put(new Draw(Game.DORADO_NOCHE, d, "", new int[]{r.nextInt(10), r.nextInt(10), r.nextInt(10), r.nextInt(10)}, -1));
            }
        }
        List<String> m = Integrity.missingStretches(sample(h, Game.DORADO_NOCHE));
        assertEquals(m.toString(), 1, m.size());
        assertTrue(m.get(0), m.get(0).startsWith("4 Mar 2024"));
    }

    @Test
    public void drawScores() {
        History h = new History();
        h.addCsv(Game.DORADO_MANANA, "2026-11-10,,7777,\n2026-11-11,,1593,\n2026-11-12,,1234,\n");
        List<Randomness.DrawScore> s = Randomness.drawScores(sample(h, Game.DORADO_MANANA));
        assertTrue(s.get(0).score < 2);   // 7777: 1 in 1,000
        assertTrue(s.get(1).score >= 50); // an ordinary number
        assertTrue(s.get(2).score < 2);   // a straight run
    }

    // ------------------------------------------------------------ predictions

    @Test
    public void predictionsAreConsistent() {
        History h = simulate(Game.BALOTO, 400, 11, 0);
        Sample s = sample(h, Game.BALOTO);
        Predictor.Result hot = Predictor.suggest(Predictor.Model.FREQUENCY, s, 1);
        Analysis.Freq f = Analysis.frequency(s);
        List<Analysis.Cell> top = f.hot(5);
        double minTop = Double.MAX_VALUE;
        for (Analysis.Cell c : top) minTop = Math.min(minTop, c.observed);
        for (int v : hot.top.nums) assertTrue(f.values.get(v - 1).observed >= minTop);
        assertEquals(1 / 15401568.0, hot.top.theoryP, 1e-18);
        assertTrue(hot.top.modelP >= hot.top.theoryP);
        Predictor.Result q = Predictor.suggest(Predictor.Model.RANDOM, s, 2);
        assertEquals(q.top.theoryP, q.top.modelP, 0);
        Predictor.Result bal = Predictor.suggest(Predictor.Model.BALANCED, s, 3);
        assertTrue(Predictor.balanced(Game.BALOTO, bal.top.nums));
        Predictor.Result un = Predictor.suggest(Predictor.Model.UNPOPULAR, s, 4);
        assertTrue(Predictor.unpopular(Game.BALOTO, un.top.nums));
        // uniform weights give every combination the theoretical probability
        Predictor.Weights w = Predictor.weights(Predictor.Model.RANDOM, s);
        assertEquals(1 / 15401568.0, Predictor.probability(Game.BALOTO, w, new int[]{1, 2, 3, 4, 5}, 1), 1e-18);
        Predictor.Backtest b = Predictor.backtest(Predictor.Model.FREQUENCY, s, 5);
        assertEquals(100, b.draws);
        assertTrue(b.p > 0.001); // fair data: no model should beat chance convincingly
    }

    @Test
    public void bayesStaysFairOnFairData() {
        Sample s = sample(simulate(Game.MILOTO, 300, 21, 0), Game.MILOTO);
        Predictor.Weights w = Predictor.weights(Predictor.Model.BAYES, s);
        double min = Double.MAX_VALUE, max = 0;
        for (int v = 1; v <= 39; v++) {
            min = Math.min(min, w.w[0][v]);
            max = Math.max(max, w.w[0][v]);
        }
        assertTrue(max / min < 1.6);
    }

    @Test
    public void checkCombination() {
        Sample s = sample(simulate(Game.BALOTO, 200, 31, 0), Game.BALOTO);
        Odds.Check c = Odds.check(Game.BALOTO, s, "5 13 19 25 29 + 7");
        assertNull(c.error);
        assertEquals(1 / 15401568.0, c.theory, 1e-18);
        assertNotNull(Odds.check(Game.BALOTO, s, "5 5 19 25 29").error);
        Odds.Check d = Odds.check(Game.DORADO_MANANA, sample(simulate(Game.DORADO_MANANA, 50, 1, 0), Game.DORADO_MANANA), "2820");
        assertNull(d.error);
        assertEquals(1e-4, d.theory, 1e-15);
    }
}
