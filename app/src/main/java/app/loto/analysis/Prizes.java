package app.loto.analysis;

/**
 * El Dorado's bets and prizes, from the official chance prize plan:
 * Decreto 1068 de 2015 (Ministerio de Hacienda, as compiled by the DIAN), modified by Decreto 1390 de 2024 —
 * per peso bet: 4 digits in order $4.500, 3 digits in order $400, 2 digits in order (la pata) $50, the last
 * digit (la uña) $5, 4 digits in any order $208, 3 digits in any order $83; only the highest-value hit is paid.
 * The digits are the last four, three, two or one of the result.
 *
 * The prize is paid on the bet value before IVA (19%): a ticket paid at $1.000 is a bet of $840,34.
 * Prizes above 48 UVT have 20% withheld at payment (Estatuto Tributario arts. 306, 317, 402, 404-1); the
 * UVT is $52.374 in 2026.
 */
final class Prizes {
    private Prizes() {}

    enum Bet {
        FOUR(4, 4500, false), THREE(3, 400, false), TWO(2, 50, false), ONE(1, 5, false),
        FOUR_ANY(4, 208, true), THREE_ANY(3, 83, true);

        final int digits;
        final double multiplier;
        final boolean anyOrder;

        Bet(int digits, double multiplier, boolean anyOrder) {
            this.digits = digits;
            this.multiplier = multiplier;
            this.anyOrder = anyOrder;
        }

        String label() {
            switch (this) {
                case FOUR: return T.t("4 cifras");
                case THREE: return T.t("3 cifras");
                case TWO: return T.t("Pata (2 cifras)");
                case ONE: return T.t("Uña (1 cifra)");
                case FOUR_ANY: return T.t("4 cifras combinado");
                default: return T.t("3 cifras combinado");
            }
        }

        /** The name on a chip. */
        String shortLabel() {
            switch (this) {
                case FOUR: case FOUR_ANY: return T.t("4 cifras");
                case THREE: case THREE_ANY: return T.t("3 cifras");
                case TWO: return T.t("Pata");
                default: return T.t("Uña");
            }
        }

        /** What has to come out for it to win. */
        String rule() {
            switch (this) {
                case FOUR: return T.t("All four digits, in order.");
                case THREE: return T.t("The last three digits, in order.");
                case TWO: return T.t("The last two digits, in order (la pata).");
                case ONE: return T.t("The last digit (la uña).");
                case FOUR_ANY: return T.t("The four digits in any order.");
                default: return T.t("The last three digits in any order.");
            }
        }
    }

    static final double IVA = 0.19, WITHHOLDING = 0.20;
    static final int WITHHOLDING_UVT = 48;

    /** The UVT of a year (DIAN): 2025 $49.799, 2026 $52.374; later years use the latest known until updated. */
    static double uvt(int year) {
        if (year <= 2025) return 49799;
        return 52374;
    }

    static double threshold(int year) { return WITHHOLDING_UVT * uvt(year); }

    static final class Payout {
        double paid, bet, iva, multiplier, gross, withheld, net;
        boolean taxed;
    }

    /** The prize of a winning bet paid at `paid` pesos (IVA included). */
    static Payout payout(Bet b, double paid, int year) {
        Payout p = new Payout();
        p.paid = paid;
        p.bet = paid / (1 + IVA);
        p.iva = paid - p.bet;
        p.multiplier = b.multiplier;
        p.gross = p.bet * b.multiplier;
        p.taxed = p.gross > threshold(year);
        p.withheld = p.taxed ? p.gross * WITHHOLDING : 0;
        p.net = p.gross - p.withheld;
        return p;
    }

    /** Distinct orders of the chosen digits (24 for 1234, 6 for 123, 12 for 1123...). */
    static int orders(int[] d) {
        int[] c = new int[10];
        for (int v : d) c[v]++;
        long r = 1;
        for (int i = 2; i <= d.length; i++) r *= i;
        for (int v : c) for (int i = 2; i <= v; i++) r /= i;
        return (int) r;
    }

    /** Probability of winning one bet with these digits. */
    static double probability(Bet b, int[] digits) {
        double base = Math.pow(10, -b.digits);
        return b.anyOrder ? orders(digits) * base : base;
    }

    /** Whether the bet wins against a result. `digits` are the player's (as many as the bet takes). */
    static boolean wins(Bet b, int[] digits, Draw result) {
        int n = result.nums.length;
        int[] tail = java.util.Arrays.copyOfRange(result.nums, n - b.digits, n);
        if (!b.anyOrder) return java.util.Arrays.equals(tail, digits);
        int[] x = tail.clone(), y = digits.clone();
        java.util.Arrays.sort(x);
        java.util.Arrays.sort(y);
        return java.util.Arrays.equals(x, y);
    }

    /** Expected return of a bet (net prize times probability, minus what was paid). */
    static double expectedNet(Bet b, int[] digits, double paid, int year) {
        return probability(b, digits) * payout(b, paid, year).net - paid;
    }

    static String cop(double v) {
        return "$" + String.format(T.L, "%,.0f", v);
    }

    static String copCents(double v) {
        return "$" + String.format(T.L, "%,.2f", v);
    }

    /** Official ticket prices (baloto.com, IVA included). */
    static final double BALOTO_PRICE = 6000, REVANCHA_PRICE = 3000, MILOTO_PRICE = 4000;
}
