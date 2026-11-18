package app.loto.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Theoretical probabilities, computed from the rules (never from history): the jackpot, every prize tier, the
 * chance of any one combination, and what playing every draw for a year adds up to. Also checks a combination
 * the user types against the history.
 */
final class Odds {
    private Odds() {}

    static final class Tier {
        final String name;
        final double p;
        final String note;
        Tier(String name, double p, String note) {
            this.name = name;
            this.p = p;
            this.note = note;
        }
    }

    /** Prize tiers of one game, from the jackpot down. */
    static List<Tier> tiers(Game g) {
        List<Tier> t = new ArrayList<>();
        if (g.digits()) {
            t.add(new Tier("4 cifras — all four digits in order", 1e-4, "the main chance bet"));
            t.add(new Tier("3 cifras — last three digits", 1e-3, ""));
            t.add(new Tier("2 cifras — last two digits", 1e-2, ""));
            t.add(new Tier("1 cifra — last digit", 1e-1, ""));
            t.add(new Tier("Combinado — four different digits, any order", 24e-4, "24 orders of e.g. 1234"));
            t.add(new Tier("Combinado — a pair (e.g. 1123), any order", 12e-4, "12 orders"));
            t.add(new Tier("Combinado — two pairs (e.g. 1122), any order", 6e-4, "6 orders"));
            t.add(new Tier("Combinado — three alike (e.g. 1112), any order", 4e-4, "4 orders"));
            t.add(new Tier("4 cifras + quinta (five digits)", 1e-5, "where the quinta is offered"));
            return t;
        }
        int N = g.max, k = g.count;
        double combos = Combi.choose(N, k);
        if (g.extraMax > 0) {
            int M = g.extraMax;
            for (int m = k; m >= 0; m--) {
                double pm = Combi.choose(k, m) * Combi.choose(N - k, k - m) / combos;
                double with = pm / M, without = pm * (M - 1) / M;
                String label = m + " + " + g.extraName;
                if (m == k) t.add(new Tier(label + " (jackpot)", with, "every number and the " + g.extraName.toLowerCase(Locale.US)));
                else t.add(new Tier(label, with, m == 0 ? "the " + g.extraName.toLowerCase(Locale.US) + " alone" : ""));
                if (m >= 3) t.add(new Tier(m + " numbers", without, ""));
            }
            return t;
        }
        for (int m = k; m >= 2; m--) {
            double pm = Combi.choose(k, m) * Combi.choose(N - k, k - m) / combos;
            t.add(new Tier(m + " numbers" + (m == k ? " (jackpot)" : ""), pm, ""));
        }
        return t;
    }

    static double jackpot(Game g) {
        return 1 / g.combinations();
    }

    /** Probability of any prize at all. */
    static double anyPrize(Game g) {
        double s = 0;
        List<Tier> t = tiers(g);
        if (g.digits()) return 0.1; // the 1-cifra bet covers every ticket that wins anything on the main number
        for (Tier x : t) s += x.p;
        return s;
    }

    /** Chance of winning at least once if you play one ticket in every draw for a year. */
    static double perYear(Game g, double p) {
        int perWeek = 0;
        for (String time : g.times) if (time != null) perWeek++;
        int draws = perWeek * 52;
        return 1 - Math.pow(1 - p, draws);
    }

    static int drawsPerYear(Game g) {
        int perWeek = 0;
        for (String time : g.times) if (time != null) perWeek++;
        return perWeek * 52;
    }

    // ================================================================ checking a combination

    static final class Check {
        String error;
        int[] nums;
        int extra = -1;
        double theory;
        int exact;
        final List<String> lines = new ArrayList<>();
    }

    /** Reads "2820" or "5 13 19 25 29 + 7" and compares it with the history. */
    static Check check(Game g, Sample s, String input) {
        Check c = new Check();
        String in = input == null ? "" : input.trim();
        if (g.digits()) {
            String digits = in.replaceAll("[^0-9]", "");
            if (digits.length() != g.count) {
                c.error = "Type a " + g.count + "-digit number, like 2820.";
                return c;
            }
            c.nums = new int[g.count];
            for (int i = 0; i < g.count; i++) c.nums[i] = digits.charAt(i) - '0';
        } else {
            String main = in, ex = null;
            int plus = in.indexOf('+');
            if (plus >= 0) {
                main = in.substring(0, plus);
                ex = in.substring(plus + 1);
            }
            String[] parts = main.trim().split("[^0-9]+");
            List<Integer> vals = new ArrayList<>();
            for (String p : parts) if (!p.isEmpty()) vals.add(Integer.parseInt(p));
            if (vals.size() != g.count) {
                c.error = String.format(Locale.US, "Type %d different numbers from 1 to %d%s.", g.count, g.max,
                        g.extraMax > 0 ? ", then + and the " + g.extraName.toLowerCase(Locale.US) + " (1–" + g.extraMax + ")" : "");
                return c;
            }
            c.nums = new int[g.count];
            for (int i = 0; i < g.count; i++) c.nums[i] = vals.get(i);
            java.util.Arrays.sort(c.nums);
            for (int i = 0; i < g.count; i++) {
                if (c.nums[i] < 1 || c.nums[i] > g.max || (i > 0 && c.nums[i] == c.nums[i - 1])) {
                    c.error = String.format(Locale.US, "Numbers must be different and between 1 and %d.", g.max);
                    return c;
                }
            }
            if (ex != null && g.extraMax > 0) {
                try {
                    c.extra = Integer.parseInt(ex.trim());
                } catch (NumberFormatException e) {
                    c.extra = -1;
                }
                if (c.extra < 1 || c.extra > g.extraMax) {
                    c.error = g.extraName + " must be between 1 and " + g.extraMax + ".";
                    return c;
                }
            }
        }
        c.theory = Predictor.theory(g);
        if (g.extraMax > 0 && !g.digits() && c.extra < 0) c.theory *= g.extraMax; // without the superbalota: main numbers only
        Draw probe = new Draw(g, null, "", c.nums, c.extra);
        c.lines.add("Theoretical probability: " + Combi.oneIn(c.theory) + " (" + Combi.percent(c.theory)
                + "), exactly the same as every other combination.");
        int D = s.size();
        if (g.digits()) {
            int exact = 0, three = 0, two = 0, perm = 0;
            String sortedProbe = sortedDigits(c.nums);
            for (Draw d : s.draws) {
                if (d.value() == probe.value()) exact++;
                if (d.lastThree() == probe.lastThree()) three++;
                if (d.lastTwo() == probe.lastTwo()) two++;
                if (sortedDigits(d.nums).equals(sortedProbe)) perm++;
            }
            c.exact = exact;
            c.lines.add(String.format(Locale.US, "In %d draws: exactly %s %d times (%s expected); last three %03d: %d (%s); "
                            + "last two %02d: %d (%s); same digits in any order: %d.", D, probe.numbersText(), exact,
                    Fmt.num(D / 10000.0), probe.lastThree(), three, Fmt.num(D / 1000.0), probe.lastTwo(), two,
                    Fmt.num(D / 100.0), perm));
            int pat = Analysis.pattern(probe);
            c.lines.add("Shape: " + Analysis.PATTERNS[pat] + ", which " + Combi.percent(Analysis.PATTERN_P[pat])
                    + " of all draws have. Combinado (any order) would win with " + permutations(c.nums) + " of the 10,000 numbers ("
                    + Combi.oneIn(permutations(c.nums) / 10000.0) + ").");
        } else {
            int[] best = new int[g.count + 1];
            for (Draw d : s.draws) best[d.shared(probe)]++;
            c.exact = best[g.count];
            StringBuilder sb = new StringBuilder(String.format(Locale.US, "In %d draws it would have matched: ", D));
            for (int m = g.count; m >= 2; m--) {
                double e = D * Combi.hypergeom(g.max, g.count, g.count, m);
                sb.append(String.format(Locale.US, "%d numbers %d× (%s expected)%s", m, best[m], Fmt.num(e), m > 2 ? "; " : "."));
            }
            c.lines.add(sb.toString());
            if (c.extra > 0) {
                int sb2 = 0, both = 0;
                for (Draw d : s.draws) {
                    if (d.extra == c.extra) {
                        sb2++;
                        if (d.shared(probe) == g.count) both++;
                    }
                }
                c.lines.add(String.format(Locale.US, "%s %02d came up %d times (%s expected). The full combination with it: %d times.",
                        g.extraName, c.extra, sb2, Fmt.num(s.withExtra().size() / (double) g.extraMax), both));
            }
        }
        c.lines.add(Predictor.shape(g, c.nums));
        return c;
    }

    private static String sortedDigits(int[] n) {
        int[] c = n.clone();
        java.util.Arrays.sort(c);
        StringBuilder sb = new StringBuilder();
        for (int v : c) sb.append(v);
        return sb.toString();
    }

    /** Distinct orderings of the digits (24 for 1234, 12 for 1123...). */
    static int permutations(int[] n) {
        int[] cnt = new int[10];
        for (int v : n) cnt[v]++;
        int r = 24;
        for (int v : cnt) for (int i = 2; i <= v; i++) r /= i;
        return r;
    }
}
