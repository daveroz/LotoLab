package app.loto.analysis;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Every stored result by date, with its weekday and time, filterable by day of the week, each with its
 * randomness audit (ordinary or unusual, and why). Then how much history is stored and where it comes from.
 */
final class HistoryTab {
    private HistoryTab() {}

    static final class Out {
        /** Newest day first; each day's draws morning to night. */
        final TreeMap<LocalDate, List<Draw>> days = new TreeMap<>(java.util.Collections.reverseOrder());
        final Map<String, Randomness.DrawScore> audits = new HashMap<>();
        int totalDays;
    }

    static String key(Draw d) { return d.game.id + "@" + d.date; }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        TreeMap<LocalDate, List<Draw>> all = new TreeMap<>(java.util.Collections.reverseOrder());
        for (Draw d : s.sample.draws) {
            if (s.weekday >= 0 && d.date.getDayOfWeek().getValue() - 1 != s.weekday) continue;
            List<Draw> l = all.get(d.date);
            if (l == null) all.put(d.date, l = new ArrayList<>());
            l.add(d);
        }
        o.totalDays = all.size();
        int n = 0;
        for (LocalDate d : all.keySet()) {
            if (n++ >= s.limit) break;
            o.days.put(d, all.get(d));
        }
        // audits over each series' whole history, so every draw is compared with the one before it
        for (Game g : s.games) {
            List<Game> one = new ArrayList<>();
            one.add(g);
            for (Randomness.DrawScore ds : Randomness.drawScores(s.h.sample(one, 0))) o.audits.put(key(ds.draw), ds);
        }
        return o;
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;

        LinearLayout res = ui.card(body, T.t("HISTORY"));
        res.addView(ui.note(MainActivity.scope(s)));
        for (Game g : s.games) {
            TextView sch = ui.note(T.f("%s plays %s", g.name, g.scheduleText()));
            sch.setPadding(0, ui.dp(2), 0, 0);
            res.addView(sch);
        }
        LinearLayout wd = ui.chipRow(res, 10);
        for (int i = -1; i < 7; i++) {
            final int idx = i;
            wd.addView(ui.chip(i < 0 ? T.t("Every day") : Fmt.dayShort(i), i == s.weekday, v -> {
                a.weekday = idx;
                a.limit = 40;
                a.rerenderInPlace();
            }), ui.wrap(i < 0 ? 0 : 6));
        }
        ui.reveal(wd, s.weekday + 1);
        if (o.days.isEmpty()) {
            TextView none = ui.body(s.weekday >= 0 ? T.f("No draws on %s in this selection.", Fmt.day(s.weekday))
                    : T.t("No results stored for this selection."));
            none.setPadding(0, ui.dp(12), 0, 0);
            res.addView(none);
        }
        boolean several = s.games.size() > 1;
        for (LocalDate day : o.days.keySet()) {
            TextView h = ui.text(Fmt.dayHeading(day), 13, Theme.uiBold(a), Theme.AMBER_LIGHT);
            h.setPadding(0, ui.dp(16), 0, ui.dp(2));
            res.addView(h);
            for (Draw d : o.days.get(day)) {
                LinearLayout row = ui.hbox();
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, ui.dp(6), 0, ui.dp(2));
                int time = d.game.usualTime(day);
                LinearLayout who = ui.vbox();
                who.addView(ui.text(several ? d.game.shortName() : d.game.name, 14, Theme.uiSemi(a), Theme.TEXT));
                String sub = (time >= 0 ? Fmt.time(time) : d.game.slot.label()) + (d.drawNo.isEmpty() ? "" : " · #" + d.drawNo);
                who.addView(ui.text(sub, Ui.SMALL, Theme.monoMedium(a), Theme.DIM));
                row.addView(who, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(ui.balls(d.game, d.nums, d.extra, Ui.NUM_S));
                res.addView(row);
                Randomness.DrawScore ds = o.audits.get(key(d));
                if (ds != null) {
                    LinearLayout ar = ui.hbox();
                    ar.setGravity(Gravity.CENTER_VERTICAL);
                    ar.setPadding(0, ui.dp(3), 0, ui.dp(4));
                    ar.addView(ui.tag(T.f("%s · %d", ds.label(), ds.score), SummaryTab.auditColor(ds.score)));
                    if (ds.score < 50) {
                        TextView why = ui.note(String.join(" ", ds.reasons));
                        why.setTextSize(Ui.SMALL);
                        why.setPadding(ui.dp(8), 0, 0, 0);
                        ar.addView(why, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    }
                    res.addView(ar);
                }
            }
        }
        if (o.totalDays > o.days.size()) {
            res.addView(ui.secondaryButton(T.f("Show %d more days", Math.min(60, o.totalDays - o.days.size())), v -> {
                a.limit += 60;
                a.rerenderInPlace();
            }), ui.matchWrap(14));
        }
        if (!o.days.isEmpty()) {
            Game g0 = s.games.get(0);
            String key = g0.digits() ? T.t("The blue tile is the quinta (fifth digit), where it was published.")
                    : g0.extraMax > 0 ? T.t("The red ball is the superbalota.") : null;
            String audit = T.t("Audit: how ordinary each draw is, from 100 (completely typical) down; unusual draws are "
                    + "expected now and then (about 3 in 100 fair draws score below 10).");
            TextView k = ui.note(key == null ? audit : key + " " + audit);
            k.setPadding(0, ui.dp(12), 0, 0);
            res.addView(k);
        }

        // ---- coverage ----
        LinearLayout cov = ui.card(body, T.t("HISTORY STORED"));
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(MainActivity.BOGOTA);
        for (Game g : Game.of(s.lottery)) {
            int n = s.h.size(g);
            LocalDate missing = SummaryTab.missing(s.h, g, now);
            cov.addView(ui.kv(g.name, T.f("%s draws", Fmt.num(n)), missing != null ? Theme.AMBER : Theme.TEXT,
                    n == 0 ? T.t("None yet.") : Fmt.shortDate(s.h.first(g)) + " – " + Fmt.shortDate(s.h.last(g)) + " · " + g.source
                            + (missing != null ? " · " + T.f("missing the result of %s", Fmt.dayMonthInline(missing)) : "")));
        }
        long at = Data.sp(a).getLong("fetchAt", 0);
        TextView up = ui.note(at == 0 ? T.t("Not refreshed yet on this phone.") : T.f("Last refresh: %s.", SummaryTab.when(at)));
        if (at == 0 || System.currentTimeMillis() - at > 6 * 3600_000L) up.setTextColor(Theme.AMBER);
        cov.addView(up, ui.matchWrap(4));
        cov.addView(ui.note(T.t("Bundled with the app, completed from the repository's copy (updated twice a day) and from "
                + "the sites below each time the app refreshes. Results are only ever added, never removed.")));

        // ---- sources ----
        String log = Data.sp(a).getString("fetchLog", "");
        if (!log.isEmpty()) {
            LinearLayout src = ui.card(body, T.t("LAST REFRESH"));
            for (String line : log.split("\n")) {
                String[] f = line.split("\t", -1);
                if (f.length < 3) continue;
                src.addView(ui.status(f[1].equals("ok") ? Theme.GREEN : Theme.AMBER, f[0], f[2]));
            }
        }
    }
}
