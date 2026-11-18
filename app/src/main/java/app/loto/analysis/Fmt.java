package app.loto.analysis;

import java.time.LocalDate;
import java.util.Locale;

/** Text formatting shared by the engine and the screens. */
final class Fmt {
    private Fmt() {}

    static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /** "Tue 17 Nov 2026". */
    static String date(LocalDate d) {
        if (d == null) return "–";
        return Game.DAY_SHORT[d.getDayOfWeek().getValue() - 1] + " " + d.getDayOfMonth() + " "
                + MONTHS[d.getMonthValue() - 1] + " " + d.getYear();
    }

    /** "17 Nov 2026". */
    static String shortDate(LocalDate d) {
        if (d == null) return "–";
        return d.getDayOfMonth() + " " + MONTHS[d.getMonthValue() - 1] + " " + d.getYear();
    }

    /** "Tuesday 17 November 2026"-style heading. */
    static String dayHeading(LocalDate d) {
        String[] full = {"January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December"};
        return Game.DAY_NAMES[d.getDayOfWeek().getValue() - 1] + " " + d.getDayOfMonth() + " " + full[d.getMonthValue() - 1]
                + " " + d.getYear();
    }

    static String num(double v) {
        if (Double.isNaN(v)) return "–";
        if (Math.abs(v - Math.rint(v)) < 1e-9 && Math.abs(v) < 1e12) return String.format(Locale.US, "%,d", (long) Math.rint(v));
        if (Math.abs(v) >= 100) return String.format(Locale.US, "%,.0f", v);
        if (Math.abs(v) >= 10) return String.format(Locale.US, "%.1f", v);
        return String.format(Locale.US, "%.2f", v);
    }

    static String signed(double z) {
        return String.format(Locale.US, "%+.1f", z);
    }

    static String pct(double share) {
        return String.format(Locale.US, "%.1f%%", share * 100);
    }
}
