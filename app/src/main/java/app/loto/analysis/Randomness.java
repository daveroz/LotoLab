package app.loto.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * The audit of single draws: how ordinary each result is compared with all possible fair draws (its sum, even/odd
 * split, repeated digits or consecutive numbers, repeats from the draw before), 100 = completely typical.
 */
final class Randomness {
    private Randomness() {}

    /** How ordinary one draw is: 100 = completely typical, near 0 = a shape fair draws rarely produce. */
    static final class DrawScore {
        final Draw draw;
        final int score;
        final double rarity;  // the corrected probability behind the score
        final List<String> reasons = new ArrayList<>();
        DrawScore(Draw draw, int score, double rarity) {
            this.draw = draw;
            this.score = score;
            this.rarity = rarity;
        }
        String label() { return drawLabel(score); }
    }

    // ================================================================ single draws

    /** P(longest run of consecutive numbers >= len) for a fair lotto draw. */
    static double runAtLeast(int N, int k, int len) {
        if (len <= 1) return 1;
        double[][] cur = new double[k + 1][len];
        cur[0][0] = 1;
        for (int i = 1; i <= N; i++) {
            double[][] nx = new double[k + 1][len];
            for (int j = 0; j <= k; j++) {
                for (int r = 0; r < len; r++) {
                    double w = cur[j][r];
                    if (w == 0) continue;
                    nx[j][0] += w;
                    if (j < k && r + 1 < len) nx[j + 1][r + 1] += w;
                }
            }
            cur = nx;
        }
        double ok = 0;
        for (int r = 0; r < len; r++) ok += cur[k][r];
        return Math.max(0, 1 - ok / Combi.choose(N, k));
    }

    static int longestRun(Draw d) {
        int best = 1, run = 1;
        for (int i = 1; i < d.nums.length; i++) {
            run = d.nums[i] == d.nums[i - 1] + 1 ? run + 1 : 1;
            best = Math.max(best, run);
        }
        return best;
    }

    private static double twoSided(double[] pr, int x) {
        double lo = 0, hi = 0;
        for (int i = 0; i < pr.length; i++) {
            if (i <= x) lo += pr[i];
            if (i >= x) hi += pr[i];
        }
        return Math.min(1, 2 * Math.min(lo, hi));
    }

    static List<DrawScore> drawScores(Sample s) {
        Game g = s.game;
        double[] sumP = Analysis.sumProbabilities(g), evenP = Analysis.evenProbabilities(g);
        double[] patTail = new double[5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) if (Analysis.PATTERN_P[j] <= Analysis.PATTERN_P[i]) patTail[i] += Analysis.PATTERN_P[j];
        }
        int[] mid = Analysis.middleHalf(g);
        List<DrawScore> out = new ArrayList<>();
        java.util.Map<Game, Draw> previous = new java.util.HashMap<>();
        for (Draw d : s.draws) {
            List<String> why = new ArrayList<>();
            List<Double> ps = new ArrayList<>();
            int sum = d.sum();
            double ps1 = twoSided(sumP, Math.min(sumP.length - 1, Math.max(0, sum)));
            ps.add(ps1);
            if (ps1 < 0.05) why.add(T.f("Sum %d is far from the middle (half of fair draws total %d–%d).", sum, mid[0], mid[1]));
            int ev = d.evens();
            double ps2 = twoSided(evenP, ev);
            ps.add(ps2);
            if (ps2 < 0.15) {
                why.add(g.digits() ? T.f("%d of %d digits are even (happens in %s of fair draws).", ev, g.count, Combi.percent(evenP[ev]))
                        : T.f("%d of %d numbers are even (happens in %s of fair draws).", ev, g.count, Combi.percent(evenP[ev])));
            }
            Draw prev = previous.get(d.game);
            if (g.digits()) {
                int pat = Analysis.pattern(d);
                ps.add(patTail[pat]);
                if (pat >= 2) why.add(T.f("%s: %s of fair draws.", Analysis.patternName(pat), Combi.percent(Analysis.PATTERN_P[pat])));
                boolean up = true, down = true;
                for (int i = 1; i < d.nums.length; i++) {
                    if (d.nums[i] != d.nums[i - 1] + 1) up = false;
                    if (d.nums[i] != d.nums[i - 1] - 1) down = false;
                }
                ps.add(up || down ? 14 / 10000.0 : 1.0);
                if (up || down) why.add(T.t("A straight run of digits: 14 of the 10,000 numbers are like that."));
                if (prev != null && prev.lastTwo() == d.lastTwo()) {
                    ps.add(0.01);
                    why.add(T.f("Same last two digits as the previous %s draw (1 in 100).", d.game.name));
                } else {
                    ps.add(1.0);
                }
            } else {
                int run = longestRun(d);
                double pr = runAtLeast(g.max, g.count, run);
                ps.add(pr);
                if (run >= 3) why.add(T.f("%d consecutive numbers in a row (%s of fair draws).", run, Combi.percent(pr)));
                if (prev != null) {
                    int sh = d.shared(prev);
                    double tail = 0;
                    for (int m = sh; m <= g.count; m++) tail += Combi.hypergeom(g.max, g.count, g.count, m);
                    ps.add(sh == 0 ? 1.0 : tail);
                    if (sh >= 2) why.add(T.f("%d numbers repeated from the previous draw (%s of fair draws).", sh, Combi.percent(tail)));
                } else {
                    ps.add(1.0);
                }
            }
            previous.put(d.game, d);
            double min = 1;
            for (double p : ps) min = Math.min(min, p);
            double rarity = Math.min(1, ps.size() * min);
            DrawScore ds = new DrawScore(d, (int) Math.round(100 * rarity), rarity);
            if (why.isEmpty()) why.add(T.t("Nothing out of the ordinary."));
            ds.reasons.addAll(why);
            out.add(ds);
        }
        return out;
    }

    static String drawLabel(int score) {
        if (score >= 50) return T.t("Ordinary");
        if (score >= 10) return T.t("A little unusual");
        if (score >= 1) return T.t("Unusual");
        return T.t("Very unusual");
    }
}
