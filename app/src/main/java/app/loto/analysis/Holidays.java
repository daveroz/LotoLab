package app.loto.analysis;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Colombian public holidays (Ley 51 de 1983, "Ley Emiliani"): six fixed dates, seven that move to the following
 * Monday, and five tied to Easter (two fixed to it, three moved to Monday). Some draws don't play on holidays and
 * others play at their Sunday time, so the schedule needs them.
 */
final class Holidays {
    private Holidays() {}

    private static final Map<Integer, Set<LocalDate>> CACHE = new HashMap<>();

    static synchronized boolean isHoliday(LocalDate d) {
        Set<LocalDate> year = CACHE.get(d.getYear());
        if (year == null) {
            year = compute(d.getYear());
            CACHE.put(d.getYear(), year);
        }
        return year.contains(d);
    }

    private static Set<LocalDate> compute(int y) {
        Set<LocalDate> s = new HashSet<>();
        // fixed
        s.add(LocalDate.of(y, 1, 1));
        s.add(LocalDate.of(y, 5, 1));
        s.add(LocalDate.of(y, 7, 20));
        s.add(LocalDate.of(y, 8, 7));
        s.add(LocalDate.of(y, 12, 8));
        s.add(LocalDate.of(y, 12, 25));
        // moved to the next Monday
        int[][] moved = {{1, 6}, {3, 19}, {6, 29}, {8, 15}, {10, 12}, {11, 1}, {11, 11}};
        for (int[] md : moved) s.add(monday(LocalDate.of(y, md[0], md[1])));
        LocalDate easter = easter(y);
        s.add(easter.minusDays(3)); // Holy Thursday
        s.add(easter.minusDays(2)); // Good Friday
        s.add(easter.plusDays(43)); // Ascension, moved to Monday
        s.add(easter.plusDays(64)); // Corpus Christi, moved to Monday
        s.add(easter.plusDays(71)); // Sacred Heart, moved to Monday
        return s;
    }

    private static LocalDate monday(LocalDate d) {
        return d.getDayOfWeek() == DayOfWeek.MONDAY ? d : d.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    /** Easter Sunday (anonymous Gregorian algorithm). */
    static LocalDate easter(int y) {
        int a = y % 19, b = y / 100, c = y % 100, d = b / 4, e = b % 4;
        int f = (b + 8) / 25, g = (b - f + 1) / 3, h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4, k = c % 4, l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31, day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(y, month, day);
    }
}
