package app.loto.analysis;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

/** One result. Chance draws keep their digits in order; lotto balls are stored sorted. */
final class Draw implements Comparable<Draw> {
    final Game game;
    final LocalDate date;
    final String drawNo;
    final int[] nums;
    /** Quinta or superbalota; -1 when there is none. */
    final int extra;

    Draw(Game game, LocalDate date, String drawNo, int[] nums, int extra) {
        this.game = game;
        this.date = date;
        this.drawNo = drawNo == null ? "" : drawNo;
        this.nums = nums;
        this.extra = extra;
    }

    /** Parses one CSV row's numbers ("2820" or "02 12 19 25 42"); null if it isn't a valid result for the game. */
    static Draw parse(Game g, LocalDate date, String drawNo, String numbers, String extra) {
        numbers = numbers.trim();
        int[] n;
        if (g.digits()) {
            if (numbers.length() != g.count) return null;
            n = new int[g.count];
            for (int i = 0; i < g.count; i++) {
                char c = numbers.charAt(i);
                if (c < '0' || c > '9') return null;
                n[i] = c - '0';
            }
        } else {
            String[] parts = numbers.split("[\\s\\-]+");
            if (parts.length != g.count) return null;
            n = new int[g.count];
            for (int i = 0; i < g.count; i++) {
                try {
                    n[i] = Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            Arrays.sort(n);
        }
        int x = -1;
        if (extra != null && !extra.trim().isEmpty()) {
            try {
                x = Integer.parseInt(extra.trim());
            } catch (NumberFormatException ignored) { }
        }
        return new Draw(g, date, drawNo, n, x);
    }

    /** Whether every number is in range and lotto balls are all different (a data check). */
    boolean valid() {
        if (game.digits()) {
            for (int v : nums) if (v < 0 || v > 9) return false;
            return extra <= 9;
        }
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] < 1 || nums[i] > game.max) return false;
            if (i > 0 && nums[i] == nums[i - 1]) return false;
        }
        return extra == -1 || (extra >= 1 && extra <= Math.max(1, game.extraMax));
    }

    /** The 4-digit number as an int (chance only). */
    int value() {
        int v = 0;
        for (int d : nums) v = v * 10 + d;
        return v;
    }

    /** Last two digits (chance only). */
    int lastTwo() { return nums[nums.length - 2] * 10 + nums[nums.length - 1]; }

    int lastThree() { return value() % 1000; }

    int sum() {
        int s = 0;
        for (int v : nums) s += v;
        return s;
    }

    int evens() {
        int e = 0;
        for (int v : nums) if (v % 2 == 0) e++;
        return e;
    }

    boolean contains(int v) {
        for (int x : nums) if (x == v) return true;
        return false;
    }

    /** Numbers shared with another lotto draw. */
    int shared(Draw o) {
        int s = 0;
        for (int v : nums) if (o.contains(v)) s++;
        return s;
    }

    /** "2820" / "02 12 19 25 42". */
    String numbersText() {
        if (game.digits()) {
            StringBuilder sb = new StringBuilder();
            for (int d : nums) sb.append(d);
            return sb.toString();
        }
        StringBuilder sb = new StringBuilder();
        for (int v : nums) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(String.format(Locale.US, "%02d", v));
        }
        return sb.toString();
    }

    String extraText() {
        if (extra < 0) return "";
        return game.digits() ? String.valueOf(extra) : String.format(Locale.US, "%02d", extra);
    }

    /** Order inside a day: by the draw's time slot, then by game. */
    int slotOrder() {
        for (int i = 0; i < Game.ALL.length; i++) if (Game.ALL[i] == game) return game.slot.ordinal() * 100 + i;
        return 0;
    }

    @Override
    public int compareTo(Draw o) {
        int c = date.compareTo(o.date);
        return c != 0 ? c : Integer.compare(slotOrder(), o.slotOrder());
    }
}
