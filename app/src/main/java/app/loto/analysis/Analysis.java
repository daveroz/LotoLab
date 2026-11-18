package app.loto.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Descriptive statistics of a sample: how often each number came up compared with what a fair draw gives,
 * repeats (inside a draw, from one draw to the next, between draws on the same day), even/odd splits, recurring
 * combinations and the sum of the numbers. Every observed figure comes with its expected value under a fair
 * draw, so the screens can always say "41 times, 34.9 expected".
 */
final class Analysis {
    private Analysis() {}

    /** One value of a frequency table: a ball, a digit in a position, a two-digit ending... */
    static final class Cell implements Comparable<Cell> {
        final String label;
        final int value;
        final int position;   // digit position 0..3, or -1
        final double observed, expected, z;
        final int gap;        // draws since it last came up
        Cell(String label, int value, int position, double observed, double expected, double sd, int gap) {
            this.label = label;
            this.value = value;
            this.position = position;
            this.observed = observed;
            this.expected = expected;
            this.z = sd > 0 ? (observed - expected) / sd : 0;
            this.gap = gap;
        }
        /** Hottest first. */
        @Override public int compareTo(Cell o) { return Double.compare(o.z, z); }
    }

    // ================================================================ frequencies

    static final class Freq {
        int draws;
        /** Lotto: one cell per ball. Digits: one per digit across all positions. */
        final List<Cell> values = new ArrayList<>();
        /** Digits: [position][digit]. */
        Cell[][] positions;
        /** Digits: two-digit endings 00..99 (the "2 cifras" bet). */
        final List<Cell> endings = new ArrayList<>();
        final List<Cell> extra = new ArrayList<>();
        int extraDraws;
        double chi, p;
        int df;
        double extraChi = Double.NaN, extraP = Double.NaN;
        int extraDf;
        /** Expected wait between two appearances of one value, in draws. */
        double meanGap;

        List<Cell> hot(int n) {
            List<Cell> all = new ArrayList<>(positions != null ? flat(positions) : values);
            Collections.sort(all);
            return all.subList(0, Math.min(n, all.size()));
        }

        List<Cell> cold(int n) {
            List<Cell> all = new ArrayList<>(positions != null ? flat(positions) : values);
            Collections.sort(all);
            Collections.reverse(all);
            return all.subList(0, Math.min(n, all.size()));
        }

        List<Cell> overdue(int n) {
            List<Cell> all = new ArrayList<>(positions != null ? flat(positions) : values);
            all.sort((a, b) -> Integer.compare(b.gap, a.gap));
            return all.subList(0, Math.min(n, all.size()));
        }

        /** Two-sided p-value of the most extreme single value after allowing for how many values were looked at. */
        double extremeP() {
            List<Cell> all = positions != null ? flat(positions) : values;
            double worst = 0;
            for (Cell c : all) worst = Math.max(worst, Math.abs(c.z));
            return Math.min(1, all.size() * Combi.zTwoSided(worst));
        }
    }

    static List<Cell> flat(Cell[][] t) {
        List<Cell> out = new ArrayList<>();
        for (Cell[] r : t) Collections.addAll(out, r);
        return out;
    }

    static final String[] POS = {"1st", "2nd", "3rd", "4th"};

    static Freq frequency(Sample s) {
        Freq f = new Freq();
        Game g = s.game;
        int D = s.size();
        f.draws = D;
        if (g.digits()) {
            int k = g.count;
            int[][] cnt = new int[k][10];
            int[][] last = new int[k][10];
            for (int[] r : last) java.util.Arrays.fill(r, -1);
            int[] end = new int[100];
            int[] endLast = new int[100];
            java.util.Arrays.fill(endLast, -1);
            for (int t = 0; t < D; t++) {
                Draw d = s.draws.get(t);
                for (int i = 0; i < k; i++) {
                    cnt[i][d.nums[i]]++;
                    last[i][d.nums[i]] = t;
                }
                end[d.lastTwo()]++;
                endLast[d.lastTwo()] = t;
            }
            double e = D / 10.0, sd = Math.sqrt(D * 0.1 * 0.9);
            f.positions = new Cell[k][10];
            double chi = 0;
            for (int i = 0; i < k; i++) {
                for (int v = 0; v < 10; v++) {
                    int gap = last[i][v] < 0 ? D : D - 1 - last[i][v];
                    f.positions[i][v] = new Cell(v + " (" + POS[i] + ")", v, i, cnt[i][v], e, sd, gap);
                    if (e > 0) chi += (cnt[i][v] - e) * (cnt[i][v] - e) / e;
                }
            }
            for (int v = 0; v < 10; v++) {
                int c = 0, gap = D;
                for (int i = 0; i < k; i++) {
                    c += cnt[i][v];
                    gap = Math.min(gap, f.positions[i][v].gap);
                }
                f.values.add(new Cell(String.valueOf(v), v, -1, c, k * e, Math.sqrt(k) * sd, gap));
            }
            double e2 = D / 100.0, sd2 = Math.sqrt(D * 0.01 * 0.99);
            for (int v = 0; v < 100; v++) {
                f.endings.add(new Cell(String.format(java.util.Locale.US, "%02d", v), v, -1, end[v], e2, sd2,
                        endLast[v] < 0 ? D : D - 1 - endLast[v]));
            }
            f.chi = chi;
            f.df = k * 9;
            f.p = D > 0 ? Combi.chiSquareP(chi, f.df) : Double.NaN;
            f.meanGap = 10;
            extraFreq(f, s, 0, 9);
        } else {
            int N = g.max, k = g.count;
            int[] cnt = new int[N + 1], last = new int[N + 1];
            java.util.Arrays.fill(last, -1);
            for (int t = 0; t < D; t++) {
                for (int v : s.draws.get(t).nums) {
                    if (v < 1 || v > N) continue;
                    cnt[v]++;
                    last[v] = t;
                }
            }
            double p = k / (double) N, e = D * p, sd = Math.sqrt(D * p * (1 - p));
            double chi = 0;
            for (int v = 1; v <= N; v++) {
                f.values.add(new Cell(String.format(java.util.Locale.US, "%02d", v), v, -1, cnt[v], e, sd,
                        last[v] < 0 ? D : D - 1 - last[v]));
                if (e > 0) chi += (cnt[v] - e) * (cnt[v] - e) / e;
            }
            // k different balls per draw make the counts less variable than independent draws would be;
            // this factor brings Pearson's statistic back to a chi-square with N-1 degrees of freedom.
            f.chi = chi * (N - 1) / (double) (N - k);
            f.df = N - 1;
            f.p = D > 0 ? Combi.chiSquareP(f.chi, f.df) : Double.NaN;
            f.meanGap = N / (double) k;
            if (g.extraMax > 0) extraFreq(f, s, 1, g.extraMax);
        }
        return f;
    }

    private static void extraFreq(Freq f, Sample s, int lo, int hi) {
        List<Draw> ex = s.withExtra();
        int M = hi - lo + 1, D = ex.size();
        f.extraDraws = D;
        if (D == 0) return;
        int[] cnt = new int[hi + 1], last = new int[hi + 1];
        java.util.Arrays.fill(last, -1);
        for (int t = 0; t < D; t++) {
            int v = ex.get(t).extra;
            if (v < lo || v > hi) continue;
            cnt[v]++;
            last[v] = t;
        }
        double e = D / (double) M, sd = Math.sqrt(D * (1.0 / M) * (1 - 1.0 / M)), chi = 0;
        for (int v = lo; v <= hi; v++) {
            f.extra.add(new Cell(s.game.digits() ? String.valueOf(v) : String.format(java.util.Locale.US, "%02d", v),
                    v, -1, cnt[v], e, sd, last[v] < 0 ? D : D - 1 - last[v]));
            chi += (cnt[v] - e) * (cnt[v] - e) / e;
        }
        f.extraChi = chi;
        f.extraDf = M - 1;
        f.extraP = Combi.chiSquareP(chi, M - 1);
    }

    // ================================================================ distributions

    /** An observed histogram next to the exact distribution of a fair draw, with a pooled chi-square. */
    static final class Dist {
        String[] labels;
        double[] observed, expected;
        double chi, p;
        int df;
        boolean enough;
        double obsMean, expMean, obsSd, expSd;
    }

    /**
     * Pearson chi-square of observed counts against expected counts, pooling neighbouring bins until each expects at
     * least 5 (the usual rule for the approximation to hold). Returns {chi, df, p}; df 0 means too little data.
     */
    static double[] pooledChi(double[] obs, double[] exp) {
        List<double[]> bins = new ArrayList<>();
        double o = 0, e = 0;
        for (int i = 0; i < obs.length; i++) {
            o += obs[i];
            e += exp[i];
            if (e >= 5) {
                bins.add(new double[]{o, e});
                o = 0;
                e = 0;
            }
        }
        if (e > 0 || o > 0) {
            if (bins.isEmpty()) bins.add(new double[]{o, e});
            else {
                double[] lastBin = bins.get(bins.size() - 1);
                lastBin[0] += o;
                lastBin[1] += e;
            }
        }
        if (bins.size() < 2) return new double[]{0, 0, Double.NaN};
        double chi = 0;
        for (double[] b : bins) if (b[1] > 0) chi += (b[0] - b[1]) * (b[0] - b[1]) / b[1];
        int df = bins.size() - 1;
        return new double[]{chi, df, Combi.chiSquareP(chi, df)};
    }

    static Dist finish(Dist d, int draws) {
        double[] r = pooledChi(d.observed, d.expected);
        d.chi = r[0];
        d.df = (int) r[1];
        d.p = r[2];
        d.enough = d.df > 0 && draws >= 20;
        return d;
    }

    /** Probability of each number of even numbers in one draw (0..count). */
    static double[] evenProbabilities(Game g) {
        double[] pr = new double[g.count + 1];
        if (g.digits()) {
            for (int e = 0; e <= g.count; e++) pr[e] = Combi.binomPmf(g.count, 0.5, e);
        } else {
            int evens = g.max / 2;
            for (int e = 0; e <= g.count; e++) pr[e] = Combi.hypergeom(g.max, evens, g.count, e);
        }
        return pr;
    }

    static Dist evenOdd(Sample s) {
        Game g = s.game;
        Dist d = new Dist();
        double[] pr = evenProbabilities(g);
        d.labels = new String[g.count + 1];
        d.observed = new double[g.count + 1];
        d.expected = new double[g.count + 1];
        for (int e = 0; e <= g.count; e++) {
            d.labels[e] = e + " even / " + (g.count - e) + " odd";
            d.expected[e] = pr[e] * s.size();
        }
        double sum = 0, sq = 0;
        for (Draw x : s.draws) {
            int e = x.evens();
            d.observed[e]++;
            sum += e;
            sq += e * (double) e;
        }
        int n = Math.max(1, s.size());
        d.obsMean = sum / n;
        d.obsSd = Math.sqrt(Math.max(0, sq / n - d.obsMean * d.obsMean));
        double m = 0, v = 0;
        for (int e = 0; e <= g.count; e++) m += e * pr[e];
        for (int e = 0; e <= g.count; e++) v += (e - m) * (e - m) * pr[e];
        d.expMean = m;
        d.expSd = Math.sqrt(v);
        return finish(d, s.size());
    }

    /** Exact distribution of the sum of one draw's numbers. */
    static double[] sumProbabilities(Game g) {
        if (g.digits()) {
            double[] p = {1};
            for (int i = 0; i < g.count; i++) {
                double[] q = new double[p.length + 9];
                for (int s = 0; s < p.length; s++) for (int v = 0; v < 10; v++) q[s + v] += p[s] / 10;
                p = q;
            }
            return p;
        }
        int N = g.max, k = g.count, maxSum = 0;
        for (int i = 0; i < k; i++) maxSum += N - i;
        // ways[j][s]: subsets of size j with sum s, built ball by ball
        double[][] ways = new double[k + 1][maxSum + 1];
        ways[0][0] = 1;
        for (int v = 1; v <= N; v++) {
            for (int j = Math.min(k, v); j >= 1; j--) {
                for (int s = maxSum; s >= v; s--) ways[j][s] += ways[j - 1][s - v];
            }
        }
        double total = Combi.choose(N, k);
        double[] p = new double[maxSum + 1];
        for (int s = 0; s <= maxSum; s++) p[s] = ways[k][s] / total;
        return p;
    }

    static Dist sums(Sample s) {
        double[] pr = sumProbabilities(s.game);
        Dist d = new Dist();
        d.labels = new String[pr.length];
        d.observed = new double[pr.length];
        d.expected = new double[pr.length];
        double m = 0, v = 0;
        for (int i = 0; i < pr.length; i++) {
            d.labels[i] = String.valueOf(i);
            d.expected[i] = pr[i] * s.size();
            m += i * pr[i];
        }
        for (int i = 0; i < pr.length; i++) v += (i - m) * (i - m) * pr[i];
        double sum = 0, sq = 0;
        for (Draw x : s.draws) {
            int t = x.sum();
            if (t >= 0 && t < pr.length) d.observed[t]++;
            sum += t;
            sq += t * (double) t;
        }
        int n = Math.max(1, s.size());
        d.obsMean = sum / n;
        d.obsSd = Math.sqrt(Math.max(0, sq / n - d.obsMean * d.obsMean));
        d.expMean = m;
        d.expSd = Math.sqrt(v);
        return finish(d, s.size());
    }

    /** The sums that hold the middle half of all fair draws: {low, high}. */
    static int[] middleHalf(Game g) {
        double[] p = sumProbabilities(g);
        double c = 0;
        int lo = 0, hi = p.length - 1;
        for (int i = 0; i < p.length; i++) {
            c += p[i];
            if (c >= 0.25) { lo = i; break; }
        }
        c = 0;
        for (int i = 0; i < p.length; i++) {
            c += p[i];
            if (c >= 0.75) { hi = i; break; }
        }
        return new int[]{lo, hi};
    }

    // ================================================================ repeats

    /** Chance only: the five shapes a 4-digit number can have, as in the poker test. */
    static final String[] PATTERNS = {"All different (ABCD)", "One pair (AABC)", "Two pairs (AABB)",
            "Three of a kind (AAAB)", "Four of a kind (AAAA)"};
    static final double[] PATTERN_P = {0.504, 0.432, 0.027, 0.036, 0.001};

    static int pattern(Draw d) {
        int[] c = new int[10];
        for (int v : d.nums) c[v]++;
        int pairs = 0, max = 0;
        for (int v : c) {
            if (v == 2) pairs++;
            max = Math.max(max, v);
        }
        if (max == 4) return 4;
        if (max == 3) return 3;
        if (pairs == 2) return 2;
        if (pairs == 1) return 1;
        return 0;
    }

    static Dist patterns(Sample s) {
        Dist d = new Dist();
        d.labels = PATTERNS.clone();
        d.observed = new double[5];
        d.expected = new double[5];
        for (Draw x : s.draws) d.observed[pattern(x)]++;
        // order rare shapes together so pooling merges like with like
        double[] o = {d.observed[0], d.observed[1], d.observed[3], d.observed[2], d.observed[4]};
        double[] e = {PATTERN_P[0], PATTERN_P[1], PATTERN_P[3], PATTERN_P[2], PATTERN_P[4]};
        for (int i = 0; i < 5; i++) {
            d.expected[i] = PATTERN_P[i] * s.size();
            e[i] *= s.size();
        }
        double[] r = pooledChi(o, e);
        d.chi = r[0];
        d.df = (int) r[1];
        d.p = r[2];
        d.enough = d.df > 0 && s.size() >= 20;
        return d;
    }

    /**
     * How much each draw repeats the one before it. Lotto: numbers shared with the previous draw (hypergeometric).
     * Chance: digits that land in the same position as in the previous draw (binomial, 4 tries at 1 in 10).
     */
    static Dist fromPrevious(Sample s) {
        Game g = s.game;
        Dist d = new Dist();
        int k = g.count;
        double[] pr = new double[k + 1];
        for (int m = 0; m <= k; m++) {
            pr[m] = g.digits() ? Combi.binomPmf(k, 0.1, m) : Combi.hypergeom(g.max, k, k, m);
        }
        d.labels = new String[k + 1];
        d.observed = new double[k + 1];
        d.expected = new double[k + 1];
        int pairs = Math.max(0, s.size() - 1);
        for (int m = 0; m <= k; m++) {
            d.labels[m] = m + (g.digits() ? " in the same place" : " shared");
            d.expected[m] = pr[m] * pairs;
        }
        for (int t = 1; t < s.size(); t++) d.observed[repeatsBetween(s.draws.get(t - 1), s.draws.get(t))]++;
        return finish(d, pairs);
    }

    static int repeatsBetween(Draw a, Draw b) {
        if (a.game.digits()) {
            int m = 0;
            for (int i = 0; i < a.nums.length; i++) if (a.nums[i] == b.nums[i]) m++;
            return m;
        }
        return a.shared(b);
    }

    /** Results that came up more than once in the sample, and whether that's more than chance gives. */
    static final class Duplicates {
        final List<String> lines = new ArrayList<>();
        int coincidingPairs;
        double expectedPairs, p;
        int sameAsPrevious;
        double expectedSameAsPrevious;
        /** Chance: the previous draw's last two digits again. */
        int endingAgain;
        double expectedEndingAgain, endingP;
    }

    static Duplicates duplicates(Sample s) {
        Duplicates r = new Duplicates();
        Map<String, List<Draw>> seen = new HashMap<>();
        for (Draw d : s.draws) {
            String key = d.numbersText();
            List<Draw> l = seen.get(key);
            if (l == null) seen.put(key, l = new ArrayList<>());
            l.add(d);
        }
        List<List<Draw>> reps = new ArrayList<>();
        for (List<Draw> l : seen.values()) {
            if (l.size() > 1) {
                reps.add(l);
                r.coincidingPairs += l.size() * (l.size() - 1) / 2;
            }
        }
        reps.sort((a, b) -> b.size() != a.size() ? b.size() - a.size() : b.get(b.size() - 1).compareTo(a.get(a.size() - 1)));
        for (int i = 0; i < Math.min(12, reps.size()); i++) {
            List<Draw> l = reps.get(i);
            StringBuilder sb = new StringBuilder(l.get(0).numbersText()).append("  ×").append(l.size()).append(":  ");
            for (int j = 0; j < l.size(); j++) {
                if (j > 0) sb.append(", ");
                Draw d = l.get(j);
                sb.append(Fmt.date(d.date));
                if (s.games.size() > 1) sb.append(" (").append(d.game.shortName).append(')');
            }
            r.lines.add(sb.toString());
        }
        double combos = s.digits() ? Math.pow(10, s.game.count) : Combi.choose(s.game.max, s.game.count);
        double n = s.size();
        r.expectedPairs = n * (n - 1) / 2 / combos;
        r.p = Combi.poissonUpper(r.expectedPairs, r.coincidingPairs);
        for (int t = 1; t < s.size(); t++) {
            Draw a = s.draws.get(t - 1), b = s.draws.get(t);
            if (a.numbersText().equals(b.numbersText())) r.sameAsPrevious++;
            if (s.digits() && a.lastTwo() == b.lastTwo()) r.endingAgain++;
        }
        r.expectedSameAsPrevious = Math.max(0, n - 1) / combos;
        if (s.digits()) {
            r.expectedEndingAgain = Math.max(0, n - 1) / 100.0;
            r.endingP = Combi.binomTwoSided((int) Math.max(0, n - 1), 0.01, r.endingAgain);
        }
        return r;
    }

    /** Draws of two different series on the same day: how much they have in common compared with chance. */
    static final class SameDay {
        String pair;
        int days;
        Dist shared;
        int identical;
        double expectedIdentical;
        /** Chance: same last two digits on the same day. */
        int sameEnding;
        double expectedSameEnding, sameEndingP;
        final List<String> examples = new ArrayList<>();
    }

    static List<SameDay> sameDay(History h, List<Game> games, int days) {
        List<SameDay> out = new ArrayList<>();
        for (int i = 0; i < games.size(); i++) {
            for (int j = i + 1; j < games.size(); j++) {
                Game a = games.get(i), b = games.get(j);
                List<Game> both = new ArrayList<>();
                both.add(a);
                both.add(b);
                Sample s = h.sample(both, days);
                Map<java.time.LocalDate, Draw[]> byDay = new HashMap<>();
                for (Draw d : s.draws) {
                    Draw[] pair = byDay.get(d.date);
                    if (pair == null) byDay.put(d.date, pair = new Draw[2]);
                    pair[d.game == a ? 0 : 1] = d;
                }
                SameDay r = new SameDay();
                r.pair = a.name + " vs " + b.name;
                int k = a.count;
                double[] pr = new double[k + 1];
                for (int m = 0; m <= k; m++) {
                    pr[m] = a.digits() ? Combi.binomPmf(k, 0.1, m) : Combi.hypergeom(a.max, k, k, m);
                }
                Dist d = new Dist();
                d.labels = new String[k + 1];
                d.observed = new double[k + 1];
                d.expected = new double[k + 1];
                List<java.time.LocalDate> dates = new ArrayList<>(byDay.keySet());
                Collections.sort(dates);
                for (java.time.LocalDate day : dates) {
                    Draw[] p = byDay.get(day);
                    if (p[0] == null || p[1] == null) continue;
                    r.days++;
                    int m = repeatsBetween(p[0], p[1]);
                    d.observed[m]++;
                    if (p[0].numbersText().equals(p[1].numbersText())) r.identical++;
                    if (a.digits() && p[0].lastTwo() == p[1].lastTwo()) r.sameEnding++;
                    boolean notable = a.digits() ? (p[0].lastTwo() == p[1].lastTwo() || m >= 3) : m >= 3;
                    if (notable) {
                        r.examples.add(Fmt.date(day) + ":  " + a.shortName + " " + p[0].numbersText() + "  ·  "
                                + b.shortName + " " + p[1].numbersText());
                    }
                }
                for (int m = 0; m <= k; m++) {
                    d.labels[m] = m + (a.digits() ? " in the same place" : " shared");
                    d.expected[m] = pr[m] * r.days;
                }
                r.shared = finish(d, r.days);
                double combos = a.digits() ? Math.pow(10, k) : Combi.choose(a.max, k);
                r.expectedIdentical = r.days / combos;
                if (a.digits()) {
                    r.expectedSameEnding = r.days / 100.0;
                    r.sameEndingP = Combi.binomTwoSided(r.days, 0.01, r.sameEnding);
                }
                Collections.reverse(r.examples);
                while (r.examples.size() > 8) r.examples.remove(r.examples.size() - 1);
                if (r.days > 0) out.add(r);
            }
        }
        return out;
    }

    // ================================================================ combinations

    static final class Combo {
        final String label;
        final int count;
        final double expected;
        Combo(String label, int count, double expected) {
            this.label = label;
            this.count = count;
            this.expected = expected;
        }
    }

    static final class Combos {
        final List<Combo> pairs = new ArrayList<>();
        final List<Combo> triples = new ArrayList<>();
        /** p-value of the most frequent pair / triple, allowing for how many there are. */
        double pairMaxP = Double.NaN, tripleMaxP = Double.NaN;
        int pairCount, tripleCount;
        /** Chance: most frequent 2- and 3-digit endings. */
        final List<Combo> endings2 = new ArrayList<>(), endings3 = new ArrayList<>();
        double ending2MaxP = Double.NaN, ending3MaxP = Double.NaN;
        /** Special shapes with their observed and expected counts. */
        final List<Combo> shapes = new ArrayList<>();
        final List<Double> shapeP = new ArrayList<>();
    }

    /** P(the largest of `cells` Poisson(lambda) counts is >= max), treating the counts as independent. */
    static double maxP(int max, double lambda, double cells) {
        double tail = Combi.poissonUpper(lambda, max);
        return Math.min(1, 1 - Math.pow(1 - tail, cells));
    }

    static Combos combos(Sample s) {
        Combos c = new Combos();
        Game g = s.game;
        int D = s.size();
        if (g.digits()) {
            int[] e2 = new int[100], e3 = new int[1000];
            for (Draw d : s.draws) {
                e2[d.lastTwo()]++;
                e3[d.lastThree()]++;
            }
            c.ending2MaxP = topCells(e2, D / 100.0, "%02d", c.endings2);
            c.ending3MaxP = topCells(e3, D / 1000.0, "%03d", c.endings3);
            int pal = 0, run = 0, dates = 0, aaaa = 0;
            for (Draw d : s.draws) {
                int[] n = d.nums;
                if (n[0] == n[3] && n[1] == n[2]) pal++;
                boolean up = true, down = true;
                for (int i = 1; i < 4; i++) {
                    if (n[i] != n[i - 1] + 1) up = false;
                    if (n[i] != n[i - 1] - 1) down = false;
                }
                if (up || down) run++;
                if (dateLike(d.value())) dates++;
                if (pattern(d) == 4) aaaa++;
            }
            addShape(c, "Mirror numbers (ABBA, e.g. 1221)", pal, D, 100 / 10000.0);
            addShape(c, "Straight runs (1234, 8765)", run, D, 14 / 10000.0);
            addShape(c, "Four of a kind (0000–9999)", aaaa, D, 10 / 10000.0);
            addShape(c, "Look like a date (DDMM, e.g. 2512)", dates, D, DATE_LIKE / 10000.0);
            return c;
        }
        int N = g.max, k = g.count;
        int[][] pair = new int[N + 1][N + 1];
        Map<Integer, Integer> triple = new HashMap<>();
        int consecutive = 0;
        for (Draw d : s.draws) {
            int[] n = d.nums;
            boolean cons = false;
            for (int i = 0; i < k; i++) {
                if (i > 0 && n[i] == n[i - 1] + 1) cons = true;
                for (int j = i + 1; j < k; j++) {
                    if (n[i] < 1 || n[j] > N) continue;
                    pair[n[i]][n[j]]++;
                    for (int l = j + 1; l < k; l++) {
                        int key = (n[i] * 64 + n[j]) * 64 + n[l];
                        Integer v = triple.get(key);
                        triple.put(key, v == null ? 1 : v + 1);
                    }
                }
            }
            if (cons) consecutive++;
        }
        double pairs = Combi.choose(N, 2), triples = Combi.choose(N, 3);
        double ep = D * Combi.choose(k, 2) / pairs, et = D * Combi.choose(k, 3) / triples;
        List<int[]> pl = new ArrayList<>();
        for (int a = 1; a <= N; a++) for (int b = a + 1; b <= N; b++) if (pair[a][b] > 0) pl.add(new int[]{a, b, pair[a][b]});
        pl.sort((x, y) -> y[2] - x[2]);
        for (int i = 0; i < Math.min(10, pl.size()); i++) {
            int[] x = pl.get(i);
            c.pairs.add(new Combo(String.format(java.util.Locale.US, "%02d · %02d", x[0], x[1]), x[2], ep));
        }
        c.pairCount = (int) pairs;
        if (!pl.isEmpty()) c.pairMaxP = maxP(pl.get(0)[2], ep, pairs);
        List<int[]> tl = new ArrayList<>();
        for (Map.Entry<Integer, Integer> en : triple.entrySet()) {
            int key = en.getKey();
            tl.add(new int[]{key / 4096, (key / 64) % 64, key % 64, en.getValue()});
        }
        tl.sort((x, y) -> y[3] - x[3]);
        for (int i = 0; i < Math.min(8, tl.size()); i++) {
            int[] x = tl.get(i);
            c.triples.add(new Combo(String.format(java.util.Locale.US, "%02d · %02d · %02d", x[0], x[1], x[2]), x[3], et));
        }
        c.tripleCount = (int) triples;
        if (!tl.isEmpty()) c.tripleMaxP = maxP(tl.get(0)[3], et, triples);
        double pCons = 1 - Combi.choose(N - k + 1, k) / Combi.choose(N, k);
        addShape(c, "At least two consecutive numbers (e.g. 17·18)", consecutive, D, pCons);
        int low = 0;
        for (Draw d : s.draws) {
            boolean allLow = true;
            for (int v : d.nums) if (v > 31) allLow = false;
            if (allLow) low++;
        }
        addShape(c, "All numbers 31 or lower (birthday picks)", low, D, Combi.choose(31, k) / Combi.choose(N, k));
        return c;
    }

    private static void addShape(Combos c, String label, int count, int draws, double p) {
        c.shapes.add(new Combo(label, count, draws * p));
        c.shapeP.add(draws > 0 ? Combi.binomTwoSided(draws, p, count) : Double.NaN);
    }

    private static double topCells(int[] counts, double expected, String fmt, List<Combo> out) {
        Integer[] idx = new Integer[counts.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> counts[b] - counts[a]);
        for (int i = 0; i < 10; i++) {
            if (counts[idx[i]] == 0) break;
            out.add(new Combo(String.format(java.util.Locale.US, fmt, idx[i]), counts[idx[i]], expected));
        }
        return counts[idx[0]] == 0 ? Double.NaN : maxP(counts[idx[0]], expected, counts.length);
    }

    /** How many of the 10,000 chance numbers read as a day and month (DDMM). */
    static final int DATE_LIKE = 366;

    static boolean dateLike(int v) {
        int dd = v / 100, mm = v % 100;
        if (mm < 1 || mm > 12 || dd < 1) return false;
        int[] len = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        return dd <= len[mm - 1];
    }
}
