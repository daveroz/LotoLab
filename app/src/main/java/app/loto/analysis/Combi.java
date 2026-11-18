package app.loto.analysis;

/**
 * The mathematics: binomial coefficients, the hypergeometric and binomial distributions, the normal and
 * chi-square distributions (through the regularized incomplete gamma function), Poisson tails and the
 * Holm-Bonferroni correction. No libraries, so the app stays dependency-free like its siblings.
 */
final class Combi {
    private Combi() {}

    static double choose(int n, int k) {
        if (k < 0 || k > n) return 0;
        k = Math.min(k, n - k);
        double r = 1;
        for (int i = 1; i <= k; i++) r = r * (n - k + i) / i;
        return Math.rint(r);
    }

    static double logChoose(int n, int k) {
        if (k < 0 || k > n) return Double.NEGATIVE_INFINITY;
        return logGamma(n + 1) - logGamma(k + 1) - logGamma(n - k + 1);
    }

    /** P(X = x) when drawing `draws` items from a population of `pop` with `good` successes. */
    static double hypergeom(int pop, int good, int draws, int x) {
        if (x < 0 || x > good || x > draws || draws - x > pop - good) return 0;
        return Math.exp(logChoose(good, x) + logChoose(pop - good, draws - x) - logChoose(pop, draws));
    }

    static double binomPmf(int n, double p, int x) {
        if (x < 0 || x > n) return 0;
        if (p <= 0) return x == 0 ? 1 : 0;
        if (p >= 1) return x == n ? 1 : 0;
        return Math.exp(logChoose(n, x) + x * Math.log(p) + (n - x) * Math.log(1 - p));
    }

    /** P(X >= x) for X ~ Binomial(n, p). */
    static double binomUpper(int n, double p, int x) {
        if (x <= 0) return 1;
        if (x > n) return 0;
        double s = 0;
        for (int i = x; i <= n; i++) s += binomPmf(n, p, i);
        return Math.min(1, s);
    }

    /** P(X <= x) for X ~ Binomial(n, p). */
    static double binomLower(int n, double p, int x) {
        if (x < 0) return 0;
        if (x >= n) return 1;
        double s = 0;
        for (int i = 0; i <= x; i++) s += binomPmf(n, p, i);
        return Math.min(1, s);
    }

    /** Two-sided binomial test p-value (doubling the smaller tail, capped at 1). */
    static double binomTwoSided(int n, double p, int x) {
        return Math.min(1, 2 * Math.min(binomUpper(n, p, x), binomLower(n, p, x)));
    }

    /** P(X >= x) for X ~ Poisson(lambda). */
    static double poissonUpper(double lambda, int x) {
        if (x <= 0) return 1;
        return regGammaP(x, lambda);
    }

    // ---------------------------------------------------------------- normal

    static double normalCdf(double z) {
        return 0.5 * erfc(-z / Math.sqrt(2));
    }

    /** Two-sided p-value of a z score. */
    static double zTwoSided(double z) {
        return Math.min(1, erfc(Math.abs(z) / Math.sqrt(2)));
    }

    /** Complementary error function, Numerical Recipes' Chebyshev fit (relative error < 1.2e-7). */
    static double erfc(double x) {
        double z = Math.abs(x);
        double t = 1 / (1 + 0.5 * z);
        double r = t * Math.exp(-z * z - 1.26551223 + t * (1.00002368 + t * (0.37409196 + t * (0.09678418
                + t * (-0.18628806 + t * (0.27886807 + t * (-1.13520398 + t * (1.48851587
                + t * (-0.82215223 + t * 0.17087277)))))))));
        return x >= 0 ? r : 2 - r;
    }

    // ---------------------------------------------------------------- chi-square

    /** Upper tail P(X >= x) for X ~ chi-square with df degrees of freedom. */
    static double chiSquareP(double x, int df) {
        if (df <= 0) return 1;
        if (x <= 0) return 1;
        return regGammaQ(df / 2.0, x / 2.0);
    }

    static double logGamma(double x) {
        double[] c = {76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155,
                0.1208650973866179e-2, -0.5395239384953e-5};
        double y = x, tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double ser = 1.000000000190015;
        for (double v : c) ser += v / ++y;
        return -tmp + Math.log(2.5066282746310005 * ser / x);
    }

    /** Regularized lower incomplete gamma P(a, x). */
    static double regGammaP(double a, double x) {
        if (x <= 0) return 0;
        if (x < a + 1) return gammaSeries(a, x);
        return 1 - gammaFraction(a, x);
    }

    /** Regularized upper incomplete gamma Q(a, x) = 1 - P(a, x), computed directly for accuracy in the tail. */
    static double regGammaQ(double a, double x) {
        if (x <= 0) return 1;
        if (x < a + 1) return 1 - gammaSeries(a, x);
        return gammaFraction(a, x);
    }

    private static double gammaSeries(double a, double x) {
        double ap = a, sum = 1 / a, del = sum;
        for (int n = 0; n < 1000; n++) {
            ap++;
            del *= x / ap;
            sum += del;
            if (Math.abs(del) < Math.abs(sum) * 1e-15) break;
        }
        return sum * Math.exp(-x + a * Math.log(x) - logGamma(a));
    }

    private static double gammaFraction(double a, double x) {
        double tiny = 1e-300, b = x + 1 - a, c = 1 / tiny, d = 1 / b, h = d;
        for (int i = 1; i < 1000; i++) {
            double an = -i * (i - a);
            b += 2;
            d = an * d + b;
            if (Math.abs(d) < tiny) d = tiny;
            c = b + an / c;
            if (Math.abs(c) < tiny) c = tiny;
            d = 1 / d;
            double del = d * c;
            h *= del;
            if (Math.abs(del - 1) < 1e-15) break;
        }
        return Math.exp(-x + a * Math.log(x) - logGamma(a)) * h;
    }

    // ---------------------------------------------------------------- multiple testing

    /** Holm-Bonferroni adjusted p-values, in the same order as given. */
    static double[] holm(double[] p) {
        int m = p.length;
        Integer[] idx = new Integer[m];
        for (int i = 0; i < m; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> Double.compare(p[a], p[b]));
        double[] adj = new double[m];
        double running = 0;
        for (int r = 0; r < m; r++) {
            double v = Math.min(1, (m - r) * p[idx[r]]);
            running = Math.max(running, v);
            adj[idx[r]] = running;
        }
        return adj;
    }

    /** "1 in 15,401,568" style odds. */
    static String oneIn(double p) {
        if (p <= 0) return "never";
        double n = 1 / p;
        if (n < 10) return String.format(java.util.Locale.US, "1 in %.1f", n);
        return "1 in " + String.format(java.util.Locale.US, "%,d", Math.round(n));
    }

    /** Probability as a percentage with sensible precision. */
    static String percent(double p) {
        double v = p * 100;
        if (v == 0) return "0%";
        if (v >= 10) return String.format(java.util.Locale.US, "%.1f%%", v);
        if (v >= 1) return String.format(java.util.Locale.US, "%.2f%%", v);
        if (v >= 0.01) return String.format(java.util.Locale.US, "%.3f%%", v);
        return String.format(java.util.Locale.US, "%.2e%%", v);
    }

    /** A p-value for people: "0.43", "0.004", "< 0.0001". */
    static String pText(double p) {
        if (Double.isNaN(p)) return "–";
        if (p < 0.0001) return "< 0.0001";
        if (p < 0.001) return String.format(java.util.Locale.US, "%.4f", p);
        if (p < 0.01) return String.format(java.util.Locale.US, "%.3f", p);
        return String.format(java.util.Locale.US, "%.2f", p);
    }
}
