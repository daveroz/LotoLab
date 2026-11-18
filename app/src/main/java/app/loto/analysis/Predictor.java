package app.loto.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Suggested combinations from several statistical models, each with its reasoning, the probability the model
 * gives it and the theoretical probability every combination has in a fair draw. A walk-forward backtest checks
 * each model honestly: it replays the last draws, letting the model see only what came before each one, and
 * compares its hits with pure chance.
 *
 * Weight models (hot, recent, overdue, Bayesian) give each number a weight; a lotto combination's model
 * probability is the product of its weights over the sum of that product across all combinations (the
 * "conditional Poisson" model, computed exactly with elementary symmetric polynomials); for chance each position
 * is its own urn, so it's the product of the four per-position probabilities.
 */
final class Predictor {
    private Predictor() {}

    enum Model {
        FREQUENCY, RECENT, OVERDUE, BAYES, ENSEMBLE, BALANCED, UNPOPULAR, RANDOM;

        String title() {
            switch (this) {
                case FREQUENCY: return T.t("Hot numbers");
                case RECENT: return T.t("Recent form");
                case OVERDUE: return T.t("Overdue");
                case BAYES: return T.t("Bayesian (fair-adjusted)");
                case ENSEMBLE: return T.t("Ensemble (performance-weighted)");
                case BALANCED: return T.t("Typical shape");
                case UNPOPULAR: return T.t("Least shared");
                default: return T.t("Quick pick");
            }
        }

        /** A short name for tables. */
        String shortTitle() {
            switch (this) {
                case BAYES: return T.t("Bayesian");
                case ENSEMBLE: return T.t("Ensemble");
                default: return title();
            }
        }

        String description() {
            switch (this) {
                case FREQUENCY: return T.t("Favours the numbers drawn most often in the period.");
                case RECENT: return T.t("Like hot numbers, but recent draws count more (weights halve every few dozen draws).");
                case OVERDUE: return T.t("Favours the numbers that haven't come up for longest. In a fair draw a number is never "
                        + "\"due\": this is the gambler's fallacy, shown for comparison.");
                case BAYES: return T.t("Starts from a fair draw and moves away from it only as far as the data's real "
                        + "variation supports. If frequencies vary no more than chance, every number stays equal.");
                case ENSEMBLE: return T.t("Mixes the four forecasting models, each weighted by how well it predicted the last "
                        + "200 draws (Bayesian model averaging on the log score). It leans on whichever model works, if any does.");
                case BALANCED: return T.t("A random combination with the shape most draws have: usual even/odd split, sum in "
                        + "the middle half, no long runs. Typical shapes are common only because there are more of them.");
                case UNPOPULAR: return T.t("Avoids what most people play (birthdays 1–31, dates, patterns, sequences). Same "
                        + "odds, but if it wins, the prize is less likely to be split.");
                default: return T.t("Uniformly random, like the terminal's quick pick. The benchmark the others are measured against.");
            }
        }
    }

    static final class Suggestion {
        int[] nums;
        int extra = -1;
        double modelP, theoryP;
        final List<String> reasons = new ArrayList<>();

        String text(Game g) {
            StringBuilder sb = new StringBuilder();
            if (g.digits()) {
                for (int d : nums) sb.append(d);
            } else {
                for (int v : nums) {
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(String.format(java.util.Locale.US, "%02d", v));
                }
            }
            return sb.toString();
        }
    }

    static final class Backtest {
        int draws;
        double hits, expected, sd, p;
        /** Chance: exact last-two-digit hits (the "2 cifras" bet) against the 1 in 100 expected. */
        int endingHits;
        double endingExpected;
        String summary = "";
    }

    static final class Result {
        Model model;
        Suggestion top;
        final List<Suggestion> alternatives = new ArrayList<>();
        String verdict = "";
        Backtest backtest;
        /** True when the model has a single most probable combination (weight models). */
        boolean hasTop;
    }

    // ================================================================ weights

    /** Per-value weights: lotto [1..max] (index 0 unused); chance [position][digit]. Extra in the last row. */
    static final class Weights {
        double[][] w;
        double[] extra;
    }

    /**
     * The weights of a model after seeing the sample. The forecasting models come from {@link Models}, the same code
     * that scores and ranks them, so every screen agrees; the shape models weigh numbers evenly (least shared
     * prefers numbers above 31).
     */
    static Weights weights(Model m, Sample s) {
        Game g = s.game;
        switch (m) {
            case FREQUENCY:
            case RECENT:
            case OVERDUE:
            case BAYES:
            case ENSEMBLE:
                return Models.over(s, m == Model.ENSEMBLE).weights(m);
            default: {
                Weights out = new Weights();
                int rows = g.digits() ? g.count : 1, cols = g.digits() ? 10 : g.max + 1;
                out.w = new double[rows][cols];
                for (double[] r : out.w) Arrays.fill(r, 1);
                int exHi = g.digits() ? 9 : g.extraMax;
                out.extra = exHi > 0 ? new double[exHi + 1] : null;
                if (out.extra != null) Arrays.fill(out.extra, 1);
                if (m == Model.UNPOPULAR && !g.digits()) for (int v = 32; v <= g.max; v++) out.w[0][v] = 2.5;
                if (!g.digits()) {
                    out.w[0][0] = 0;
                    if (out.extra != null) out.extra[0] = 0;
                }
                return out;
            }
        }
    }

    // ================================================================ combination probabilities

    /** Elementary symmetric polynomials e_r of w[v..N] for every v, r: E[v][r]. */
    private static double[][] esp(double[] w, int k) {
        int N = w.length - 1;
        double[][] E = new double[N + 2][k + 1];
        for (int v = N + 1; v >= 1; v--) {
            E[v][0] = 1;
            if (v == N + 1) continue;
            for (int r = 1; r <= k; r++) E[v][r] = E[v + 1][r] + w[v] * E[v + 1][r - 1];
        }
        return E;
    }

    static double probability(Game g, Weights wt, int[] nums, int extra) {
        double p;
        if (g.digits()) {
            p = 1;
            for (int i = 0; i < g.count; i++) p *= wt.w[i][nums[i]] / sum(wt.w[i]);
        } else {
            double[][] E = esp(wt.w[0], g.count);
            double prod = 1;
            for (int v : nums) prod *= wt.w[0][v];
            p = prod / E[1][g.count];
        }
        if (wt.extra != null && extra >= 0 && (!g.digits() || g.extraMax > 0) && hasExtra(g)) p *= wt.extra[extra] / sum(wt.extra);
        return p;
    }

    /** Whether suggestions include the extra number: Baloto's superbalota yes, chance's quinta no (a separate bet). */
    static boolean hasExtra(Game g) { return !g.digits() && g.extraMax > 0; }

    static double theory(Game g) {
        double c = g.digits() ? Math.pow(10, g.count) : Combi.choose(g.max, g.count);
        if (hasExtra(g)) c *= g.extraMax;
        return 1 / c;
    }

    private static double sum(double[] a) {
        double s = 0;
        for (double v : a) s += v;
        return s;
    }

    private static int[] topCombination(Game g, Weights wt) {
        if (g.digits()) {
            int[] n = new int[g.count];
            for (int i = 0; i < g.count; i++) n[i] = argmax(wt.w[i], 0);
            return n;
        }
        Integer[] idx = new Integer[g.max];
        for (int i = 0; i < g.max; i++) idx[i] = i + 1;
        Arrays.sort(idx, (a, b) -> wt.w[0][b] != wt.w[0][a] ? Double.compare(wt.w[0][b], wt.w[0][a]) : a - b);
        int[] n = new int[g.count];
        for (int i = 0; i < g.count; i++) n[i] = idx[i];
        Arrays.sort(n);
        return n;
    }

    private static int argmax(double[] a, int lo) {
        int best = lo;
        for (int i = lo; i < a.length; i++) if (a[i] > a[best]) best = i;
        return best;
    }

    private static int[] sample(Game g, Weights wt, Random rnd) {
        if (g.digits()) {
            int[] n = new int[g.count];
            for (int i = 0; i < g.count; i++) n[i] = pick(wt.w[i], rnd);
            return n;
        }
        double[] w = wt.w[0];
        double[][] E = esp(w, g.count);
        int[] n = new int[g.count];
        int r = g.count, j = 0;
        for (int v = 1; v <= g.max && r > 0; v++) {
            double pIn = w[v] * E[v + 1][r - 1] / E[v][r];
            if (rnd.nextDouble() < pIn) {
                n[j++] = v;
                r--;
            }
        }
        return n;
    }

    private static int pick(double[] w, Random rnd) {
        double t = rnd.nextDouble() * sum(w);
        for (int i = 0; i < w.length; i++) {
            t -= w[i];
            if (t <= 0 && w[i] > 0) return i;
        }
        return w.length - 1;
    }

    // ================================================================ shape filters

    static boolean balanced(Game g, int[] n) {
        int[] mid = Analysis.middleHalf(g);
        int sum = 0, ev = 0;
        for (int v : n) {
            sum += v;
            if (v % 2 == 0) ev++;
        }
        if (sum < mid[0] || sum > mid[1]) return false;
        double[] pe = Analysis.evenProbabilities(g);
        double best = 0;
        for (double p : pe) best = Math.max(best, p);
        if (pe[ev] < best * 0.6) return false;
        if (g.digits()) {
            Draw d = new Draw(g, null, "", n, -1);
            return Analysis.pattern(d) <= 1;
        }
        int run = 1, worst = 1;
        boolean[] decade = new boolean[6];
        for (int i = 0; i < n.length; i++) {
            if (i > 0) run = n[i] == n[i - 1] + 1 ? run + 1 : 1;
            worst = Math.max(worst, run);
            decade[n[i] / 10] = true;
        }
        int decades = 0;
        for (boolean b : decade) if (b) decades++;
        return worst <= 2 && decades >= 3;
    }

    static boolean unpopular(Game g, int[] n) {
        if (g.digits()) {
            Draw d = new Draw(g, null, "", n, -1);
            int v = d.value();
            if (Analysis.dateLike(v) || Analysis.dateLike(n[2] * 1000 + n[3] * 100 + n[0] * 10 + n[1])) return false;
            if (Analysis.pattern(d) >= 2) return false;
            if (n[0] == n[3] && n[1] == n[2]) return false;
            if (v % 100 == 0) return false;
            boolean up = true, down = true;
            for (int i = 1; i < 4; i++) {
                if (n[i] != n[i - 1] + 1) up = false;
                if (n[i] != n[i - 1] - 1) down = false;
            }
            return !up && !down && n[0] >= 3; // most players' "years" and low dates start with 0-2
        }
        int high = 0, months = 0;
        for (int v : n) {
            if (v > 31) high++;
            if (v <= 12) months++;
        }
        if (high < 2 || months > 2) return false;
        int step = n[1] - n[0];
        boolean progression = true;
        for (int i = 2; i < n.length; i++) if (n[i] - n[i - 1] != step) progression = false;
        if (progression) return false;
        int run = 1;
        for (int i = 1; i < n.length; i++) {
            run = n[i] == n[i - 1] + 1 ? run + 1 : 1;
            if (run >= 3) return false;
        }
        int sameDecade = 0;
        int[] dc = new int[6];
        for (int v : n) sameDecade = Math.max(sameDecade, ++dc[v / 10]);
        return sameDecade <= 3;
    }

    // ================================================================ suggestions

    static Result suggest(Model m, Sample s, long seed) {
        Result r = new Result();
        r.model = m;
        Game g = s.game;
        Random rnd = new Random(seed);
        Weights wt = weights(m, s);
        r.hasTop = m == Model.FREQUENCY || m == Model.RECENT || m == Model.OVERDUE || m == Model.BAYES || m == Model.ENSEMBLE;
        Analysis.Freq f = Analysis.frequency(s);
        List<int[]> picks = new ArrayList<>();
        if (r.hasTop) picks.add(topCombination(g, wt));
        int tries = 0;
        while (picks.size() < 4 && tries++ < 4000) {
            int[] n = sample(g, wt, rnd);
            if (m == Model.BALANCED && !balanced(g, n)) continue;
            if (m == Model.UNPOPULAR && !unpopular(g, n)) continue;
            boolean dup = false;
            for (int[] p : picks) if (Arrays.equals(p, n)) dup = true;
            if (!dup) picks.add(n);
        }
        for (int i = 0; i < picks.size(); i++) {
            Suggestion sg = new Suggestion();
            sg.nums = picks.get(i);
            if (hasExtra(g) && wt.extra != null) {
                sg.extra = r.hasTop && i == 0 ? argmax(wt.extra, 1) : pick(wt.extra, rnd);
                if (sg.extra < 1) sg.extra = 1 + rnd.nextInt(g.extraMax);
            }
            sg.theoryP = theory(g);
            sg.modelP = probability(g, wt, sg.nums, sg.extra);
            if (m == Model.BALANCED || m == Model.UNPOPULAR || m == Model.RANDOM) sg.modelP = sg.theoryP;
            explain(sg, m, s, f, wt);
            if (i == 0) r.top = sg;
            else r.alternatives.add(sg);
        }
        r.verdict = verdict(m, f);
        return r;
    }

    private static String verdict(Model m, Analysis.Freq f) {
        if (m == Model.RANDOM) return T.t("Every combination has exactly the same chance; this is what the other models are compared with.");
        if (m == Model.BALANCED) return T.t("Doesn't change your odds: each typical-looking combination is exactly as likely as any other single combination.");
        if (m == Model.UNPOPULAR) return T.t("Doesn't change your odds of winning. It only changes how many people you might share a jackpot with.");
        if (m == Model.OVERDUE) return T.t("Past draws don't make a number due. If the draw is fair, this model can't beat chance; see the backtest.");
        if (Double.isNaN(f.p)) return T.t("Not enough draws to judge.");
        if (f.p >= 0.05) {
            return T.f("The frequency differences this model uses are within what chance produces "
                    + "(chi-square p = %s). Expect its picks to do no better than random.", Combi.pText(f.p));
        }
        return T.f("Frequencies vary more than chance usually allows (p = %s). That could be a real bias "
                + "or a fluke; the backtest shows whether it actually helped predict.", Combi.pText(f.p));
    }

    private static void explain(Suggestion sg, Model m, Sample s, Analysis.Freq f, Weights wt) {
        Game g = s.game;
        int D = s.size();
        int recent = Math.min(30, D);
        if (g.digits()) {
            for (int i = 0; i < g.count; i++) {
                int v = sg.nums[i];
                Analysis.Cell c = f.positions[i][v];
                String line;
                switch (m) {
                    case RECENT: {
                        int n = 0;
                        for (int t = D - recent; t < D; t++) if (s.draws.get(t).nums[i] == v) n++;
                        line = T.f("%s digit %d: %d times in the last %d draws (%s expected).",
                                Analysis.pos(i), v, n, recent, Fmt.num(recent / 10.0));
                        break;
                    }
                    case OVERDUE:
                        line = T.f("%s digit %d: not seen there for %d draws (typical wait 10).",
                                Analysis.pos(i), v, c.gap);
                        break;
                    case BAYES:
                    case ENSEMBLE:
                        line = T.f("%s digit %d: estimated %s chance (fair: 10%%).", Analysis.pos(i), v,
                                Fmt.pct(wt.w[i][v] / sum(wt.w[i])));
                        break;
                    case FREQUENCY:
                        line = T.f("%s digit %d: %s of %d draws (%s; fair 10%%), z %s.", Analysis.pos(i), v,
                                Fmt.num(c.observed), D, Fmt.pct(D > 0 ? c.observed / D : 0), Fmt.signed(c.z));
                        break;
                    default:
                        line = null;
                }
                if (line != null) sg.reasons.add(line);
            }
        } else {
            for (int v : sg.nums) {
                Analysis.Cell c = f.values.get(v - 1);
                String line;
                switch (m) {
                    case FREQUENCY:
                        line = T.f("%02d: drawn %s times (%s expected, z %s), last seen %d draws ago.", v,
                                Fmt.num(c.observed), Fmt.num(c.expected), Fmt.signed(c.z), c.gap);
                        break;
                    case RECENT: {
                        int n = 0;
                        for (int t = D - recent; t < D; t++) if (s.draws.get(t).contains(v)) n++;
                        line = T.f("%02d: %d times in the last %d draws (%s expected).", v, n, recent,
                                Fmt.num(recent * g.count / (double) g.max));
                        break;
                    }
                    case OVERDUE:
                        line = T.f("%02d: not drawn for %d draws (typical wait %s).", v, c.gap, Fmt.num(f.meanGap));
                        break;
                    case BAYES:
                    case ENSEMBLE:
                        line = T.f("%02d: estimated %s chance per draw (fair: %s).", v,
                                Fmt.pct(g.count * wt.w[0][v] / sum(wt.w[0])), Fmt.pct(g.count / (double) g.max));
                        break;
                    default:
                        line = null;
                }
                if (line != null) sg.reasons.add(line);
            }
            if (sg.extra > 0 && !f.extra.isEmpty() && (m == Model.FREQUENCY || m == Model.RECENT || m == Model.OVERDUE)) {
                Analysis.Cell c = f.extra.get(sg.extra - 1);
                sg.reasons.add(T.f("%s %02d: %s times (%s expected), last seen %d draws ago.", g.extraName,
                        sg.extra, Fmt.num(c.observed), Fmt.num(c.expected), c.gap));
            }
            if (m == Model.UNPOPULAR) {
                int high = 0;
                for (int v : sg.nums) if (v > 31) high++;
                sg.reasons.add(T.f("%d of the numbers are above 31, which birthday players can't choose.", high));
                sg.reasons.add(T.t("No sequence, no arithmetic pattern, at most two month numbers (1–12)."));
            }
        }
        if (g.digits() && m == Model.UNPOPULAR) {
            sg.reasons.add(T.t("Not a date, no repeated digits, no sequence or mirror, doesn't start with 0–2 like most years and dates."));
        }
        if (m == Model.RANDOM) sg.reasons.add(T.t("Drawn uniformly at random, like a quick pick."));
        if (m == Model.ENSEMBLE) {
            double[] a = Models.over(s, true).ensembleWeights();
            StringBuilder mix = new StringBuilder();
            for (int j = 0; j < Models.BASE.length; j++) {
                if (mix.length() > 0) mix.append(" · ");
                mix.append(Models.BASE[j].title()).append(' ').append(Fmt.pct(a[j]));
            }
            sg.reasons.add(0, T.f("Mix: %s", mix));
        }
        sg.reasons.add(shape(g, sg.nums));
    }

    /** "3 even / 2 odd (34% of fair draws) · sum 118 (middle half 92–128)". */
    static String shape(Game g, int[] n) {
        int ev = 0, sum = 0;
        for (int v : n) {
            sum += v;
            if (v % 2 == 0) ev++;
        }
        double[] pe = Analysis.evenProbabilities(g);
        int[] mid = Analysis.middleHalf(g);
        String s = T.f("Shape: %d even / %d odd (%s of fair draws) · sum %d (half of fair draws: %d–%d)",
                ev, n.length - ev, Combi.percent(pe[ev]), sum, mid[0], mid[1]);
        if (!g.digits()) {
            int cons = 0;
            for (int i = 1; i < n.length; i++) if (n[i] == n[i - 1] + 1) cons++;
            if (cons > 0) s += " · " + T.f("consecutive pairs: %d", cons);
        }
        return s + ".";
    }

    // ================================================================ backtest

    static Backtest backtest(Model m, Sample s, long seed) {
        Backtest b = new Backtest();
        Game g = s.game;
        int D = s.size();
        int B = Math.min(100, D - 30);
        if (B < 10) {
            b.summary = T.t("Not enough draws to replay.");
            return b;
        }
        Random rnd = new Random(seed);
        int k = g.count;
        double mu, var;
        if (g.digits()) {
            mu = k * 0.1;
            var = k * 0.1 * 0.9;
        } else {
            double N = g.max;
            mu = k * k / N;
            var = k * (k / N) * ((N - k) / N) * ((N - k) / (N - 1));
        }
        for (int t = D - B; t < D; t++) {
            Sample train = s.upTo(t);
            int[] pick;
            if (m == Model.FREQUENCY || m == Model.RECENT || m == Model.OVERDUE || m == Model.BAYES || m == Model.ENSEMBLE) {
                pick = topCombination(g, weights(m, train));
            } else {
                Weights wt = weights(m, train);
                pick = sample(g, wt, rnd);
                for (int i = 0; i < 500; i++) {
                    if (m == Model.BALANCED && !balanced(g, pick)) pick = sample(g, wt, rnd);
                    else if (m == Model.UNPOPULAR && !unpopular(g, pick)) pick = sample(g, wt, rnd);
                    else break;
                }
            }
            Draw actual = s.draws.get(t);
            Draw guess = new Draw(g, actual.date, "", g.digits() ? pick : sorted(pick), -1);
            b.hits += Analysis.repeatsBetween(guess, actual);
            if (g.digits() && guess.lastTwo() == actual.lastTwo()) b.endingHits++;
        }
        b.draws = B;
        b.expected = B * mu;
        b.sd = Math.sqrt(B * var);
        double z = b.sd > 0 ? (b.hits - b.expected) / b.sd : 0;
        b.p = 1 - Combi.normalCdf(z);
        b.endingExpected = B / 100.0;
        b.summary = g.digits()
                ? T.f("Replaying the last %d draws, letting the model see only earlier results: its pick matched %.2f "
                + "digits in the right place per draw on average; pure chance gives %.2f.", B, b.hits / B, mu)
                : T.f("Replaying the last %d draws, letting the model see only earlier results: its pick matched %.2f "
                + "numbers per draw on average; pure chance gives %.2f.", B, b.hits / B, mu);
        b.summary += " " + (b.p < 0.05
                ? T.f("That's better than chance (p = %s), though with several models tried one doing well by luck is "
                + "expected now and then.", Combi.pText(b.p))
                : T.f("No better than chance (p = %s).", Combi.pText(b.p)));
        if (g.digits()) {
            b.summary += " " + T.f("Last two digits right: %d times (%s expected).", b.endingHits, Fmt.num(b.endingExpected));
        }
        return b;
    }

    private static int[] sorted(int[] a) {
        int[] c = a.clone();
        Arrays.sort(c);
        return c;
    }
}
