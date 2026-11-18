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
        MORNING, AFTERNOON, NIGHT;

        String label() {
            switch (this) {
                case MORNING: return T.t("Morning");
                case AFTERNOON: return T.t("Afternoon");
                default: return T.t("Evening / Night");
            }
        }
    }

    final String id;          // also the history file name
    final String lottery;     // Dorado, Paisita, Baloto, MiLoto
    final String name;        // Dorado Mañana
    private final String shortName;
    final Slot slot;
    final Kind kind;
    final int count;          // digits in the number, or balls drawn
    final int max;            // highest ball (LOTTO) or 9 (DIGITS)
    final int extraMax;       // superbalota 1..16, quinta 0..9, 0 = none
    final String extraName;
    /** Draw time per weekday in minutes after midnight, Monday first; -1 where there's no draw. */
    final int[] times;
    /** Whether it also plays on Colombian public holidays (at the Sunday time when it has one). */
    final boolean playsHolidays;
    final String source;

    private Game(String id, String lottery, String name, String shortName, Slot slot, Kind kind, int count, int max,
                 int extraMax, String extraName, int[] times, boolean playsHolidays, String source) {
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

    static int hm(int h, int m) { return h * 60 + m; }

    private static int[] week(int monSat, int sun) {
        return new int[]{monSat, monSat, monSat, monSat, monSat, monSat, sun};
    }

    private static int[] days(int time, DayOfWeek... on) {
        int[] t = {-1, -1, -1, -1, -1, -1, -1};
        for (DayOfWeek d : on) t[d.getValue() - 1] = time;
        return t;
    }

    static final String ASTRO = "astroluna.co";
    static final String OFFICIAL = "baloto.com";

    static final Game DORADO_MANANA = new Game("dorado-manana", "Dorado", "Dorado Mañana", "Mañana", Slot.MORNING,
            Kind.DIGITS, 4, 9, 9, "Quinta", week(hm(11, 0), -1), false, ASTRO);
    static final Game DORADO_TARDE = new Game("dorado-tarde", "Dorado", "Dorado Tarde", "Tarde", Slot.AFTERNOON,
            Kind.DIGITS, 4, 9, 9, "Quinta", week(hm(15, 30), -1), false, ASTRO);
    static final Game DORADO_NOCHE = new Game("dorado-noche", "Dorado", "Dorado Noche", "Noche", Slot.NIGHT,
            Kind.DIGITS, 4, 9, 9, "Quinta",
            new int[]{hm(22, 5), hm(22, 5), hm(22, 5), hm(22, 5), hm(22, 5), hm(22, 15), hm(19, 25)}, true, ASTRO);
    static final Game PAISITA_DIA = new Game("paisita-dia", "Paisita", "Paisita Día", "Día", Slot.AFTERNOON,
            Kind.DIGITS, 4, 9, 9, "Quinta", week(hm(13, 0), hm(14, 0)), true, ASTRO);
    static final Game PAISITA_NOCHE = new Game("paisita-noche", "Paisita", "Paisita Noche", "Noche", Slot.NIGHT,
            Kind.DIGITS, 4, 9, 9, "Quinta", week(hm(18, 0), hm(20, 0)), true, ASTRO);
    static final Game PAISITA_3 = new Game("paisita-3", "Paisita", "Paisita 3", "3", Slot.NIGHT,
            Kind.DIGITS, 4, 9, 9, "Quinta", days(hm(22, 0), DayOfWeek.SATURDAY), false, ASTRO);
    static final Game BALOTO = new Game("baloto", "Baloto", "Baloto", "Baloto", Slot.NIGHT, Kind.LOTTO, 5, 43, 16,
            "Superbalota", days(hm(23, 0), DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY), true, OFFICIAL);
    static final Game REVANCHA = new Game("revancha", "Baloto", "Baloto Revancha", "Revancha", Slot.NIGHT, Kind.LOTTO, 5,
            43, 16, "Superbalota", days(hm(23, 0), DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY), true,
            OFFICIAL);
    static final Game MILOTO = new Game("miloto", "MiLoto", "MiLoto", "MiLoto", Slot.NIGHT, Kind.LOTTO, 5, 39, 0, null,
            days(hm(22, 0), DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY), true, OFFICIAL);

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

    /** "Mañana", "Noche", "Revancha"; Paisita 3 is "3 (Sat)" / "3 (sáb)". */
    String shortName() {
        return this == PAISITA_3 ? T.f("3 (%s)", Fmt.dayShort(5)) : shortName;
    }

    /** The draw time on that date in minutes after midnight, or -1 if there's no draw (holidays included). */
    int timeOn(LocalDate date) {
        boolean holiday = Holidays.isHoliday(date);
        if (holiday && !playsHolidays) return -1;
        int dow = date.getDayOfWeek().getValue() - 1;
        if (holiday && dow < 6 && times[dow] >= 0) return times[6] >= 0 ? times[6] : times[dow];
        return times[dow];
    }

    /** The usual time on that weekday (ignoring holidays), or -1. */
    int usualTime(LocalDate date) { return times[date.getDayOfWeek().getValue() - 1]; }

    /** The longest regular wait between two draws in days (Saturday to Monday is 2). */
    int longestRegularGap() {
        int worst = 1;
        for (int d = 0; d < 7; d++) {
            if (times[d] < 0) continue;
            int gap = 1;
            while (times[(d + gap) % 7] < 0) gap++;
            worst = Math.max(worst, gap);
        }
        return worst;
    }

    int drawsPerWeek() {
        int n = 0;
        for (int t : times) if (t >= 0) n++;
        return n;
    }

    /** "Mon–Sat 11:00 a.m." style summary of the schedule, in the app's language. */
    String scheduleText() {
        List<String> parts = new ArrayList<>();
        int d = 0;
        while (d < 7) {
            if (times[d] < 0) { d++; continue; }
            int e = d;
            while (e + 1 < 7 && times[e + 1] == times[d]) e++;
            String days;
            if (e == d) days = Fmt.dayShort(d);
            else if (e == d + 1) days = Fmt.dayShort(d) + ", " + Fmt.dayShort(e);
            else days = Fmt.dayShort(d) + "–" + Fmt.dayShort(e);
            parts.add(days + " " + Fmt.time(times[d]));
            d = e + 1;
        }
        String s = String.join(" · ", parts);
        if (parts.size() > 1) {
            // non-contiguous days at one time read better grouped: "Mon, Wed, Sat 11:00 p.m."
            int time = -2;
            boolean same = true;
            List<String> ds = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (times[i] < 0) continue;
                if (time == -2) time = times[i];
                else if (time != times[i]) same = false;
                ds.add(Fmt.dayShort(i));
            }
            if (same) s = String.join(", ", ds) + " " + Fmt.time(time);
        }
        if (playsHolidays && times[6] >= 0 && times[6] != times[0]) s += " " + T.t("(holidays: Sunday time)");
        else if (!playsHolidays) s += " " + T.t("(no draw on public holidays)");
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
}
