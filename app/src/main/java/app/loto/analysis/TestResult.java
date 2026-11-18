package app.loto.analysis;

/** One statistical test: what it asks, what it found and how surprising that is for a fair draw. */
final class TestResult {
    final String key;
    final String name;
    /** The question in plain words. */
    final String question;
    double stat;
    int df;
    /** Probability of a result at least this far from expectation if the draw is fair. */
    double p = Double.NaN;
    /** p after the Holm-Bonferroni correction for running several tests. */
    double pAdj = Double.NaN;
    /** False when there's too little data for the test to mean anything. */
    boolean enough;
    /** What was observed against what was expected. */
    String detail = "";
    /** For replication: a short tag of what deviated (e.g. "07 high"), so the halves can be compared. */
    String direction = "";
    String game = "";

    TestResult(String key, String name, String question) {
        this.key = key;
        this.name = name;
        this.question = question;
    }

    boolean flaggedRaw() { return enough && p < 0.05; }

    boolean flagged() { return enough && pAdj < 0.05; }

    String verdict() {
        if (!enough) return T.t("Not enough data");
        if (pAdj < 0.001) return T.t("Strong deviation");
        if (pAdj < 0.01) return T.t("Clear deviation");
        if (pAdj < 0.05) return T.t("Deviation");
        if (p < 0.05) return T.t("Chance-level blip");
        return T.t("As expected");
    }
}
