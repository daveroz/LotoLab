package app.loto.analysis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The daily recommendation, and how good recommending has really been.
 *
 * Ranking: every forecasting model (hot, recent, overdue, Bayesian, ensemble) is scored on each past draw with a
 * proper scoring rule, the log score of what it forecast for the result that came, measured against a fair draw
 * and using only earlier results. A fair forecast scores 0 on average; only a model that genuinely sees something
 * coming scores above 0. Models are ranked by their mean score over the last 200 draws, with a 95% confidence
 * interval. Because the best of five is chosen, its advantage is also tested by Monte Carlo: the same ranking is
 * run on simulated fair draws, and the p-value is how often a fair lottery produces a best model at least as good.
 *
 * Recommendation: the top-ranked model's single most probable combination for the next draw, built from all
 * results so far; exact ties are broken by the target date, so it is reproducible and changes only as data does.
 *
 * Track record: a walk-forward replay recomputes, for every past draw, the recommendation the app would have made
 * from the results before it, and compares it with what came, by draw, by model and by period, against random
 * selection. A daily change of recommendation never means a change of probability: every combination keeps the
 * same theoretical chance.
 */
final class Recommender {
    private Recommender() {}

    static final int WARMUP = 30;

    static final class Rank {
        Predictor.Model model;
        double mean, se, lo, hi;
        int n;
        int[] nums;
        int extra = -1;
        double modelP;
    }

    static final class Rec {
        Game game;
        LocalDate target, dataUntil;
        Predictor.Model model;
        int[] nums;
        int extra = -1;
        double modelP, theoryP;
        final List<Rank> ranking = new ArrayList<>();
        /** Monte Carlo p-value of the best model's score; NaN when there are too few draws. */
        double mcP = Double.NaN;
        int draws;
        /** Whether a real advantage over chance is shown (best model's score significantly above 0 and Monte Carlo p < 0.05). */
        boolean advantage;
    }

    /** One past recommendation and what came. */
    static final class Outcome {
        LocalDate date;
        Predictor.Model model;
        int[] nums;
        int extra = -1;
        Draw actual;
        int matched;          // digits in the right place (chance) or numbers shared (lotto)
        boolean exact, last3, last2, last1, extraHit;
    }

    static final class Track {
        Game game;
        final List<Outcome> best = new ArrayList<>();
        final Map<Predictor.Model, List<Outcome>> perModel = new EnumMap<>(Predictor.Model.class);
    }

    /** Hit counts of a set of recommendations against random selection. */
    static final class Stats {
        int n;
        int exact, last3, last2, last1, extraHits, two, three;
        double matched;
        double pExact, pLast3, pLast2, pLast1, pTwo, pMatched;
        double expExact, expLast3, expLast2, expLast1, expTwo, expMatched;
        double[] ciLast2 = {0, 0}, ciTwo = {0, 0};
        int extraN;
        /** The headline comparison: two-digit endings (chance) or average numbers matched (lotto); p of beating random. */
        double primaryP = Double.NaN;
    }

    // ------------------------------------------------------------------ ranking

    private static double[] meanSe(List<double[]> scores, int col, int from) {
        int n = scores.size() - from;
        if (n <= 1) return new double[]{0, Double.NaN, Math.max(0, n)};
        double m = 0;
        for (int i = from; i < scores.size(); i++) m += scores.get(i)[col];
        m /= n;
        double v = 0;
        for (int i = from; i < scores.size(); i++) v += (scores.get(i)[col] - m) * (scores.get(i)[col] - m);
        v /= (n - 1);
        return new double[]{m, Math.sqrt(v / n), n};
    }

    /** Column of a ranked model in the score rows recorded by {@link Models}. */
    private static int col(Predictor.Model m) {
        for (int j = 0; j < Models.BASE.length; j++) if (Models.BASE[j] == m) return j;
        return Models.BASE.length; // the ensemble
    }

    /** The models ranked by mean score over the last WINDOW draws scored, best first. */
    static List<Rank> rank(Models st) {
        List<Rank> out = new ArrayList<>();
        int from = Math.max(Math.min(WARMUP, Math.max(0, st.scores.size() - 1)), st.scores.size() - Models.WINDOW);
        for (Predictor.Model m : Models.RANKED) {
            double[] ms = meanSe(st.scores, col(m), Math.max(0, from));
            Rank r = new Rank();
            r.model = m;
            r.mean = ms[0];
            r.se = ms[1];
            r.n = (int) ms[2];
            r.lo = Double.isNaN(r.se) ? Double.NaN : r.mean - 1.96 * r.se;
            r.hi = Double.isNaN(r.se) ? Double.NaN : r.mean + 1.96 * r.se;
            out.add(r);
        }
        out.sort((a, b) -> Double.compare(b.mean, a.mean));
        return out;
    }

    static long seed(Game g, LocalDate target) {
        return (g.id.hashCode() * 31L + (target == null ? 0 : target.toEpochDay())) * 0x9E3779B97F4A7C15L;
    }

    /** Today's recommendation for the game's next draw (`target`), from every draw in the sample. */
    static Rec recommend(Sample s, LocalDate target, boolean monteCarlo) {
        Rec r = new Rec();
        Game g = s.game;
        r.game = g;
        r.target = target;
        r.draws = s.size();
        r.dataUntil = s.to();
        r.theoryP = Predictor.theory(g);
        if (s.size() == 0) return r;
        Models st = Models.over(s, true);
        long seed = seed(g, target);
        for (Rank k : rank(st)) {
            Predictor.Weights w = st.weights(k.model);
            k.nums = st.top(w, seed);
            k.extra = Predictor.hasExtra(g) ? st.topExtra(w, seed) : -1;
            k.modelP = Predictor.probability(g, w, k.nums, k.extra);
            r.ranking.add(k);
        }
        Rank best = r.ranking.get(0);
        r.model = best.model;
        r.nums = best.nums;
        r.extra = best.extra;
        r.modelP = best.modelP;
        if (monteCarlo && s.size() >= WARMUP + 50) {
            r.mcP = monteCarloP(g, s.size(), best.mean, seed);
            r.advantage = !Double.isNaN(best.lo) && best.lo > 0 && r.mcP < 0.05;
        }
        return r;
    }

    private static final Map<String, Double> MC = new HashMap<>();

    /**
     * How often a fair lottery produces a best-of-five model scoring at least `observed` over the window: the same
     * models run on simulated fair draws (99 runs, seeded, so the answer is reproducible). Accounts for picking the
     * best of several models, which a plain confidence interval doesn't.
     */
    static synchronized double monteCarloP(Game g, int length, double observed, long seed) {
        String key = g.id + "/" + length + "/" + Math.round(observed * 1e6);
        Double cached = MC.get(key);
        if (cached != null) return cached;
        int sims = 99, atLeast = 0;
        Random rnd = new Random(seed);
        int scored = Math.min(length - 1, Models.WINDOW + 50);
        for (int s = 0; s < sims; s++) {
            Models m = new Models(g);
            for (int t = 0; t < length; t++) m.add(fair(g, rnd), t >= length - scored);
            double best = Double.NEGATIVE_INFINITY;
            int from = Math.max(0, m.scores.size() - Models.WINDOW);
            for (Predictor.Model pm : Models.RANKED) best = Math.max(best, meanSe(m.scores, col(pm), from)[0]);
            if (best >= observed) atLeast++;
        }
        double p = (atLeast + 1) / (double) (sims + 1);
        MC.put(key, p);
        return p;
    }

    static Draw fair(Game g, Random r) {
        if (g.digits()) {
            int[] n = new int[g.count];
            for (int i = 0; i < n.length; i++) n[i] = r.nextInt(10);
            return new Draw(g, null, "", n, r.nextInt(10));
        }
        int[] pool = new int[g.max];
        for (int i = 0; i < g.max; i++) pool[i] = i + 1;
        for (int i = 0; i < g.count; i++) {
            int j = i + r.nextInt(g.max - i);
            int x = pool[i];
            pool[i] = pool[j];
            pool[j] = x;
        }
        int[] n = java.util.Arrays.copyOf(pool, g.count);
        java.util.Arrays.sort(n);
        return new Draw(g, null, "", n, g.extraMax > 0 ? 1 + r.nextInt(g.extraMax) : -1);
    }

    // ------------------------------------------------------------------ track record

    /**
     * Replays the whole history: before every draw (after the first WARMUP), the recommendation the app would have
     * made from the earlier draws only, and every model's own top pick, each compared with the result that came.
     */
    private static final Map<String, Track> TRACKS = new HashMap<>();

    static Track track(Sample s) {
        String key = s.game.id + "/" + s.size() + "/" + s.to();
        synchronized (TRACKS) {
            Track t = TRACKS.get(key);
            if (t != null) return t;
        }
        Track t = replay(s);
        synchronized (TRACKS) {
            TRACKS.put(key, t);
        }
        return t;
    }

    private static Track replay(Sample s) {
        Track tr = new Track();
        Game g = s.game;
        tr.game = g;
        for (Predictor.Model m : Models.RANKED) tr.perModel.put(m, new ArrayList<>());
        Models st = new Models(g);
        for (int t = 0; t < s.size(); t++) {
            Draw d = s.draws.get(t);
            if (t >= WARMUP) {
                long seed = seed(g, d.date);
                List<Rank> ranking = rank(st);
                Map<Predictor.Model, Outcome> made = new EnumMap<>(Predictor.Model.class);
                for (Predictor.Model m : Models.RANKED) {
                    Predictor.Weights w = st.weights(m);
                    Outcome o = outcome(g, d, m, st.top(w, seed), Predictor.hasExtra(g) ? st.topExtra(w, seed) : -1);
                    tr.perModel.get(m).add(o);
                    made.put(m, o);
                }
                tr.best.add(made.get(ranking.get(0).model));
            }
            st.add(d, true);
        }
        return tr;
    }

    static Outcome outcome(Game g, Draw actual, Predictor.Model m, int[] nums, int extra) {
        Outcome o = new Outcome();
        o.date = actual.date;
        o.model = m;
        o.nums = nums;
        o.extra = extra;
        o.actual = actual;
        Draw guess = new Draw(g, actual.date, "", g.digits() ? nums : sorted(nums), extra);
        o.matched = Analysis.repeatsBetween(guess, actual);
        if (g.digits()) {
            o.exact = guess.value() == actual.value();
            o.last3 = guess.lastThree() == actual.lastThree();
            o.last2 = guess.lastTwo() == actual.lastTwo();
            o.last1 = nums[nums.length - 1] == actual.nums[actual.nums.length - 1];
        } else {
            o.exact = o.matched == g.count && (extra < 0 || actual.extra == extra);
            o.extraHit = extra > 0 && actual.extra == extra;
        }
        return o;
    }

    private static int[] sorted(int[] a) {
        int[] c = a.clone();
        java.util.Arrays.sort(c);
        return c;
    }

    /** Hits of the outcomes dated `from` or later (null: all), against what random selection gives. */
    static Stats stats(Game g, List<Outcome> list, LocalDate from) {
        Stats st = new Stats();
        for (Outcome o : list) {
            if (from != null && o.date.isBefore(from)) continue;
            st.n++;
            st.matched += o.matched;
            if (o.exact) st.exact++;
            if (o.last3) st.last3++;
            if (o.last2) st.last2++;
            if (o.last1) st.last1++;
            if (!g.digits()) {
                if (o.matched >= 2) st.two++;
                if (o.matched >= 3) st.three++;
                if (o.extra > 0 && o.actual.extra > 0) {
                    st.extraN++;
                    if (o.extraHit) st.extraHits++;
                }
            }
        }
        int n = st.n;
        if (g.digits()) {
            st.expExact = n * 1e-4;
            st.expLast3 = n * 1e-3;
            st.expLast2 = n * 1e-2;
            st.expLast1 = n * 0.1;
            st.expMatched = n * 0.4;
            st.pExact = Combi.binomUpper(n, 1e-4, st.exact);
            st.pLast3 = Combi.binomUpper(n, 1e-3, st.last3);
            st.pLast2 = Combi.binomUpper(n, 1e-2, st.last2);
            st.pLast1 = Combi.binomUpper(n, 0.1, st.last1);
            st.ciLast2 = wilson(st.last2, n);
            double sd = Math.sqrt(n * 0.36);
            st.pMatched = sd > 0 ? 1 - Combi.normalCdf((st.matched - st.expMatched) / sd) : Double.NaN;
            st.primaryP = st.pLast2;
        } else {
            int N = g.max, k = g.count;
            double p2 = 0, mu = k * k / (double) N;
            for (int m = 2; m <= k; m++) p2 += Combi.hypergeom(N, k, k, m);
            double var = k * (k / (double) N) * ((N - k) / (double) N) * ((N - k) / (double) (N - 1));
            st.expMatched = n * mu;
            st.expTwo = n * p2;
            st.pTwo = Combi.binomUpper(n, p2, st.two);
            st.ciTwo = wilson(st.two, n);
            double sd = Math.sqrt(n * var);
            st.pMatched = sd > 0 ? 1 - Combi.normalCdf((st.matched - st.expMatched) / sd) : Double.NaN;
            st.expExact = n / g.combinations();
            st.primaryP = st.pMatched;
        }
        return st;
    }

    /** 95% Wilson interval for a proportion. */
    static double[] wilson(int x, int n) {
        if (n == 0) return new double[]{0, 0};
        double z = 1.96, p = x / (double) n, den = 1 + z * z / n;
        double c = (p + z * z / (2 * n)) / den, h = z * Math.sqrt(p * (1 - p) / n + z * z / (4.0 * n * n)) / den;
        return new double[]{Math.max(0, c - h), Math.min(1, c + h)};
    }
}
