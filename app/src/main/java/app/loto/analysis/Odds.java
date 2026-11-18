package app.loto.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Theoretical probabilities, computed from the rules (never from history): the jackpot, every prize tier, the
 * chance of any one combination, and what playing every draw for a year adds up to. Also reads a combination
 * the user types and measures it against the history, so it can be set beside the suggested pick.
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
            t.add(new Tier(T.t("4 cifras — all four digits in order"), 1e-4, T.t("the main chance bet")));
            t.add(new Tier(T.t("3 cifras — last three digits"), 1e-3, ""));
            t.add(new Tier(T.t("2 cifras — last two digits"), 1e-2, ""));
            t.add(new Tier(T.t("1 cifra — last digit"), 1e-1, ""));
            t.add(new Tier(T.t("Combinado — four different digits, any order"), 24e-4, T.t("24 orders of e.g. 1234")));
            t.add(new Tier(T.t("Combinado — a pair (e.g. 1123), any order"), 12e-4, T.t("12 orders")));
            t.add(new Tier(T.t("Combinado — two pairs (e.g. 1122), any order"), 6e-4, T.t("6 orders")));
            t.add(new Tier(T.t("Combinado — three alike (e.g. 1112), any order"), 4e-4, T.t("4 orders")));
            t.add(new Tier(T.t("4 cifras + quinta (five digits)"), 1e-5, T.t("where the quinta is offered")));
            return t;
        }
        int N = g.max, k = g.count;
        double combos = Combi.choose(N, k);
        if (g.extraMax > 0) {
            int M = g.extraMax;
            for (int m = k; m >= 0; m--) {
                double pm = Combi.choose(k, m) * Combi.choose(N - k, k - m) / combos;
                double with = pm / M, without = pm * (M - 1) / M;
                if (m == k) t.add(new Tier(T.f("%d + %s (jackpot)", m, g.extraName), with, T.t("every number and the superbalota")));
                else t.add(new Tier(T.f("%d + %s", m, g.extraName), with, m == 0 ? T.t("the superbalota alone") : ""));
                if (m >= 3) t.add(new Tier(T.f("%d numbers", m), without, ""));
            }
            return t;
        }
        for (int m = k; m >= 2; m--) {
            double pm = Combi.choose(k, m) * Combi.choose(N - k, k - m) / combos;
            t.add(new Tier(m == k ? T.f("%d numbers (jackpot)", m) : T.f("%d numbers", m), pm, ""));
        }
        return t;
    }

    static double jackpot(Game g) {
        return 1 / g.combinations();
    }

    /** Probability of any prize at all (lotto); for chance the cheapest bet, the last digit. */
    static double anyPrize(Game g) {
        if (g.digits()) return 0.1;
        double s = 0;
        for (Tier x : tiers(g)) s += x.p;
        return s;
    }

    /** Chance of winning at least once if you play one ticket in every draw for a year. */
    static double perYear(Game g, double p) {
        return 1 - Math.pow(1 - p, drawsPerYear(g));
    }

    static int drawsPerYear(Game g) {
        return g.drawsPerWeek() * 52;
    }

    // ================================================================ reading and checking a combination

    static final class Check {
        String error;
        int[] nums;
        int extra = -1;
        /** Probability of this exact combination (with the superbalota if one was given). */
        double theory;
        int draws;
        /** Times it came up exactly. */
        int exact;
        /** Chance: last three / last two digits seen, and draws with the same digits in any order. */
        int lastThree, lastTwo, anyOrder;
        /** Lotto: draws matching m of its numbers, m = 0..count. */
        int[] matched;
        int extraSeen = -1;
        /** How common its shape is among fair draws (even/odd split; for chance the repeated-digit pattern). */
        double shapeShare;
    }

    /** Reads "2820" or "5 13 19 25 29 + 7". */
    static Check read(Game g, String input) {
        Check c = new Check();
        String in = input == null ? "" : input.trim();
        if (g.digits()) {
            String digits = in.replaceAll("[^0-9]", "");
            if (digits.length() != g.count) {
                c.error = T.t("Type a 4-digit number, like 2820.");
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
            List<Integer> vals = new ArrayList<>();
            for (String p : main.trim().split("[^0-9]+")) {
                if (p.isEmpty()) continue;
                try {
                    vals.add(Integer.parseInt(p));
                } catch (NumberFormatException e) {
                    vals.add(-1);
                }
            }
            if (vals.size() != g.count) {
                c.error = g.extraMax > 0
                        ? T.f("Type %d different numbers from 1 to %d, then + and the superbalota (1–%d).", g.count, g.max, g.extraMax)
                        : T.f("Type %d different numbers from 1 to %d.", g.count, g.max);
                return c;
            }
            c.nums = new int[g.count];
            for (int i = 0; i < g.count; i++) c.nums[i] = vals.get(i);
            java.util.Arrays.sort(c.nums);
            for (int i = 0; i < g.count; i++) {
                if (c.nums[i] < 1 || c.nums[i] > g.max || (i > 0 && c.nums[i] == c.nums[i - 1])) {
                    c.error = T.f("Numbers must be different and between 1 and %d.", g.max);
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
                    c.error = T.f("The superbalota must be between 1 and %d.", g.extraMax);
                    return c;
                }
            }
        }
        return c;
    }

    /** Reads the input and measures it against the history. */
    static Check check(Game g, Sample s, String input) {
        Check c = read(g, input);
        if (c.error != null) return c;
        measure(g, s, c);
        return c;
    }

    /** Fills in the history and probability figures of an already-read combination. */
    static void measure(Game g, Sample s, Check c) {
        c.theory = g.digits() ? 1e-4 : 1 / (Combi.choose(g.max, g.count) * (g.extraMax > 0 && c.extra > 0 ? g.extraMax : 1));
        c.draws = s.size();
        Draw probe = new Draw(g, null, "", c.nums, c.extra);
        if (g.digits()) {
            String sortedProbe = sortedDigits(c.nums);
            for (Draw d : s.draws) {
                if (d.value() == probe.value()) c.exact++;
                if (d.lastThree() == probe.lastThree()) c.lastThree++;
                if (d.lastTwo() == probe.lastTwo()) c.lastTwo++;
                if (sortedDigits(d.nums).equals(sortedProbe)) c.anyOrder++;
            }
            c.shapeShare = Analysis.PATTERN_P[Analysis.pattern(probe)];
        } else {
            c.matched = new int[g.count + 1];
            for (Draw d : s.draws) c.matched[d.shared(probe)]++;
            c.exact = c.matched[g.count];
            if (c.extra > 0) {
                c.extraSeen = 0;
                for (Draw d : s.draws) if (d.extra == c.extra) c.extraSeen++;
            }
            c.shapeShare = Analysis.evenProbabilities(g)[probe.evens()];
        }
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
