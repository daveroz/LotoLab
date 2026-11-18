package app.loto.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Does the sample look like a fair random draw? A battery of classic tests (frequencies, even/odd, sums, the
 * poker test, gaps, runs, serial correlation, independence of positions, weekday dependence, drift over time,
 * repeated results), each with its p-value. Because running many tests produces some low p-values by chance
 * alone, the score uses Holm-Bonferroni adjusted p-values: a fair lottery should score 90-100 most of the time.
 * Every single draw also gets a typicality score: how ordinary it is compared with all possible fair draws.
 */
final class Randomness {
    private Randomness() {}

    static final class Report {
        final List<TestResult> tests = new ArrayList<>();
        /** 0..100, or -1 when no test had enough data. */
        int score = -1;
        int rawFlags, adjustedFlags, usable;
        final List<DrawScore> draws = new ArrayList<>();

        String label() { return label(score); }

        static String label(int score) {
            if (score < 0) return "Not enough data";
            if (score >= 90) return "Consistent with a fair random draw";
            if (score >= 70) return "Small deviations, typical of chance";
            if (score >= 40) return "Notable deviations";
            return "Strong deviations from randomness";
        }

        String summary() {
            if (usable == 0) return "Too few draws in this period for the tests to say anything. Choose a longer period.";
            double expected = usable * 0.05;
            return String.format(Locale.US, "%d of %d tests came out below p = 0.05 on their own (about %.1f would by chance "
                    + "alone). After correcting for running %d tests, %d remain%s.", rawFlags, usable, expected, usable,
                    adjustedFlags, adjustedFlags == 1 ? "s" : "");
        }
    }

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
    }

    static Report run(Sample s) {
        Report r = new Report();
        int D = s.size();
        Game g = s.game;
        Analysis.Freq f = Analysis.frequency(s);

        TestResult t = new TestResult("freq", "Number frequencies",
                g.digits() ? "Does every digit come up equally often in each of the four positions?"
                        : "Does every ball from 1 to " + g.max + " come up equally often?");
        t.stat = f.chi;
        t.df = f.df;
        t.p = f.p;
        t.enough = g.digits() ? D >= 50 : D * g.count / (double) g.max >= 5;
        List<Analysis.Cell> hot = f.hot(1), cold = f.cold(1);
        if (!hot.isEmpty()) {
            Analysis.Cell h = hot.get(0), c = cold.get(0);
            t.detail = String.format(Locale.US, "Most frequent %s: %s times (%s expected). Least frequent %s: %s times.",
                    h.label, Fmt.num(h.observed), Fmt.num(h.expected), c.label, Fmt.num(c.observed));
            t.direction = h.label + " high";
        }
        r.tests.add(t);

        if (f.extraDraws > 0) {
            int M = f.extra.size();
            t = new TestResult("extra", g.extraName + " frequencies", "Does every " + g.extraName.toLowerCase(Locale.US)
                    + " number come up equally often?");
            t.stat = f.extraChi;
            t.df = f.extraDf;
            t.p = f.extraP;
            t.enough = f.extraDraws >= 5 * M;
            Analysis.Cell top = Collections.min(f.extra);
            t.detail = String.format(Locale.US, "%d draws with a %s. Most frequent: %s (%s times, %s expected).",
                    f.extraDraws, g.extraName.toLowerCase(Locale.US), top.label, Fmt.num(top.observed), Fmt.num(top.expected));
            t.direction = top.label + " high";
            r.tests.add(t);
        }

        Analysis.Dist eo = Analysis.evenOdd(s);
        t = distTest("evenodd", "Even / odd split", "Is the number of even " + (g.digits() ? "digits" : "numbers")
                + " per draw what a fair draw gives?", eo);
        t.detail = String.format(Locale.US, "Average %.2f even per draw (fair: %.2f).", eo.obsMean, eo.expMean);
        t.direction = eo.obsMean > eo.expMean ? "more even" : "more odd";
        r.tests.add(t);

        Analysis.Dist sums = Analysis.sums(s);
        t = distTest("sum", "Sum of the numbers", "Are the totals of each draw spread the way fair draws spread?", sums);
        t.detail = String.format(Locale.US, "Average sum %.1f, spread %.1f (fair: %.1f and %.1f).", sums.obsMean, sums.obsSd,
                sums.expMean, sums.expSd);
        t.direction = sums.obsMean > sums.expMean ? "high sums" : "low sums";
        r.tests.add(t);

        if (g.digits()) {
            Analysis.Dist pat = Analysis.patterns(s);
            t = distTest("poker", "Repeated digits (poker test)",
                    "Do numbers like 1123 or 7777 (repeated digits) appear as often as they should?", pat);
            t.detail = String.format(Locale.US, "All digits different: %s (%s expected); with a pair: %s (%s); three or four "
                            + "alike: %s (%s).", Fmt.num(pat.observed[0]), Fmt.num(pat.expected[0]), Fmt.num(pat.observed[1]),
                    Fmt.num(pat.expected[1]), Fmt.num(pat.observed[3] + pat.observed[4]),
                    Fmt.num(pat.expected[3] + pat.expected[4]));
            t.direction = pat.observed[0] > pat.expected[0] ? "fewer repeats" : "more repeats";
            r.tests.add(t);
        } else {
            Analysis.Combos c = Analysis.combos(s);
            Analysis.Combo cons = c.shapes.get(0);
            t = new TestResult("consec", "Consecutive numbers", "Do draws with neighbouring numbers (like 17 and 18) "
                    + "appear as often as they should?");
            t.stat = cons.count;
            t.p = c.shapeP.get(0);
            t.enough = D >= 30;
            t.detail = String.format(Locale.US, "%d draws had two or more consecutive numbers (%s expected).", cons.count,
                    Fmt.num(cons.expected));
            t.direction = cons.count > cons.expected ? "more consecutive" : "fewer consecutive";
            r.tests.add(t);
        }

        Analysis.Dist prev = Analysis.fromPrevious(s);
        t = distTest("prev", "Repeats from the previous draw", g.digits()
                ? "Do digits stay in the same place from one draw to the next more (or less) than chance?"
                : "Do numbers carry over from one draw to the next more (or less) than chance?", prev);
        double repObs = 0, repExp = 0;
        for (int m = 1; m < prev.observed.length; m++) {
            repObs += m * prev.observed[m];
            repExp += m * prev.expected[m];
        }
        t.detail = String.format(Locale.US, "%s repeats in %d draw-to-draw steps (%s expected).", Fmt.num(repObs),
                Math.max(0, D - 1), Fmt.num(repExp));
        t.direction = repObs > repExp ? "more repeats" : "fewer repeats";
        r.tests.add(t);

        r.tests.add(serial(s));
        r.tests.add(runs(s));
        r.tests.add(gaps(s, f));
        if (g.digits()) r.tests.add(positions(s));
        r.tests.add(weekday(s));
        r.tests.add(drift(s));

        Analysis.Duplicates dup = Analysis.duplicates(s);
        t = new TestResult("dupes", "Repeated results", "Does the exact same result come back more often than the "
                + "\"birthday problem\" predicts?");
        t.stat = dup.coincidingPairs;
        t.p = dup.p;
        t.enough = D >= 30;
        t.detail = String.format(Locale.US, "%d pairs of identical results (%s expected by chance).", dup.coincidingPairs,
                Fmt.num(dup.expectedPairs));
        t.direction = "repeats";
        r.tests.add(t);

        for (TestResult x : r.tests) x.game = s.label();
        score(r);
        r.draws.addAll(drawScores(s));
        return r;
    }

    /** Adjusts the p-values for multiple testing and turns them into the 0-100 score. */
    static void score(Report r) {
        List<TestResult> usable = new ArrayList<>();
        for (TestResult t : r.tests) if (t.enough && !Double.isNaN(t.p)) usable.add(t);
        r.usable = usable.size();
        if (usable.isEmpty()) return;
        double[] p = new double[usable.size()];
        for (int i = 0; i < p.length; i++) p[i] = usable.get(i).p;
        double[] adj = Combi.holm(p);
        double sum = 0;
        for (int i = 0; i < p.length; i++) {
            TestResult t = usable.get(i);
            t.pAdj = adj[i];
            if (t.p < 0.05) r.rawFlags++;
            if (adj[i] < 0.05) r.adjustedFlags++;
            sum += adj[i] >= 0.05 ? 1 : adj[i] >= 0.01 ? 0.6 : adj[i] >= 0.001 ? 0.3 : 0;
        }
        r.score = (int) Math.round(100 * sum / p.length);
    }

    private static TestResult distTest(String key, String name, String question, Analysis.Dist d) {
        TestResult t = new TestResult(key, name, question);
        t.stat = d.chi;
        t.df = d.df;
        t.p = d.p;
        t.enough = d.enough;
        return t;
    }

    /** Lag-1 correlation of the sums: does a high total tend to follow a high total? */
    static TestResult serial(Sample s) {
        TestResult t = new TestResult("serial", "Serial correlation", "Does one draw's total say anything about the next one's?");
        int n = s.size();
        t.enough = n >= 30;
        if (n < 3) return t;
        double mean = 0;
        for (Draw d : s.draws) mean += d.sum();
        mean /= n;
        double num = 0, den = 0;
        for (int i = 0; i < n; i++) {
            double a = s.draws.get(i).sum() - mean;
            den += a * a;
            if (i > 0) num += a * (s.draws.get(i - 1).sum() - mean);
        }
        double rho = den > 0 ? num / den : 0;
        double z = rho * Math.sqrt(n);
        t.stat = z;
        t.p = Combi.zTwoSided(z);
        t.detail = String.format(Locale.US, "Correlation between consecutive totals: %+.3f (fair: about 0 ± %.3f).", rho,
                1 / Math.sqrt(n));
        t.direction = rho > 0 ? "positive" : "negative";
        return t;
    }

    /** Wald-Wolfowitz runs test on the totals above / below the median. */
    static TestResult runs(Sample s) {
        TestResult t = new TestResult("runs", "Runs above / below the middle",
                "Do high and low totals alternate the way a fair draw makes them, without long streaks or zigzags?");
        int n = s.size();
        if (n < 3) return t;
        double[] v = new double[n];
        for (int i = 0; i < n; i++) v[i] = s.draws.get(i).sum();
        double[] sorted = v.clone();
        java.util.Arrays.sort(sorted);
        double med = n % 2 == 1 ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2;
        int n1 = 0, n2 = 0, runs = 0, prev = 0;
        for (double x : v) {
            if (x == med) continue;
            int side = x > med ? 1 : -1;
            if (side > 0) n1++;
            else n2++;
            if (side != prev) runs++;
            prev = side;
        }
        t.enough = n1 >= 10 && n2 >= 10;
        if (n1 == 0 || n2 == 0) return t;
        double N = n1 + n2;
        double mu = 2.0 * n1 * n2 / N + 1;
        double var = 2.0 * n1 * n2 * (2.0 * n1 * n2 - N) / (N * N * (N - 1));
        double z = var > 0 ? (runs - mu) / Math.sqrt(var) : 0;
        t.stat = z;
        t.p = Combi.zTwoSided(z);
        t.detail = String.format(Locale.US, "%d runs (fair: %.1f ± %.1f).", runs, mu, Math.sqrt(var));
        t.direction = runs > mu ? "zigzag" : "streaky";
        return t;
    }

    /** Gap test: the waits between two appearances of the same value should follow a geometric distribution. */
    static TestResult gaps(Sample s, Analysis.Freq f) {
        TestResult t = new TestResult("gaps", "Waiting times (gap test)",
                "Do numbers come back after the waits a fair draw gives, not too regularly and not in clumps?");
        Game g = s.game;
        int D = s.size();
        double p = g.digits() ? 0.1 : g.count / (double) g.max;
        int maxBin = (int) Math.ceil(4 / p);
        double[] obs = new double[maxBin + 1];
        int total = 0;
        double sumGap = 0;
        if (g.digits()) {
            for (int pos = 0; pos < g.count; pos++) {
                int[] last = new int[10];
                java.util.Arrays.fill(last, -1);
                for (int i = 0; i < D; i++) {
                    int v = s.draws.get(i).nums[pos];
                    if (last[v] >= 0) {
                        int gap = i - last[v];
                        obs[Math.min(maxBin, gap)]++;
                        total++;
                        sumGap += gap;
                    }
                    last[v] = i;
                }
            }
        } else {
            int[] last = new int[g.max + 1];
            java.util.Arrays.fill(last, -1);
            for (int i = 0; i < D; i++) {
                for (int v : s.draws.get(i).nums) {
                    if (v < 1 || v > g.max) continue;
                    if (last[v] >= 0) {
                        int gap = i - last[v];
                        obs[Math.min(maxBin, gap)]++;
                        total++;
                        sumGap += gap;
                    }
                    last[v] = i;
                }
            }
        }
        // A wait of g draws can only be seen if it fits inside the D draws, which happens at D - g starting points,
        // so the geometric probabilities are weighted by (D - g) before scaling them to the number of waits seen.
        double[] exp = new double[maxBin + 1];
        double wsum = 0;
        for (int gp = 1; gp < D; gp++) {
            double w = (D - gp) * p * Math.pow(1 - p, gp - 1);
            exp[Math.min(maxBin, gp)] += w;
            wsum += w;
        }
        if (wsum > 0) for (int gp = 1; gp <= maxBin; gp++) exp[gp] *= total / wsum;
        double[] o2 = java.util.Arrays.copyOfRange(obs, 1, obs.length), e2 = java.util.Arrays.copyOfRange(exp, 1, exp.length);
        double[] res = Analysis.pooledChi(o2, e2);
        t.stat = res[0];
        t.df = (int) res[1];
        t.p = res[2];
        t.enough = total >= 200 && t.df > 0 && D >= 4 / p;
        t.detail = String.format(Locale.US, "%d waits measured, average %.1f draws (fair: %.1f).", total,
                total > 0 ? sumGap / total : 0, 1 / p);
        t.direction = total > 0 && sumGap / total > 1 / p ? "long waits" : "short waits";
        return t;
    }

    /** Chance only: is each digit independent of the digit next to it? */
    static TestResult positions(Sample s) {
        TestResult t = new TestResult("position", "Independent positions",
                "Is each digit independent of the one next to it (no favourite digit pairs like 3 followed by 7)?");
        double chi = 0;
        int df = 0;
        for (int pos = 0; pos + 1 < s.game.count; pos++) {
            double[][] table = new double[10][10];
            for (Draw d : s.draws) table[d.nums[pos]][d.nums[pos + 1]]++;
            double[] c = contingency(table, 1);
            chi += c[0];
            df += (int) c[1];
        }
        t.stat = chi;
        t.df = df;
        t.p = Combi.chiSquareP(chi, df);
        t.enough = s.size() >= 300;
        t.detail = String.format(Locale.US, "Checked %d neighbouring digit pairs in each of %d draws.", s.game.count - 1, s.size());
        t.direction = "pairs";
        return t;
    }

    /** Do the numbers depend on the day of the week? */
    static TestResult weekday(Sample s) {
        TestResult t = new TestResult("weekday", "Day of the week",
                "Are the numbers drawn on one day of the week different from those on other days?");
        Game g = s.game;
        int[] perDay = new int[7];
        for (Draw d : s.draws) perDay[d.date.getDayOfWeek().getValue() - 1]++;
        int days = 0, minDay = Integer.MAX_VALUE;
        for (int c : perDay) {
            if (c > 0) {
                days++;
                minDay = Math.min(minDay, c);
            }
        }
        if (days < 2) {
            t.detail = "All draws in this selection fall on the same day of the week.";
            return t;
        }
        double chi = 0;
        int df = 0;
        double minExpected;
        if (g.digits()) {
            for (int pos = 0; pos < g.count; pos++) {
                double[][] table = new double[7][10];
                for (Draw d : s.draws) table[d.date.getDayOfWeek().getValue() - 1][d.nums[pos]]++;
                double[] c = contingency(table, 1);
                chi += c[0];
                df += (int) c[1];
            }
            minExpected = minDay / 10.0;
        } else {
            double[][] table = new double[7][g.max];
            for (Draw d : s.draws) for (int v : d.nums) if (v >= 1 && v <= g.max) table[d.date.getDayOfWeek().getValue() - 1][v - 1]++;
            double[] c = contingency(table, (g.max - 1) / (double) (g.max - g.count));
            chi = c[0];
            df = (int) c[1];
            minExpected = minDay * g.count / (double) g.max;
        }
        t.stat = chi;
        t.df = df;
        t.p = Combi.chiSquareP(chi, df);
        t.enough = minExpected >= 3 && df > 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (perDay[i] == 0) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(Game.DAY_SHORT[i]).append(' ').append(perDay[i]);
        }
        t.detail = "Draws per day: " + sb + ".";
        t.direction = "weekday";
        return t;
    }

    /** Did the numbers change between the first and second half of the period? */
    static TestResult drift(Sample s) {
        TestResult t = new TestResult("drift", "Stable over time",
                "Are the numbers in the first half of the period distributed like those in the second half?");
        Game g = s.game;
        int D = s.size(), half = D / 2;
        double chi = 0;
        int df = 0;
        if (g.digits()) {
            for (int pos = 0; pos < g.count; pos++) {
                double[][] table = new double[2][10];
                for (int i = 0; i < D; i++) table[i < half ? 0 : 1][s.draws.get(i).nums[pos]]++;
                double[] c = contingency(table, 1);
                chi += c[0];
                df += (int) c[1];
            }
            t.enough = half / 10.0 >= 5;
        } else {
            double[][] table = new double[2][g.max];
            for (int i = 0; i < D; i++) for (int v : s.draws.get(i).nums) if (v >= 1 && v <= g.max) table[i < half ? 0 : 1][v - 1]++;
            double[] c = contingency(table, (g.max - 1) / (double) (g.max - g.count));
            chi = c[0];
            df = (int) c[1];
            t.enough = half * g.count / (double) g.max >= 5;
        }
        t.stat = chi;
        t.df = df;
        t.p = df > 0 ? Combi.chiSquareP(chi, df) : Double.NaN;
        if (D >= 2) {
            t.detail = "First half: " + Fmt.shortDate(s.draws.get(0).date) + " – " + Fmt.shortDate(s.draws.get(Math.max(0, half - 1)).date)
                    + ". Second half: " + Fmt.shortDate(s.draws.get(half).date) + " – " + Fmt.shortDate(s.draws.get(D - 1).date) + ".";
        }
        t.direction = "drift";
        return t;
    }

    /**
     * Pearson chi-square test of independence on a table, ignoring empty rows and columns. `scale` corrects for
     * lotto draws holding several different balls. Returns {chi, df}.
     */
    static double[] contingency(double[][] table, double scale) {
        int R = table.length, C = table[0].length;
        double[] rt = new double[R], ct = new double[C];
        double n = 0;
        for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) {
            rt[i] += table[i][j];
            ct[j] += table[i][j];
            n += table[i][j];
        }
        if (n == 0) return new double[]{0, 0};
        int rows = 0, cols = 0;
        for (double v : rt) if (v > 0) rows++;
        for (double v : ct) if (v > 0) cols++;
        double chi = 0;
        for (int i = 0; i < R; i++) {
            if (rt[i] == 0) continue;
            for (int j = 0; j < C; j++) {
                if (ct[j] == 0) continue;
                double e = rt[i] * ct[j] / n;
                chi += (table[i][j] - e) * (table[i][j] - e) / e;
            }
        }
        return new double[]{chi * scale, Math.max(0, (rows - 1) * (cols - 1))};
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
            if (ps1 < 0.05) why.add(String.format(Locale.US, "Sum %d is far from the middle (half of fair draws total %d–%d).",
                    sum, mid[0], mid[1]));
            int ev = d.evens();
            double ps2 = twoSided(evenP, ev);
            ps.add(ps2);
            if (ps2 < 0.15) why.add(String.format(Locale.US, "%d of %d %s are even (happens in %s of fair draws).", ev, g.count,
                    g.digits() ? "digits" : "numbers", Combi.percent(evenP[ev])));
            Draw prev = previous.get(d.game);
            if (g.digits()) {
                int pat = Analysis.pattern(d);
                ps.add(patTail[pat]);
                if (pat >= 2) why.add(Analysis.PATTERNS[pat] + ": " + Combi.percent(Analysis.PATTERN_P[pat]) + " of fair draws.");
                boolean up = true, down = true;
                for (int i = 1; i < d.nums.length; i++) {
                    if (d.nums[i] != d.nums[i - 1] + 1) up = false;
                    if (d.nums[i] != d.nums[i - 1] - 1) down = false;
                }
                ps.add(up || down ? 14 / 10000.0 : 1.0);
                if (up || down) why.add("A straight run of digits: 14 of the 10,000 numbers are like that.");
                if (prev != null && prev.lastTwo() == d.lastTwo()) {
                    ps.add(0.01);
                    why.add("Same last two digits as the previous " + d.game.name + " draw (1 in 100).");
                } else {
                    ps.add(1.0);
                }
            } else {
                int run = longestRun(d);
                double pr = runAtLeast(g.max, g.count, run);
                ps.add(pr);
                if (run >= 3) why.add(String.format(Locale.US, "%d consecutive numbers in a row (%s of fair draws).", run, Combi.percent(pr)));
                if (prev != null) {
                    int sh = d.shared(prev);
                    double tail = 0;
                    for (int m = sh; m <= g.count; m++) tail += Combi.hypergeom(g.max, g.count, g.count, m);
                    ps.add(sh == 0 ? 1.0 : tail);
                    if (sh >= 2) why.add(String.format(Locale.US, "%d numbers repeated from the previous draw (%s of fair draws).",
                            sh, Combi.percent(tail)));
                } else {
                    ps.add(1.0);
                }
            }
            previous.put(d.game, d);
            double min = 1;
            for (double p : ps) min = Math.min(min, p);
            double rarity = Math.min(1, ps.size() * min);
            DrawScore ds = new DrawScore(d, (int) Math.round(100 * rarity), rarity);
            if (why.isEmpty()) why.add("Nothing out of the ordinary.");
            ds.reasons.addAll(why);
            out.add(ds);
        }
        return out;
    }

    static String drawLabel(int score) {
        if (score >= 50) return "Ordinary";
        if (score >= 10) return "A little unusual";
        if (score >= 1) return "Unusual";
        return "Very unusual";
    }
}
