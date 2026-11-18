package app.loto.analysis;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/** Today's draws, the schedule, every stored result by date and weekday, and where the data comes from. */
final class ResultsTab {
    private ResultsTab() {}

    static final class Today {
        Game game;
        String time;
        Draw result;
    }

    static final class Out {
        LocalDate today;
        final List<Today> todays = new ArrayList<>();
        /** Newest day first; each day's draws morning to night. */
        final TreeMap<LocalDate, List<Draw>> days = new TreeMap<>(java.util.Collections.reverseOrder());
        int totalDays;
        String log;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        o.today = ZonedDateTime.now(MainActivity.BOGOTA).toLocalDate();
        for (Game g : Game.ALL) {
            String time = g.timeOn(o.today);
            if (time == null) continue;
            Today t = new Today();
            t.game = g;
            t.time = time;
            t.result = s.h.on(g, o.today);
            o.todays.add(t);
        }
        o.todays.sort((a, b) -> Integer.compare(minutes(a.time), minutes(b.time)));
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
        return o;
    }

    /** "3:30 p.m." -> minutes after midnight, for ordering. */
    static int minutes(String t) {
        try {
            String[] hm = t.split("[: ]");
            int h = Integer.parseInt(hm[0]) % 12, m = Integer.parseInt(hm[1]);
            if (t.contains("p.m.")) h += 12;
            return h * 60 + m;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;

        // ---- today, every lottery ----
        LinearLayout today = ui.card(body, "TODAY · " + Fmt.dayHeading(o.today).toUpperCase(Locale.US));
        if (o.todays.isEmpty()) today.addView(ui.body("No draws today."));
        Game.Slot lastSlot = null;
        LocalTime now = ZonedDateTime.now(MainActivity.BOGOTA).toLocalTime();
        for (Today t : o.todays) {
            if (t.game.slot != lastSlot) {
                TextView sl = ui.label(t.game.slot.label, Theme.DIM);
                sl.setPadding(0, lastSlot == null ? 0 : ui.dp(10), 0, ui.dp(4));
                today.addView(sl);
                lastSlot = t.game.slot;
            }
            LinearLayout row = ui.hbox();
            row.setPadding(0, ui.dp(6), 0, ui.dp(6));
            TextView time = ui.text(t.time, 12, Theme.monoMedium(a), Theme.MUTED);
            time.setPadding(0, ui.dp(2), 0, 0);
            row.addView(time, new LinearLayout.LayoutParams(ui.dp(76), ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout right = ui.vbox();
            right.addView(ui.text(t.game.name, 14, Theme.uiSemi(a), Theme.TEXT));
            if (t.result != null) {
                right.addView(ui.balls(t.game, t.result.nums, t.result.extra, 26), ui.matchWrap(5));
            } else {
                boolean due = now.toSecondOfDay() / 60 > minutes(t.time) + 20;
                LinearLayout tagRow = ui.hbox();
                tagRow.addView(ui.tag(due ? "awaiting result" : "plays at " + t.time, due ? Theme.AMBER : Theme.DIM));
                right.addView(tagRow, ui.matchWrap(5));
            }
            row.addView(right, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            today.addView(row);
        }
        TextView tz = ui.note("Times are Colombia time. Chance results usually appear a few minutes after the draw.");
        tz.setPadding(0, ui.dp(8), 0, 0);
        today.addView(tz);

        // ---- schedule ----
        LinearLayout sched = ui.card(body, "DRAW SCHEDULE · " + s.lottery.toUpperCase(Locale.US));
        boolean[] slots = new boolean[3];
        for (Game g : Game.of(s.lottery)) {
            slots[g.slot.ordinal()] = true;
            LinearLayout r = ui.vbox();
            r.setPadding(0, ui.dp(6), 0, ui.dp(6));
            LinearLayout top = ui.hbox();
            top.setGravity(Gravity.CENTER_VERTICAL);
            top.addView(ui.text(g.name, 14, Theme.uiSemi(a), Theme.TEXT), new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            top.addView(ui.tag(g.slot.label, g.slot == Game.Slot.MORNING ? Theme.AMBER_LIGHT : g.slot == Game.Slot.AFTERNOON
                    ? Theme.AMBER : Theme.COOL));
            r.addView(top);
            TextView when = ui.note(g.scheduleText());
            when.setPadding(0, ui.dp(3), 0, 0);
            r.addView(when);
            sched.addView(r);
        }
        List<String> missing = new ArrayList<>();
        for (Game.Slot sl : Game.Slot.values()) if (!slots[sl.ordinal()]) missing.add(sl.label.toLowerCase(Locale.US));
        if (!missing.isEmpty()) {
            TextView m = ui.note(s.lottery + " has no " + String.join(" or ", missing) + " draw.");
            m.setPadding(0, ui.dp(6), 0, 0);
            sched.addView(m);
        }

        // ---- results by date ----
        LinearLayout res = ui.card(body, "RESULTS");
        res.addView(ui.note(MainActivity.scope(s)));
        LinearLayout wd = ui.chipRow(res, 10);
        for (int i = -1; i < 7; i++) {
            final int idx = i;
            wd.addView(ui.chip(i < 0 ? "Every day" : Game.DAY_SHORT[i], i == s.weekday, v -> {
                a.weekday = idx;
                a.limit = 40;
                a.rerenderInPlace();
            }), ui.wrap(i < 0 ? 0 : 6));
        }
        ui.reveal(wd, s.weekday + 1);
        if (o.days.isEmpty()) {
            TextView none = ui.body(s.weekday >= 0 ? "No draws on " + Game.DAY_NAMES[s.weekday] + "s in this selection."
                    : "No results stored for this selection.");
            none.setPadding(0, ui.dp(12), 0, 0);
            res.addView(none);
        }
        boolean several = s.games.size() > 1;
        for (LocalDate day : o.days.keySet()) {
            TextView h = ui.text(Fmt.dayHeading(day), 12, Theme.uiBold(a), Theme.AMBER_LIGHT);
            h.setPadding(0, ui.dp(14), 0, ui.dp(4));
            res.addView(h);
            for (Draw d : o.days.get(day)) {
                LinearLayout row = ui.hbox();
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, ui.dp(4), 0, ui.dp(4));
                String time = d.game.timeOn(day);
                if (time == null) time = d.game.times[day.getDayOfWeek().getValue() - 1];
                LinearLayout who = ui.vbox();
                who.addView(ui.text(several ? d.game.shortName : d.game.name, 13, Theme.uiSemi(a), Theme.TEXT));
                String sub = (time != null ? time : d.game.slot.label) + (d.drawNo.isEmpty() ? "" : " · #" + d.drawNo);
                who.addView(ui.text(sub, 10, Theme.monoMedium(a), Theme.DIM));
                row.addView(who, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(ui.balls(d.game, d.nums, d.extra, d.game.digits() ? 26 : 25));
                res.addView(row);
            }
        }
        if (o.totalDays > o.days.size()) {
            res.addView(ui.secondaryButton("Show " + Math.min(60, o.totalDays - o.days.size()) + " more days", v -> {
                a.limit += 60;
                a.rerenderInPlace();
            }), ui.matchWrap(14));
        }
        if (!o.days.isEmpty()) {
            boolean digits = s.games.get(0).digits();
            TextView key = ui.note(digits ? "The blue tile is the quinta (fifth digit), where it was published."
                    : s.games.get(0).extraMax > 0 ? "The red ball is the superbalota." : "");
            key.setPadding(0, ui.dp(10), 0, 0);
            if (key.getText().length() > 0) res.addView(key);
        }

        // ---- coverage ----
        LinearLayout cov = ui.card(body, "HISTORY STORED");
        for (Game g : Game.of(s.lottery)) {
            int n = s.h.size(g);
            cov.addView(ui.kv(g.name, String.format(Locale.US, "%,d draws", n), Theme.TEXT,
                    n == 0 ? "None yet." : Fmt.shortDate(s.h.first(g)) + " – " + Fmt.shortDate(s.h.last(g)) + " · " + g.source));
        }
        cov.addView(ui.note("Bundled with the app, completed from the repository's copy (updated twice a day) and from "
                + "the sites below each time the app refreshes. Results are only ever added, never removed."));

        // ---- sources ----
        String log = Data.sp(a).getString("fetchLog", "");
        if (!log.isEmpty()) {
            LinearLayout src = ui.card(body, "LAST REFRESH");
            for (String line : log.split("\n")) {
                String[] f = line.split("\t", -1);
                if (f.length < 3) continue;
                src.addView(ui.status(f[1].equals("ok") ? Theme.GREEN : Theme.AMBER, f[0], f[2]));
            }
        }
    }
}
