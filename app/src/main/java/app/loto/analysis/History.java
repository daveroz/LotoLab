package app.loto.analysis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Every stored result, per game, keyed by date (each series draws at most once a day). Read from and written to
 * the same CSV format as the bundled files (date,draw,numbers,extra), so the bundled history, the copy downloaded
 * from GitHub and the results fetched live all merge the same way.
 */
final class History {
    private final Map<String, TreeMap<LocalDate, Draw>> byGame = new HashMap<>();
    /** Rows that couldn't be read as a valid result: kept so the Integrity tab can report them. */
    final List<String> rejected = new ArrayList<>();

    History() {
        for (Game g : Game.ALL) byGame.put(g.id, new TreeMap<>());
    }

    /** Adds every row of a CSV file; returns how many draws were new or changed. */
    synchronized int addCsv(Game g, String csv) {
        int changed = 0;
        if (csv == null) return 0;
        String[] lines = csv.split("\r?\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("date")) continue;
            String[] f = line.split(",", -1);
            if (f.length < 3) continue;
            LocalDate d;
            try {
                d = LocalDate.parse(f[0].trim());
            } catch (Exception e) {
                continue;
            }
            Draw draw = Draw.parse(g, d, f[1].trim(), f[2], f.length > 3 ? f[3] : "");
            if (draw == null) {
                if (rejected.size() < 50) rejected.add(g.name + " " + line);
                continue;
            }
            if (put(draw)) changed++;
        }
        return changed;
    }

    /** Adds or updates one draw; true if it was new or brought something new (a quinta, a draw number). */
    synchronized boolean put(Draw d) {
        TreeMap<LocalDate, Draw> m = byGame.get(d.game.id);
        Draw old = m.get(d.date);
        if (old == null) {
            m.put(d.date, d);
            return true;
        }
        boolean sameNumbers = old.numbersText().equals(d.numbersText());
        boolean better = !sameNumbers || (old.extra < 0 && d.extra >= 0) || (old.drawNo.isEmpty() && !d.drawNo.isEmpty());
        if (!better) return false;
        int extra = d.extra >= 0 ? d.extra : (sameNumbers ? old.extra : -1);
        String no = !d.drawNo.isEmpty() ? d.drawNo : old.drawNo;
        m.put(d.date, new Draw(d.game, d.date, no, d.nums, extra));
        return true;
    }

    synchronized String toCsv(Game g) {
        StringBuilder sb = new StringBuilder("date,draw,numbers,extra\n");
        for (Draw d : byGame.get(g.id).values()) {
            sb.append(d.date).append(',').append(d.drawNo).append(',').append(d.numbersText()).append(',')
                    .append(d.extraText()).append('\n');
        }
        return sb.toString();
    }

    synchronized List<Draw> all(Game g) {
        return new ArrayList<>(byGame.get(g.id).values());
    }

    synchronized int size(Game g) { return byGame.get(g.id).size(); }

    synchronized LocalDate first(Game g) {
        TreeMap<LocalDate, Draw> m = byGame.get(g.id);
        return m.isEmpty() ? null : m.firstKey();
    }

    synchronized LocalDate last(Game g) {
        TreeMap<LocalDate, Draw> m = byGame.get(g.id);
        return m.isEmpty() ? null : m.lastKey();
    }

    synchronized Draw on(Game g, LocalDate d) { return byGame.get(g.id).get(d); }

    /**
     * The draws of the chosen games within the last `days` days before the newest of them (days <= 0: all),
     * oldest first; draws on the same day are ordered morning to night.
     */
    synchronized Sample sample(List<Game> games, int days) {
        LocalDate end = null;
        for (Game g : games) {
            LocalDate l = last(g);
            if (l != null && (end == null || l.isAfter(end))) end = l;
        }
        List<Draw> out = new ArrayList<>();
        LocalDate start = end == null || days <= 0 ? null : end.minusDays(days - 1);
        for (Game g : games) {
            for (Draw d : byGame.get(g.id).values()) if (start == null || !d.date.isBefore(start)) out.add(d);
        }
        Collections.sort(out);
        return new Sample(games, out);
    }
}
