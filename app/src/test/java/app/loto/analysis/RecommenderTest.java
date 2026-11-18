package app.loto.analysis;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/** The daily recommendation: reproducible, honest on fair draws, and able to find a real bias when there is one. */
public class RecommenderTest {

    private static Sample dorado(int draws, long seed, double bias7) {
        Random r = new Random(seed);
        List<Draw> list = new ArrayList<>();
        LocalDate d = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < draws; i++) {
            int[] n = new int[4];
            for (int p = 0; p < 4; p++) n[p] = r.nextInt(10);
            if (r.nextDouble() < bias7) n[0] = 7;
            list.add(new Draw(Game.DORADO_MANANA, d, "", n, r.nextInt(10)));
            d = d.plusDays(1);
        }
        List<Game> one = new ArrayList<>();
        one.add(Game.DORADO_MANANA);
        return new Sample(one, list);
    }

    private static Sample baloto(int draws, long seed) {
        Random r = new Random(seed);
        List<Draw> list = new ArrayList<>();
        LocalDate d = LocalDate.of(2022, 1, 1);
        for (int i = 0; i < draws; i++) {
            list.add(Recommender.fair(Game.BALOTO, r));
            Draw x = list.get(list.size() - 1);
            list.set(list.size() - 1, new Draw(Game.BALOTO, d, "", x.nums, x.extra));
            d = d.plusDays(3);
        }
        List<Game> one = new ArrayList<>();
        one.add(Game.BALOTO);
        return new Sample(one, list);
    }

    @Test
    public void sameDataGivesTheSameRecommendation() {
        Sample s = dorado(400, 1, 0);
        LocalDate target = LocalDate.of(2025, 2, 5);
        Recommender.Rec a = Recommender.recommend(s, target, true), b = Recommender.recommend(s, target, true);
        assertEquals(a.model, b.model);
        assertArrayEquals(a.nums, b.nums);
        assertEquals(a.mcP, b.mcP, 0);
        assertEquals(Models.RANKED.length, a.ranking.size());
        for (int i = 1; i < a.ranking.size(); i++) assertTrue(a.ranking.get(i - 1).mean >= a.ranking.get(i).mean);
        assertEquals(a.ranking.get(0).model, a.model);
        assertEquals(1e-4, a.theoryP, 1e-15);
    }

    @Test
    public void fairDrawsShowNoAdvantage() {
        for (long seed = 1; seed <= 3; seed++) {
            Recommender.Rec r = Recommender.recommend(dorado(400, seed, 0), LocalDate.of(2025, 2, 5), true);
            assertFalse("seed " + seed + " mc p " + r.mcP, r.advantage);
            assertTrue(r.mcP > 0 && r.mcP <= 1);
        }
        Recommender.Rec b = Recommender.recommend(baloto(300, 9), LocalDate.of(2024, 6, 1), true);
        assertFalse(b.advantage);
        assertEquals(5, b.nums.length);
        assertTrue(b.extra >= 1 && b.extra <= 16);
        assertEquals(1 / 15401568.0, b.theoryP, 1e-18);
    }

    @Test
    public void aRealBiasIsFoundAndUsed() {
        // the first digit is 7 in about 55% of draws: a forecaster that learns it must score above a fair guess
        Recommender.Rec r = Recommender.recommend(dorado(400, 5, 0.5), LocalDate.of(2025, 2, 5), true);
        assertTrue(r.advantage);
        assertEquals(7, r.nums[0]);
        assertTrue(r.ranking.get(0).lo > 0);
        assertTrue(r.mcP < 0.05);
        assertTrue(r.modelP > r.theoryP);
    }

    @Test
    public void trackRecordReplaysEveryDrawAfterTheWarmUp() {
        Sample s = dorado(300, 2, 0);
        Recommender.Track t = Recommender.track(s);
        assertEquals(300 - Recommender.WARMUP, t.best.size());
        for (Predictor.Model m : Models.RANKED) assertEquals(300 - Recommender.WARMUP, t.perModel.get(m).size());
        Recommender.Stats st = Recommender.stats(Game.DORADO_MANANA, t.best, null);
        assertEquals(270, st.n);
        assertEquals(2.7, st.expLast2, 1e-9);
        assertEquals(27, st.expLast1, 1e-9);
        // a recommendation made before a draw never sees it: outcomes match a recount from the raw draws
        int last2 = 0;
        for (Recommender.Outcome o : t.best) if (o.nums[2] == o.actual.nums[2] && o.nums[3] == o.actual.nums[3]) last2++;
        assertEquals(last2, st.last2);
        Recommender.Stats recent = Recommender.stats(Game.DORADO_MANANA, t.best, s.to().minusDays(29));
        assertEquals(30, recent.n);
    }

    @Test
    public void wilsonIntervalCoversTheRate() {
        double[] ci = Recommender.wilson(15, 1125);
        assertTrue(ci[0] < 15 / 1125.0 && 15 / 1125.0 < ci[1]);
        assertEquals(0.0081, ci[0], 0.0005);
        assertEquals(0.0219, ci[1], 0.0005);
        assertArrayEquals(new double[]{0, 0}, Recommender.wilson(0, 0), 0);
    }
}
