package app.loto.analysis;

import java.time.LocalDate;

/** Dates, times and numbers in the app's language. */
final class Fmt {
    private Fmt() {}

    private static final String[] DAYS_EN = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final String[] DAYS_ES = {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"};
    private static final String[] DSHORT_EN = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private static final String[] DSHORT_ES = {"lun", "mar", "mié", "jue", "vie", "sáb", "dom"};
    private static final String[] MONTHS_EN = {"January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December"};
    private static final String[] MONTHS_ES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
            "septiembre", "octubre", "noviembre", "diciembre"};
    private static final String[] MSHORT_EN = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static final String[] MSHORT_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};

    /** Day name, Monday = 0. */
    static String day(int i) { return T.es ? DAYS_ES[i] : DAYS_EN[i]; }

    static String dayShort(int i) { return T.es ? DSHORT_ES[i] : DSHORT_EN[i]; }

    static String day(LocalDate d) { return day(d.getDayOfWeek().getValue() - 1); }

    static String monthShort(int m) { return T.es ? MSHORT_ES[m - 1] : MSHORT_EN[m - 1]; }

    /** "Tue 17 Nov 2026" / "mar 17 nov 2026". */
    static String date(LocalDate d) {
        if (d == null) return "–";
        return dayShort(d.getDayOfWeek().getValue() - 1) + " " + d.getDayOfMonth() + " " + monthShort(d.getMonthValue()) + " " + d.getYear();
    }

    /** "17 Nov 2026" / "17 nov 2026". */
    static String shortDate(LocalDate d) {
        if (d == null) return "–";
        return d.getDayOfMonth() + " " + monthShort(d.getMonthValue()) + " " + d.getYear();
    }

    /** "Tuesday 17 November 2026" / "Martes 17 de noviembre de 2026". */
    static String dayHeading(LocalDate d) {
        if (T.es) {
            String s = DAYS_ES[d.getDayOfWeek().getValue() - 1] + " " + d.getDayOfMonth() + " de " + MONTHS_ES[d.getMonthValue() - 1]
                    + " de " + d.getYear();
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
        return DAYS_EN[d.getDayOfWeek().getValue() - 1] + " " + d.getDayOfMonth() + " " + MONTHS_EN[d.getMonthValue() - 1] + " " + d.getYear();
    }

    /** "Tuesday 17 November" / "Martes 17 de noviembre". */
    static String dayMonth(LocalDate d) {
        if (T.es) {
            String s = DAYS_ES[d.getDayOfWeek().getValue() - 1] + " " + d.getDayOfMonth() + " de " + MONTHS_ES[d.getMonthValue() - 1];
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
        return DAYS_EN[d.getDayOfWeek().getValue() - 1] + " " + d.getDayOfMonth() + " " + MONTHS_EN[d.getMonthValue() - 1];
    }

    /** dayMonth for the middle of a sentence: "Tuesday 17 November" / "martes 17 de noviembre". */
    static String dayMonthInline(LocalDate d) {
        if (T.es) return DAYS_ES[d.getDayOfWeek().getValue() - 1] + " " + d.getDayOfMonth() + " de " + MONTHS_ES[d.getMonthValue() - 1];
        return dayMonth(d);
    }

    /** Minutes after midnight as "3:30 p.m." / "3:30 p. m.". */
    static String time(int minutes) {
        if (minutes < 0) return "–";
        int h = minutes / 60, m = minutes % 60;
        boolean pm = h >= 12;
        int h12 = h % 12 == 0 ? 12 : h % 12;
        String suffix = T.es ? (pm ? "p. m." : "a. m.") : (pm ? "p.m." : "a.m.");
        return h12 + ":" + (m < 10 ? "0" : "") + m + " " + suffix;
    }

    static String num(double v) {
        if (Double.isNaN(v)) return "–";
        if (Math.abs(v - Math.rint(v)) < 1e-9 && Math.abs(v) < 1e12) return String.format(T.L, "%,d", (long) Math.rint(v));
        if (Math.abs(v) >= 100) return String.format(T.L, "%,.0f", v);
        if (Math.abs(v) >= 10) return String.format(T.L, "%.1f", v);
        return String.format(T.L, "%.2f", v);
    }

    static String signed(double z) {
        return String.format(T.L, "%+.1f", z);
    }

    static String pct(double share) {
        return String.format(T.L, "%.1f%%", share * 100);
    }
}
