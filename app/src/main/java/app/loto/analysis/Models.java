package app.loto.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The predictive models as a running state over a sequence of draws: after each draw is added, every model can
 * say how likely it thinks each number is next. Built once over a sample, it gives the same weights the Picks tab
 * shows; built step by step, it gives what each model would have said before every past draw, which is how the
 * models are scored and ranked (see {@link Recommender}).
 *
 * Models: hot (counts + 1), recent (exponentially weighted, half-life 30 draws), overdue (draws since last seen),
 * Bayesian (counts shrunk towards fair by how much they vary beyond chance) and an ensemble that mixes the four by
 * their recent predictive scores (exponential weights, i.e. Bayesian model averaging on the log score).
 */
final class Models {
    static final double HALF_LIFE = 30;
    static final int WINDOW = 200;
    static final Predictor.Model[] BASE = {Predictor.Model.FREQUENCY, Predictor.Model.RECENT, Predictor.Model.OVERDUE,
            Predictor.Model.BAYES};
    /** The models that make probability forecasts and are ranked: the four above and their ensemble. */
    static final Predictor.Model[] RANKED = {Predictor.Model.FREQUENCY, Predictor.Model.RECENT, Predictor.Model.OVERDUE,
            Predictor.Model.BAYES, Predictor.Model.ENSEMBLE};

    final Game g;
    private final boolean digits;
    private final int rows, cols, lo, k;
    private final double p0;
    private final double[][] cnt, dec;
    private final int[][] last;
    private double mass;
    int t;
    private final int xcols, xlo;
    private final double[] xcnt, xdec;
    private final int[] xlast;
    private double xmass;
    int xt;
    private final double lambda = Math.pow(0.5, 1 / HALF_LIFE);
    /** Log-score of each base model (then the ensemble) for every draw added after the first, in order. */
    final List<double[]> scores = new ArrayList<>();

    Models(Game g) {
        this.g = g;
        digits = g.digits();
        rows = digits ? g.count : 1;
        cols = digits ? 10 : g.max + 1;
        lo = digits ? 0 : 1;
        k = g.count;
        p0 = digits ? 0.1 : g.count / (double) g.max;
        cnt = new double[rows][cols];
        dec = new double[rows][cols];
        last = new int[rows][cols];
        for (int[] r : last) Arrays.fill(r, -1);
        boolean hasExtra = digits || g.extraMax > 0;
        xlo = digits ? 0 : 1;
        xcols = hasExtra ? (digits ? 10 : g.extraMax + 1) : 0;
        xcnt = new double[xcols];
        xdec = new double[xcols];
        xlast = new int[xcols];
        Arrays.fill(xlast, -1);
    }

    /** The state after every draw of the sample; `score` also records each model's forecast scores (the ensemble needs them). */
    static Models over(Sample s, boolean score) {
        Models m = new Models(s.game);
        for (Draw d : s.draws) m.add(d, score);
        return m;
    }

    /** Adds the next draw; with `score`, first records how well every model predicted it. */
    void add(Draw d, boolean score) {
        if (score && t > 0) scores.add(scoreAll(d));
        for (double[] r : dec) for (int v = 0; v < r.length; v++) r[v] *= lambda;
        mass = mass * lambda + 1;
        if (digits) {
            for (int i = 0; i < rows; i++) {
                int v = d.nums[i];
                cnt[i][v]++;
                dec[i][v]++;
                last[i][v] = t;
            }
        } else {
            for (int v : d.nums) {
                if (v < 1 || v > g.max) continue;
                cnt[0][v]++;
                dec[0][v]++;
                last[0][v] = t;
            }
        }
        t++;
        if (xcols > 0 && d.extra >= xlo && d.extra < xcols) {
            for (int v = 0; v < xcols; v++) xdec[v] *= lambda;
            xmass = xmass * lambda + 1;
            xcnt[d.extra]++;
            xdec[d.extra]++;
            xlast[d.extra] = xt;
            xt++;
        }
    }

    // ------------------------------------------------------------------ weights

    Predictor.Weights weights(Predictor.Model m) {
        if (m == Predictor.Model.ENSEMBLE) return ensemble();
        Predictor.Weights w = new Predictor.Weights();
        w.w = new double[rows][cols];
        w.extra = xcols > 0 ? new double[xcols] : null;
        for (int r = 0; r < rows; r++) fill(m, w.w[r], lo, cnt[r], dec[r], last[r], t, mass, p0);
        if (w.extra != null) fill(m, w.extra, xlo, xcnt, xdec, xlast, xt, xmass, 1.0 / (xcols - xlo));
        if (!digits) {
            w.w[0][0] = 0;
            if (w.extra != null) w.extra[0] = 0;
        }
        return w;
    }

    private static void fill(Predictor.Model m, double[] out, int lo0, double[] c, double[] dc, int[] ls, int n, double ms, double p) {
        switch (m) {
            case FREQUENCY:
                for (int v = lo0; v < out.length; v++) out[v] = c[v] + 1;
                break;
            case RECENT: {
                double prior = 0.1 * ms * p + 1e-6;
                for (int v = lo0; v < out.length; v++) out[v] = dc[v] + prior;
                break;
            }
            case OVERDUE:
                for (int v = lo0; v < out.length; v++) out[v] = (ls[v] < 0 ? n : n - 1 - ls[v]) + 1;
                break;
            case BAYES: {
                // keep only the part of each count's deviation beyond chance variation (empirical Bayes)
                int vals = out.length - lo0;
                double e = n * p, var = n * p * (1 - p), obs = 0;
                for (int v = lo0; v < out.length; v++) obs += (c[v] - e) * (c[v] - e);
                obs /= Math.max(1, vals);
                double keep = obs > var && obs > 0 ? 1 - var / obs : 0;
                for (int v = lo0; v < out.length; v++) out[v] = Math.max(0.01, e + keep * (c[v] - e)) + 1;
                break;
            }
            default:
                for (int v = lo0; v < out.length; v++) out[v] = 1;
        }
    }

    /** How much weight each base model gets in the ensemble: exp(sum of its log scores over the window). */
    double[] ensembleWeights() {
        int n = BASE.length;
        double[] s = new double[n];
        int from = Math.max(0, scores.size() - WINDOW);
        for (int i = from; i < scores.size(); i++) for (int j = 0; j < n; j++) s[j] += scores.get(i)[j];
        double max = Double.NEGATIVE_INFINITY;
        for (double v : s) max = Math.max(max, v);
        double[] w = new double[n];
        double sum = 0;
        for (int j = 0; j < n; j++) {
            w[j] = Math.exp(s[j] - max);
            sum += w[j];
        }
        for (int j = 0; j < n; j++) w[j] /= sum;
        return w;
    }

    private Predictor.Weights ensemble() {
        double[] a = ensembleWeights();
        Predictor.Weights out = new Predictor.Weights();
        out.w = new double[rows][cols];
        out.extra = xcols > 0 ? new double[xcols] : null;
        for (int j = 0; j < BASE.length; j++) {
            Predictor.Weights w = weights(BASE[j]);
            for (int r = 0; r < rows; r++) {
                double s = 0;
                for (int v = lo; v < cols; v++) s += w.w[r][v];
                for (int v = lo; v < cols; v++) out.w[r][v] += a[j] * w.w[r][v] / s;
            }
            if (out.extra != null) {
                double s = 0;
                for (int v = xlo; v < xcols; v++) s += w.extra[v];
                for (int v = xlo; v < xcols; v++) out.extra[v] += a[j] * w.extra[v] / s;
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ scoring

    /**
     * Log score of a model's forecast for the draw that actually came, minus that of a fair draw (nats). Zero on
     * average for a fair forecast; positive only if the model really saw something coming. Lotto numbers are
     * scored one by one as in/out of the draw (Bernoulli log loss), the extra number as a categorical forecast.
     */
    double score(Predictor.Weights w, Draw d) {
        double s = 0;
        if (digits) {
            for (int i = 0; i < rows; i++) s += Math.log(w.w[i][d.nums[i]] / sum(w.w[i]) / 0.1);
        } else {
            double total = sum(w.w[0]);
            boolean[] in = new boolean[cols];
            for (int v : d.nums) if (v >= 1 && v < cols) in[v] = true;
            for (int v = 1; v < cols; v++) {
                double pi = Math.max(1e-6, Math.min(0.999, k * w.w[0][v] / total));
                s += in[v] ? Math.log(pi / p0) : Math.log((1 - pi) / (1 - p0));
            }
            if (w.extra != null && d.extra >= xlo && d.extra < xcols) {
                s += Math.log(w.extra[d.extra] / sum(w.extra) * (xcols - xlo));
            }
        }
        return s;
    }

    private double[] scoreAll(Draw d) {
        double[] out = new double[BASE.length + 1];
        for (int j = 0; j < BASE.length; j++) out[j] = score(weights(BASE[j]), d);
        out[BASE.length] = score(ensemble(), d);
        return out;
    }

    private static double sum(double[] a) {
        double s = 0;
        for (double v : a) s += v;
        return s;
    }

    // ------------------------------------------------------------------ the strongest candidate

    /**
     * The single most probable combination under a model: per position for chance, the top balls (and extra) for
     * lotto. Exact ties are broken by `seed` (the target draw), so the choice is reproducible.
     */
    int[] top(Predictor.Weights w, long seed) {
        java.util.Random r = new java.util.Random(seed);
        if (digits) {
            int[] n = new int[rows];
            for (int i = 0; i < rows; i++) n[i] = argmax(w.w[i], 0, r);
            return n;
        }
        Integer[] idx = new Integer[g.max];
        double[] jitter = new double[g.max + 1];
        for (int v = 1; v <= g.max; v++) jitter[v] = r.nextDouble();
        for (int i = 0; i < g.max; i++) idx[i] = i + 1;
        Arrays.sort(idx, (a, b) -> w.w[0][a] != w.w[0][b] ? Double.compare(w.w[0][b], w.w[0][a]) : Double.compare(jitter[a], jitter[b]));
        int[] n = new int[k];
        for (int i = 0; i < k; i++) n[i] = idx[i];
        Arrays.sort(n);
        return n;
    }

    int topExtra(Predictor.Weights w, long seed) {
        if (w.extra == null || digits) return -1;
        return argmax(w.extra, 1, new java.util.Random(seed ^ 0x5DEECE66DL));
    }

    private static int argmax(double[] a, int lo, java.util.Random r) {
        int best = lo;
        double bestJ = r.nextDouble();
        for (int i = lo + 1; i < a.length; i++) {
            double j = r.nextDouble();
            if (a[i] > a[best] || (a[i] == a[best] && j < bestJ)) {
                best = i;
                bestJ = j;
            }
        }
        return best;
    }
}
