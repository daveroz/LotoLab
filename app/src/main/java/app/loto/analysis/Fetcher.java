package app.loto.analysis;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Brings the history up to date. Three kinds of source, all free, no key, no account:
 * 1. This repository's own copy of the full history (refreshed twice a day by a GitHub Action; HEAD is the
 *    default branch, whatever its name), so a phone that was offline for weeks still gets every draw it missed.
 * 2. The newest results straight from the sites: astroluna.co for the chance draws (last 100, with the quinta),
 *    loteriasdehoy.co if that fails, and baloto.com, the official site, for Baloto, Revancha and MiLoto.
 * 3. Whatever is already stored stays: results are only ever added or completed, never removed.
 * Every fetch reports the draws that weren't stored before, so they can be announced.
 */
final class Fetcher {
    private Fetcher() {}

    static final String REPO = "https://raw.githubusercontent.com/daveroz/LotoLab/HEAD/app/src/main/assets/history/";
    private static final Map<String, String> ASTRO = new HashMap<>();
    static {
        ASTRO.put("dorado-manana", "dorado-manana");
        ASTRO.put("dorado-tarde", "dorado-tarde");
        ASTRO.put("dorado-noche", "dorado-noche");
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

    /** What a fetch did: one line per source for the status card, and the draws that are new. */
    static final class Status {
        final List<String[]> lines = new ArrayList<>(); // {name, ok/fail, detail}
        final List<Draw> fresh = new ArrayList<>();
        int added;
    }

    /** Everything: the repository copy (at most every 3 hours unless `everything`) and every site. */
    static Status refresh(Context c, History h, boolean everything) {
        Status st = new Status();
        SharedPreferences sp = Data.sp(c);
        long now = System.currentTimeMillis();
        if (everything || now - sp.getLong("repoAt", 0) > 3 * 3600_000L) {
            int added = 0, ok = 0;
            String err = null;
            for (Game g : Game.ALL) {
                try {
                    List<Draw> before = h.all(g);
                    added += h.addCsv(g, Net.get(REPO + g.id + ".csv"));
                    collectNew(h, g, before, st.fresh);
                    ok++;
                } catch (Exception e) {
                    err = e.getMessage();
                }
            }
            if (ok > 0) sp.edit().putLong("repoAt", now).apply();
            st.added += added;
            st.lines.add(new String[]{T.t("Full history (GitHub)"), ok > 0 ? "ok" : "fail",
                    ok > 0 ? T.f("%d new or completed draws, %d of %d files", added, ok, Game.ALL.length) : String.valueOf(err)});
        }
        sites(h, java.util.Arrays.asList(Game.ALL), st);
        finish(c, h, st, true);
        return st;
    }

    /** Only the sites of the given draws (for the background check after a draw). */
    static Status latest(Context c, History h, Collection<Game> games) {
        Status st = new Status();
        sites(h, games, st);
        finish(c, h, st, false);
        return st;
    }

    private static void finish(Context c, History h, Status st, boolean log) {
        if (st.added > 0) Data.save(c, h);
        SharedPreferences.Editor e = Data.sp(c).edit().putLong("fetchAt", System.currentTimeMillis());
        if (log) {
            StringBuilder sb = new StringBuilder();
            for (String[] l : st.lines) sb.append(l[0]).append('\t').append(l[1]).append('\t').append(l[2]).append('\n');
            e.putString("fetchLog", sb.toString());
        }
        e.apply();
    }

    private static void collectNew(History h, Game g, List<Draw> before, List<Draw> fresh) {
        java.util.Set<LocalDate> had = new java.util.HashSet<>();
        for (Draw d : before) had.add(d.date);
        for (Draw d : h.all(g)) if (!had.contains(d.date)) fresh.add(d);
    }

    private static void sites(History h, Collection<Game> games, Status st) {
        boolean baloto = false, miloto = false;
        for (Game g : games) {
            if (g == Game.BALOTO || g == Game.REVANCHA) baloto = true;
            else if (g == Game.MILOTO) miloto = true;
            else chance(h, g, st);
        }
        if (baloto) baloto(h, st);
        if (miloto) miloto(h, st);
    }

    private static void chance(History h, Game g, Status st) {
        int added = 0;
        String src = "astroluna.co", err = null;
        try {
            added = parseChance(h, g, Net.get("https://www.astroluna.co/" + ASTRO.get(g.id)), ASTRO_ROW, st.fresh);
            if (added < 0) throw new IllegalStateException(T.t("no results on the page"));
        } catch (Exception e) {
            err = e.getMessage();
            try {
                src = "loteriasdehoy.co";
                added = parseChance(h, g, Net.get("https://loteriasdehoy.co/" + g.id), LDH_ROW, st.fresh);
                if (added < 0) throw new IllegalStateException(T.t("no results on the page"));
                err = null;
            } catch (Exception e2) {
                err = err + "; " + e2.getMessage();
            }
        }
        if (added > 0) st.added += added;
        st.lines.add(new String[]{g.name, err == null ? "ok" : "fail", err == null ? T.f("%d new · %s", Math.max(0, added), src) : String.valueOf(err)});
    }

    private static void baloto(History h, Status st) {
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
                    if (d == null) continue;
                    boolean isNew = h.on(g, d.date) == null;
                    if (h.put(d)) {
                        n++;
                        if (isNew) st.fresh.add(d);
                    }
                }
                if (rows == 0 && page == 1) throw new IllegalStateException(T.t("no results on the page"));
                added += n;
                if (n == 0) break;
            }
            st.added += added;
            st.lines.add(new String[]{"Baloto + Revancha", "ok", T.f("%d new · %s", added, "baloto.com")});
        } catch (Exception e) {
            st.lines.add(new String[]{"Baloto + Revancha", "fail", String.valueOf(e.getMessage())});
        }
    }

    private static void miloto(History h, Status st) {
        try {
            int added = 0;
            for (int page = 1; page <= 20; page++) {
                String html = Net.get("https://www.baloto.com/miloto/resultados/?page=" + page);
                int n = 0, rows = 0;
                Matcher m = MILOTO_ROW.matcher(html);
                while (m.find()) {
                    rows++;
                    Draw d = Draw.parse(Game.MILOTO, date(m.group(1), m.group(2), m.group(3)), m.group(5), balls(m.group(4)), "");
                    if (d == null) continue;
                    boolean isNew = h.on(Game.MILOTO, d.date) == null;
                    if (h.put(d)) {
                        n++;
                        if (isNew) st.fresh.add(d);
                    }
                }
                if (rows == 0 && page == 1) throw new IllegalStateException(T.t("no results on the page"));
                added += n;
                if (n == 0) break;
            }
            st.added += added;
            st.lines.add(new String[]{"MiLoto", "ok", T.f("%d new · %s", added, "baloto.com")});
        } catch (Exception e) {
            st.lines.add(new String[]{"MiLoto", "fail", String.valueOf(e.getMessage())});
        }
    }

    /** Returns the number of new or completed draws, or -1 if the page had no result rows at all. */
    private static int parseChance(History h, Game g, String html, Pattern row, List<Draw> fresh) {
        Matcher m = row.matcher(html);
        int rows = 0, added = 0;
        while (m.find()) {
            rows++;
            try {
                Draw d = Draw.parse(g, date(m.group(1), m.group(2), m.group(3)), "", m.group(4),
                        m.group(5) == null ? "" : m.group(5));
                if (d == null) continue;
                boolean isNew = h.on(g, d.date) == null;
                if (h.put(d)) {
                    added++;
                    if (isNew) fresh.add(d);
                }
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
