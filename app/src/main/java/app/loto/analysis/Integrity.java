package app.loto.analysis;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Looks for signs that a lottery's draws are not behaving like fair random draws, and is careful about what that
 * can and can't mean.
 *
 * 1. Every randomness test on every draw series of the lottery, plus tests across series (do two draws on the same
 *    day share more than chance?) and a "popular numbers" test (would results avoid what people like to bet, such
 *    as birthdays?). All p-values are corrected together (Holm-Bonferroni), because with dozens of tests a few low
 *    p-values are certain to appear by chance alone.
 * 2. A deviation that survives the correction is re-tested on the first and the second half of the period
 *    separately. A real, systematic effect shows up in both halves; a coincidence usually doesn't.
 * 3. The data itself is checked: impossible values, unreadable rows, unexplained breaks in the schedule.
 *
 * The result is graded as no sign, an anomaly (probably chance), a persistent deviation (a systematic pattern worth
 * investigating) or a data problem. None of these is proof of manipulation: statistics can only say that results
 * are unlikely under a fair draw, never why.
 */
final class Integrity {
    private Integrity() {}

    enum Level { CLEAR, ANOMALY, PERSISTENT, DATA }

    static final class Finding {
        final Level level;
        final String title, evidence, meaning;
        Finding(Level level, String title, String evidence, String meaning) {
            this.level = level;
            this.title = title;
            this.evidence = evidence;
            this.meaning = meaning;
        }
    }

    static final class Report {
        Level overall = Level.CLEAR;
        String headline = "", explanation = "";
        final List<Finding> findings = new ArrayList<>();
        final List<TestResult> tests = new ArrayList<>();
        int usable, rawFlags, adjustedFlags;
        double expectedRawFlags, excessP = Double.NaN;
        final List<String> dataIssues = new ArrayList<>();
        int draws;
    }

    static Report run(History h, String lottery, int days) {
        Report r = new Report();
        List<Game> games = Game.of(lottery);
        List<Sample> samples = new ArrayList<>();
        for (Game g : games) {
            List<Game> one = new ArrayList<>();
            one.add(g);
            Sample s = h.sample(one, days);
            r.draws += s.size();
            if (s.size() >= 30) samples.add(s);
            dataChecks(r, h, s);
        }
        for (Sample s : samples) {
            Randomness.Report rep = Randomness.run(s);
            r.tests.addAll(rep.tests);
            r.tests.add(popular(s));
        }
        for (Analysis.SameDay sd : Analysis.sameDay(h, games, days)) r.tests.addAll(sameDayTests(sd));

        List<TestResult> usable = new ArrayList<>();
        for (TestResult t : r.tests) if (t.enough && !Double.isNaN(t.p)) usable.add(t);
        r.usable = usable.size();
        if (!usable.isEmpty()) {
            double[] p = new double[usable.size()];
            for (int i = 0; i < p.length; i++) p[i] = usable.get(i).p;
            double[] adj = Combi.holm(p);
            for (int i = 0; i < p.length; i++) {
                usable.get(i).pAdj = adj[i];
                if (p[i] < 0.05) r.rawFlags++;
                if (adj[i] < 0.05) r.adjustedFlags++;
            }
            r.expectedRawFlags = usable.size() * 0.05;
            r.excessP = Combi.binomUpper(usable.size(), 0.05, r.rawFlags);
        }

        for (TestResult t : usable) {
            if (!t.flagged()) continue;
            Sample s = null;
            for (Sample x : samples) if (x.label().equals(t.game)) s = x;
            double[] halves = replicate(h, games, days, s, t);
            boolean persistent = halves != null && halves[0] < 0.05 && halves[1] < 0.05;
            String ev = T.f("%s · %s. p = %s, %s after correcting for %d tests.", t.game, t.detail,
                    Combi.pText(t.p), Combi.pText(t.pAdj), r.usable);
            if (halves != null) {
                ev += " " + T.f("First half p = %s, second half p = %s.", Combi.pText(halves[0]), Combi.pText(halves[1]));
            }
            if (persistent) {
                r.findings.add(new Finding(Level.PERSISTENT, t.name + " — " + t.game, ev,
                        T.t("The deviation is there in both halves of the period, so it's unlikely to be a one-off coincidence. "
                                + "It points to something systematic: worn or unevenly weighted balls, a machine or procedure "
                                + "that changed, errors in the published results, or deliberate interference. Statistics can't "
                                + "tell these apart.")));
            } else {
                r.findings.add(new Finding(Level.ANOMALY, t.name + " — " + t.game, ev,
                        T.t("Significant over the whole period but not in both halves on its own, which is what a chance "
                                + "fluctuation (or a short-lived problem) looks like. Worth watching as new draws arrive.")));
            }
        }
        for (String issue : r.dataIssues) {
            r.findings.add(new Finding(Level.DATA, T.t("Data check"), issue,
                    T.t("A gap or error in the published results, not a sign of how the draws were made.")));
        }

        boolean persistent = false, anomaly = false;
        for (Finding f : r.findings) {
            if (f.level == Level.PERSISTENT) persistent = true;
            if (f.level == Level.ANOMALY) anomaly = true;
        }
        if (r.usable == 0) {
            r.overall = Level.CLEAR;
            r.headline = T.t("Not enough data");
            r.explanation = T.t("This period has too few draws to test. Choose a longer period.");
        } else if (persistent) {
            r.overall = Level.PERSISTENT;
            r.headline = T.t("Persistent deviation from randomness");
            r.explanation = T.t("At least one pattern is both statistically significant after correction and present in both halves "
                    + "of the period. That is a reason to look closer, not proof of manipulation: proving manipulation needs "
                    + "evidence such as audits, inspection of the balls and machines, or the draw records.");
        } else if (anomaly) {
            r.overall = Level.ANOMALY;
            r.headline = T.t("Statistical anomaly, most likely chance");
            r.explanation = T.t("Something came out significant after correction, but it doesn't hold up in both halves of the "
                    + "period. Unusual patterns turn up in every long run of fair draws; this one isn't persistent enough to "
                    + "suggest a systematic cause.");
        } else {
            r.overall = Level.CLEAR;
            r.headline = T.t("No sign of irregularities");
            r.explanation = T.f("None of the %d tests shows a deviation that survives the correction for "
                    + "multiple testing. %d came out below p = 0.05 on their own, about what chance alone produces (%.1f "
                    + "expected). The draws look like fair random draws.", r.usable, r.rawFlags, r.expectedRawFlags);
        }
        return r;
    }

    /** Would the draws avoid the numbers people bet most (birthdays)? An operator rigging results might. */
    static TestResult popular(Sample s) {
        TestResult t = new TestResult("popular", T.t("Popular-number avoidance"), s.digits()
                ? T.t("Do numbers that look like a birthday (DDMM, e.g. 2512) come up less often than they should?")
                : T.t("Do the numbers 1 to 31 (birthday picks) come up less often than they should?"));
        int D = s.size();
        t.game = s.label();
        if (s.digits()) {
            int dates = 0;
            for (Draw d : s.draws) if (Analysis.dateLike(d.value())) dates++;
            double p = Analysis.DATE_LIKE / 10000.0;
            t.stat = dates;
            t.p = Combi.binomTwoSided(D, p, dates);
            t.enough = D * p >= 5;
            t.detail = T.f("%d date-like results (%s expected).", dates, Fmt.num(D * p));
            t.direction = dates < D * p ? "fewer" : "more";
        } else {
            Game g = s.game;
            int low = 0;
            for (Draw d : s.draws) for (int v : d.nums) if (v <= 31) low++;
            double q = 31.0 / g.max;
            double mean = D * g.count * q;
            double var = D * g.count * q * (1 - q) * (g.max - g.count) / (double) (g.max - 1);
            double z = var > 0 ? (low - mean) / Math.sqrt(var) : 0;
            t.stat = z;
            t.p = Combi.zTwoSided(z);
            t.enough = D >= 30;
            t.detail = T.f("%d of %d balls were 31 or lower (%s expected).", low, D * g.count, Fmt.num(mean));
            t.direction = low < mean ? "fewer" : "more";
        }
        return t;
    }

    static List<TestResult> sameDayTests(Analysis.SameDay sd) {
        List<TestResult> out = new ArrayList<>();
        TestResult t = new TestResult("sameday", T.t("Same-day overlap"),
                T.t("Do two draws held on the same day have more (or fewer) numbers in common than chance?"));
        t.game = sd.pair;
        t.stat = sd.shared.chi;
        t.df = sd.shared.df;
        t.p = sd.shared.p;
        t.enough = sd.shared.enough && sd.days >= 30;
        double o = 0, e = 0;
        for (int m = 1; m < sd.shared.observed.length; m++) {
            o += m * sd.shared.observed[m];
            e += m * sd.shared.expected[m];
        }
        t.detail = T.f("%d days with both draws: %s numbers in common (%s expected).", sd.days, Fmt.num(o), Fmt.num(e));
        t.direction = o > e ? "more" : "fewer";
        out.add(t);
        TestResult id = new TestResult("identical", T.t("Identical results on the same day"),
                T.t("Do both draws ever give exactly the same result on one day, more often than chance?"));
        id.game = sd.pair;
        id.stat = sd.identical;
        id.p = Combi.poissonUpper(sd.expectedIdentical, sd.identical);
        id.enough = sd.days >= 30;
        id.detail = T.f("%d identical (%s expected).", sd.identical, Fmt.num(sd.expectedIdentical));
        id.direction = "identical";
        out.add(id);
        return out;
    }

    /** p-values of the same test on the first and second half of the period, or null if it can't be split. */
    private static double[] replicate(History h, List<Game> games, int days, Sample s, TestResult t) {
        if (t.key.equals("sameday") || t.key.equals("identical")) {
            // split by date: rebuild the pair on each half
            String[] names = t.game.split(" vs ");
            Game a = null, b = null;
            for (Game g : games) {
                if (g.name.equals(names[0])) a = g;
                if (names.length > 1 && g.name.equals(names[1])) b = g;
            }
            if (a == null || b == null) return null;
            List<Game> pair = new ArrayList<>();
            pair.add(a);
            pair.add(b);
            Sample both = h.sample(pair, days);
            if (both.size() < 60) return null;
            LocalDate mid = both.draws.get(both.size() / 2).date;
            double[] out = new double[2];
            for (int half = 0; half < 2; half++) {
                History part = new History();
                for (Draw d : both.draws) if ((half == 0) == d.date.isBefore(mid)) part.put(d);
                List<Analysis.SameDay> sd = Analysis.sameDay(part, pair, 0);
                if (sd.isEmpty()) return null;
                List<TestResult> r = sameDayTests(sd.get(0));
                out[half] = t.key.equals("sameday") ? r.get(0).p : r.get(1).p;
            }
            return out;
        }
        if (s == null || s.size() < 60) return null;
        double[] out = new double[2];
        Sample[] halves = {s.firstHalf(), s.secondHalf()};
        for (int i = 0; i < 2; i++) {
            TestResult again = null;
            if (t.key.equals("popular")) again = popular(halves[i]);
            else for (TestResult x : Randomness.run(halves[i]).tests) if (x.key.equals(t.key)) again = x;
            if (again == null || Double.isNaN(again.p)) return null;
            out[i] = again.p;
        }
        return out;
    }

    private static void dataChecks(Report r, History h, Sample s) {
        Game g = s.game;
        int bad = 0;
        for (Draw d : s.draws) if (!d.valid()) bad++;
        if (bad > 0) r.dataIssues.add(T.f("%s: %d result(s) with impossible values (out of range or a "
                + "repeated ball).", g.name, bad));
        int rejected = 0;
        for (String line : h.rejected) if (line.startsWith(g.name + " ")) rejected++;
        if (rejected > 0) r.dataIssues.add(T.f("%s: %d row(s) in the history couldn't be read.", g.name, rejected));
        List<String> breaks = missingStretches(s);
        if (!breaks.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = Math.max(0, breaks.size() - 4); i < breaks.size(); i++) {
                if (sb.length() > 0) sb.append("; ");
                sb.append(breaks.get(i));
            }
            r.dataIssues.add(breaks.size() == 1
                    ? T.f("%s: no results for %s, when it normally draws.", g.name, sb)
                    : breaks.size() > 4 ? T.f("%s: %d stretches with no results on days it normally draws, the latest: %s.",
                    g.name, breaks.size(), sb)
                    : T.f("%s: %d stretches with no results on days it normally draws: %s.", g.name, breaks.size(), sb));
        }
    }

    /**
     * Days without a result on which the series normally draws. Schedules change over the years (Dorado Noche went
     * from daily to weekends and back), so "normally" is judged locally: the same weekday had a draw in at least 70%
     * of the weeks within five weeks either side. Holidays don't count for series that don't play on them.
     */
    static List<String> missingStretches(Sample s) {
        Game g = s.game;
        List<String> out = new ArrayList<>();
        if (s.size() < 20) return out;
        java.util.Set<LocalDate> have = new java.util.HashSet<>();
        for (Draw d : s.draws) have.add(d.date);
        LocalDate first = s.from(), last = s.to();
        LocalDate runStart = null, runEnd = null;
        int runDays = 0;
        for (LocalDate x = first.plusDays(1); x.isBefore(last); x = x.plusDays(1)) {
            boolean missing = false;
            if (!have.contains(x) && !(Holidays.isHoliday(x) && !g.playsHolidays)) {
                int weeks = 0, drawn = 0;
                for (int w = -5; w <= 5; w++) {
                    if (w == 0) continue;
                    LocalDate y = x.plusWeeks(w);
                    if (y.isBefore(first) || y.isAfter(last) || (Holidays.isHoliday(y) && !g.playsHolidays)) continue;
                    weeks++;
                    if (have.contains(y)) drawn++;
                }
                missing = weeks >= 4 && drawn >= 0.7 * weeks;
            }
            if (missing) {
                if (runStart == null) runStart = x;
                runEnd = x;
                runDays++;
            } else if (runStart != null && ChronoUnit.DAYS.between(runEnd, x) > 3) {
                out.add(stretch(runStart, runEnd, runDays));
                runStart = null;
                runDays = 0;
            }
        }
        if (runStart != null) out.add(stretch(runStart, runEnd, runDays));
        return out;
    }

    private static String stretch(LocalDate a, LocalDate b, int days) {
        return a.equals(b) ? Fmt.shortDate(a) : T.f("%s – %s (%d draw days)", Fmt.shortDate(a), Fmt.shortDate(b), days);
    }
}
