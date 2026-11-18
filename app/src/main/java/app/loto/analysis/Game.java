package app.loto.analysis;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One draw series: a lottery's draw at one time of day (Dorado Mañana, Paisita Noche, Baloto...). Two kinds:
 * DIGITS games (the Colombian chance: a 4-digit number 0000-9999, each digit drawn from its own urn, plus an
 * optional fifth digit, the "quinta") and LOTTO games (5 different balls from 1..max, plus Baloto's superbalota).
 * Plain Java, no Android, so the statistics can be tested on any JVM.
 */
final class Game {
    enum Kind { DIGITS, LOTTO }

    enum Slot {
        MORNING("Morning"), AFTERNOON("Afternoon"), NIGHT("Evening / Night");
        final String label;
        Slot(String label) { this.label = label; }
    }

    static final String[] DAY_NAMES = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    static final String[] DAY_SHORT = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    final String id;          // also the history file name
    final String lottery;     // Dorado, Paisita, Baloto, MiLoto
    final String name;        // Dorado Mañana
    final String shortName;   // Mañana
    final Slot slot;
    final Kind kind;
    final int count;          // digits in the number, or balls drawn
    final int max;            // highest ball (LOTTO) or 9 (DIGITS)
    final int extraMax;       // superbalota 1..16, quinta 0..9, 0 = none
    final String extraName;
    /** Draw time per weekday, Monday first; null where there's no draw. */
    final String[] times;
    /** Whether it also plays on Colombian public holidays (at the Sunday time). */
    final boolean playsHolidays;
    final String source;

    private Game(String id, String lottery, String name, String shortName, Slot slot, Kind kind, int count, int max,
                 int extraMax, String extraName, String[] times, boolean playsHolidays, String source) {
        this.id = id;
        this.lottery = lottery;
        this.name = name;
        this.shortName = shortName;
        this.slot = slot;
        this.kind = kind;
        this.count = count;
        this.max = max;
        this.extraMax = extraMax;
        this.extraName = extraName;
        this.times = times;
        this.playsHolidays = playsHolidays;
        this.source = source;
    }

    private static String[] week(String monSat, String sun) {
        return new String[]{monSat, monSat, monSat, monSat, monSat, monSat, sun};
    }

    private static String[] days(String time, DayOfWeek... on) {
        String[] t = new String[7];
        for (DayOfWeek d : on) t[d.getValue() - 1] = time;
        return t;
    }

    static final String ASTRO = "astroluna.co";
    static final String OFFICIAL = "baloto.com (official)";

    static final Game DORADO_MANANA = new Game("dorado-manana", "Dorado", "Dorado Mañana", "Mañana", Slot.MORNING,
            Kind.DIGITS, 4, 9, 9, "Quinta", week("11:00 a.m.", null), false, ASTRO);
    static final Game DORADO_TARDE = new Game("dorado-tarde", "Dorado", "Dorado Tarde", "Tarde", Slot.AFTERNOON,
            Kind.DIGITS, 4, 9, 9, "Quinta", week("3:30 p.m.", null), false, ASTRO);
    static final Game DORADO_NOCHE = new Game("dorado-noche", "Dorado", "Dorado Noche", "Noche", Slot.NIGHT,
            Kind.DIGITS, 4, 9, 9, "Quinta",
            new String[]{"10:05 p.m.", "10:05 p.m.", "10:05 p.m.", "10:05 p.m.", "10:05 p.m.", "10:15 p.m.", "7:25 p.m."},
            true, ASTRO);
    static final Game PAISITA_DIA = new Game("paisita-dia", "Paisita", "Paisita Día", "Día", Slot.AFTERNOON,
            Kind.DIGITS, 4, 9, 9, "Quinta", week("1:00 p.m.", "2:00 p.m."), true, ASTRO);
    static final Game PAISITA_NOCHE = new Game("paisita-noche", "Paisita", "Paisita Noche", "Noche", Slot.NIGHT,
            Kind.DIGITS, 4, 9, 9, "Quinta", week("6:00 p.m.", "8:00 p.m."), true, ASTRO);
    static final Game PAISITA_3 = new Game("paisita-3", "Paisita", "Paisita 3", "3 (Sat)", Slot.NIGHT,
            Kind.DIGITS, 4, 9, 9, "Quinta", days("10:00 p.m.", DayOfWeek.SATURDAY), false, ASTRO);
    static final Game BALOTO = new Game("baloto", "Baloto", "Baloto", "Baloto", Slot.NIGHT, Kind.LOTTO, 5, 43, 16,
            "Superbalota", days("11:00 p.m.", DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY), true, OFFICIAL);
    static final Game REVANCHA = new Game("revancha", "Baloto", "Baloto Revancha", "Revancha", Slot.NIGHT, Kind.LOTTO, 5,
            43, 16, "Superbalota", days("11:00 p.m.", DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY), true,
            OFFICIAL);
    static final Game MILOTO = new Game("miloto", "MiLoto", "MiLoto", "MiLoto", Slot.NIGHT, Kind.LOTTO, 5, 39, 0, null,
            days("10:00 p.m.", DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY), true, OFFICIAL);

    static final Game[] ALL = {DORADO_MANANA, DORADO_TARDE, DORADO_NOCHE, PAISITA_DIA, PAISITA_NOCHE, PAISITA_3,
            BALOTO, REVANCHA, MILOTO};
    static final String[] LOTTERIES = {"Dorado", "Paisita", "Baloto", "MiLoto"};

    static List<Game> of(String lottery) {
        List<Game> out = new ArrayList<>();
        for (Game g : ALL) if (g.lottery.equals(lottery)) out.add(g);
        return Collections.unmodifiableList(out);
    }

    static Game byId(String id) {
        for (Game g : ALL) if (g.id.equals(id)) return g;
        return null;
    }

    boolean digits() { return kind == Kind.DIGITS; }

    /** The draw time on that date, or null if there's no draw (holidays included). */
    String timeOn(LocalDate date) {
        boolean holiday = Holidays.isHoliday(date);
        if (holiday && !playsHolidays) return null;
        int dow = date.getDayOfWeek().getValue() - 1;
        if (holiday && dow < 6) return times[6] != null ? times[6] : times[dow];
        return times[dow];
    }

    /** The longest regular wait between two draws in days (Saturday to Monday is 2). */
    int longestRegularGap() {
        int worst = 1;
        for (int d = 0; d < 7; d++) {
            if (times[d] == null) continue;
            int gap = 1;
            while (times[(d + gap) % 7] == null) gap++;
            worst = Math.max(worst, gap);
        }
        return worst;
    }

    /** "Mon–Sat 11:00 a.m." style summary of the schedule. */
    String scheduleText() {
        StringBuilder sb = new StringBuilder();
        int d = 0;
        while (d < 7) {
            if (times[d] == null) { d++; continue; }
            int e = d;
            while (e + 1 < 7 && times[d].equals(times[e + 1])) e++;
            if (sb.length() > 0) sb.append(" · ");
            if (e == d) sb.append(DAY_SHORT[d]);
            else if (e == d + 1) sb.append(DAY_SHORT[d]).append(", ").append(DAY_SHORT[e]);
            else sb.append(DAY_SHORT[d]).append("–").append(DAY_SHORT[e]);
            sb.append(' ').append(times[d]);
            d = e + 1;
        }
        // non-contiguous single days with the same time read better grouped: "Mon, Wed, Sat 11:00 p.m."
        String s = sb.toString();
        String[] parts = s.split(" · ");
        if (parts.length > 1) {
            String time = null;
            boolean same = true;
            StringBuilder ds = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                if (times[i] == null) continue;
                if (time == null) time = times[i];
                else if (!time.equals(times[i])) same = false;
                if (ds.length() > 0) ds.append(", ");
                ds.append(DAY_SHORT[i]);
            }
            if (same && time != null) s = ds + " " + time;
        }
        if (playsHolidays && times[6] != null) s += " (holidays: Sunday time)";
        else if (!playsHolidays) s += " (no draw on public holidays)";
        return s;
    }

    /** Total number of possible results: 10,000 for chance, C(max, count) × extra for lotto. */
    double combinations() {
        if (digits()) return Math.pow(10, count);
        double c = Combi.choose(max, count);
        return extraMax > 0 ? c * extraMax : c;
    }

    /** Number of possible values for each drawn number. */
    int values() { return digits() ? 10 : max; }

    int firstValue() { return digits() ? 0 : 1; }
}
