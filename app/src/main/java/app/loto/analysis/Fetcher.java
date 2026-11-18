package app.loto.analysis;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Brings the history up to date. Three kinds of source, all free, no key, no account:
 * 1. This repository's own copy of the full history (refreshed twice a day by a GitHub Action), so a phone that
 *    was offline for weeks still gets every draw it missed.
 * 2. The newest results straight from the sites: astroluna.co for the chance draws (last 100, with the quinta),
 *    loteriasdehoy.co if that fails, and baloto.com, the official site, for Baloto, Revancha and MiLoto.
 * 3. Whatever is already stored stays: results are only ever added or completed, never removed.
 */
final class Fetcher {
    private Fetcher() {}

    static final String REPO = "https://raw.githubusercontent.com/daveroz/Loto-App/main/app/src/main/assets/history/";
    private static final Map<String, String> ASTRO = new HashMap<>();
    static {
        ASTRO.put("dorado-manana", "dorado-manana");
        ASTRO.put("dorado-tarde", "dorado-tarde");
        ASTRO.put("dorado-noche", "dorado-noche");
        ASTRO.put("paisita-dia", "paisita-dia");
        ASTRO.put("paisita-noche", "paisita-noche");
        ASTRO.put("paisita-3", "paisita-tres");
    }

    private static final Pattern ASTRO_ROW = Pattern.compile("<tr>\\s*<td>\\s*\\S+\\s+(\\d{1,2})\\s+([a-zA-Z]+)\\s+(\\d{4})\\s*</td>\\s*"
            + "<td class=\"td-resultado\">\\s*(\\d{4})\\s*</td>\\s*<td>\\s*(\\d?)\\s*</td>");
    private static final Pattern LDH_ROW = Pattern.compile("<td>\\s*(\\d{1,2})\\s+([A-Za-z]+)\\s+(\\d{4})\\s*</td>\\s*"
            + "<td>\\s*(\\d{4})(?:\\s*-\\s*(\\d))?\\s*</td>");
    private static final Pattern BALOTO_ROW = Pattern.compile("<td class=\"creation-date-results\">\\s*(\\d{1,2}) de ([A-Za-z]+) de (\\d{4})\\s*</td>\\s*"
            + "<td[^>]*>\\s*([\\d\\s\\-]+?)\\s*-?\\s*<span class=\"balota-red-results\">\\s*(\\d+)\\s*</span>\\s*</td>\\s*"
            + "<td>\\s*<a[^>]*href=\"/resultados-(baloto|revancha)/(\\d+)\"", Pattern.DOTALL);
    private static final Pattern MILOTO_ROW = Pattern.compile("<tr>\\s*<td>\\s*(\\d{1,2}) de ([A-Za-z]+) de (\\d{4})\\s*</td>\\s*"
            + "<td class=\"td-results\">(.*?)</td>\\s*<td>\\s*<a[^>]*href=\"/miloto/resultados-miloto/(\\d+)/\"", Pattern.DOTALL);
    private static final Pattern NUM = Pattern.compile("\\d+");

    private static final String[] MONTHS = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
            "septiembre", "octubre", "noviembre", "diciembre"};

    static LocalDate date(String day, String month, String year) {
        String m = month.toLowerCase(Locale.ROOT);
        if (m.equals("setiembre")) m = "septiembre";
        for (int i = 0; i < 12; i++) {
            if (MONTHS[i].equals(m)) return LocalDate.of(Integer.parseInt(year), i + 1, Integer.parseInt(day));
        }
        throw new IllegalArgumentException(month);
    }

    /** One line per source for the status card. */
    static final class Status {
        final List<String[]> lines = new ArrayList<>(); // {name, ok/fail, detail}
        int added;
    }

    /** Fetches everything; safe to call from a background thread. `everything` also re-reads the repository copy. */
    static Status refresh(Context c, History h, boolean everything) {
        Status st = new Status();
        SharedPreferences sp = Data.sp(c);
        long now = System.currentTimeMillis();

        // 1. the repository's full history, at most every 3 hours unless asked
        if (everything || now - sp.getLong("repoAt", 0) > 3 * 3600_000L) {
            int added = 0, ok = 0;
            String err = null;
            for (Game g : Game.ALL) {
                try {
                    added += h.addCsv(g, Net.get(REPO + g.id + ".csv"));
                    ok++;
                } catch (Exception e) {
                    err = e.getMessage();
                }
            }
            if (ok > 0) sp.edit().putLong("repoAt", now).apply();
            st.added += added;
            st.lines.add(new String[]{"Full history (GitHub)", ok > 0 ? "ok" : "fail",
                    ok > 0 ? (added + " new or completed draws, " + ok + " of " + Game.ALL.length + " files") : String.valueOf(err)});
        }

        // 2. chance: astroluna, loteriasdehoy as a fallback
        for (Game g : Game.ALL) {
            if (!g.digits()) continue;
            int added = 0;
            String src = "astroluna.co", err = null;
            try {
                added = parseChance(h, g, Net.get("https://www.astroluna.co/" + ASTRO.get(g.id)), ASTRO_ROW);
                if (added < 0) throw new IllegalStateException("no results on the page");
            } catch (Exception e) {
                err = e.getMessage();
                try {
                    src = "loteriasdehoy.co";
                    added = parseChance(h, g, Net.get("https://loteriasdehoy.co/" + g.id), LDH_ROW);
                    if (added < 0) throw new IllegalStateException("no results on the page");
                    err = null;
                } catch (Exception e2) {
                    err = err + "; " + e2.getMessage();
                }
            }
            if (added > 0) st.added += added;
            st.lines.add(new String[]{g.name, err == null ? "ok" : "fail",
                    err == null ? (added + " new · " + src) : String.valueOf(err)});
        }

        // 3. Baloto + Revancha and MiLoto from the official site: newest page, more while pages bring new draws
        try {
            int added = 0;
            for (int page = 1; page <= 20; page++) {
                String html = Net.get("https://www.baloto.com/resultados?page=" + page);
                int n = 0, rows = 0;
                Matcher m = BALOTO_ROW.matcher(html);
                while (m.find()) {
                    rows++;
                    Game g = m.group(6).equals("baloto") ? Game.BALOTO : Game.REVANCHA;
                    Draw d = Draw.parse(g, date(m.group(1), m.group(2), m.group(3)), m.group(7), balls(m.group(4)), m.group(5));
                    if (d != null && h.put(d)) n++;
                }
                if (rows == 0 && page == 1) throw new IllegalStateException("no results on the page");
                added += n;
                if (n == 0) break;
            }
            st.added += added;
            st.lines.add(new String[]{"Baloto + Revancha", "ok", added + " new · baloto.com"});
        } catch (Exception e) {
            st.lines.add(new String[]{"Baloto + Revancha", "fail", String.valueOf(e.getMessage())});
        }
        try {
            int added = 0;
            for (int page = 1; page <= 20; page++) {
                String html = Net.get("https://www.baloto.com/miloto/resultados/?page=" + page);
                int n = 0, rows = 0;
                Matcher m = MILOTO_ROW.matcher(html);
                while (m.find()) {
                    rows++;
                    Draw d = Draw.parse(Game.MILOTO, date(m.group(1), m.group(2), m.group(3)), m.group(5), balls(m.group(4)), "");
                    if (d != null && h.put(d)) n++;
                }
                if (rows == 0 && page == 1) throw new IllegalStateException("no results on the page");
                added += n;
                if (n == 0) break;
            }
            st.added += added;
            st.lines.add(new String[]{"MiLoto", "ok", added + " new · baloto.com"});
        } catch (Exception e) {
            st.lines.add(new String[]{"MiLoto", "fail", String.valueOf(e.getMessage())});
        }

        if (st.added > 0) Data.save(c, h);
        StringBuilder log = new StringBuilder();
        for (String[] l : st.lines) log.append(l[0]).append('\t').append(l[1]).append('\t').append(l[2]).append('\n');
        sp.edit().putLong("fetchAt", now).putString("fetchLog", log.toString()).apply();
        return st;
    }

    /** Returns the number of new draws, or -1 if the page had no result rows at all. */
    private static int parseChance(History h, Game g, String html, Pattern row) {
        Matcher m = row.matcher(html);
        int rows = 0, added = 0;
        while (m.find()) {
            rows++;
            try {
                Draw d = Draw.parse(g, date(m.group(1), m.group(2), m.group(3)), "", m.group(4),
                        m.group(5) == null ? "" : m.group(5));
                if (d != null && h.put(d)) added++;
            } catch (IllegalArgumentException ignored) { }
        }
        return rows == 0 ? -1 : added;
    }

    private static String balls(String text) {
        StringBuilder sb = new StringBuilder();
        Matcher m = NUM.matcher(text);
        while (m.find()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(String.format(Locale.US, "%02d", Integer.parseInt(m.group())));
        }
        return sb.toString();
    }
}
