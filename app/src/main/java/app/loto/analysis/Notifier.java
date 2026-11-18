package app.loto.analysis;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Announces new results. About ten minutes after each draw of a followed lottery an alarm (allowed to fire in
 * Doze, no exact-alarm permission needed) starts a job that fetches only the draws that are due; if a result
 * isn't published yet it tries again every 15 minutes for up to four hours. Every new result becomes one
 * notification with its numbers and its randomness audit. A periodic job every three hours re-arms the alarm
 * in case the phone dropped it.
 */
final class Notifier {
    private Notifier() {}

    static final String CHANNEL = "results";
    static final int JOB_NOW = 7101, JOB_PERIODIC = 7102;
    static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final long MIN = 60_000L;

    static boolean enabled(Context c, String lottery) {
        return Data.sp(c).getBoolean("notify_" + lottery, true);
    }

    static void setEnabled(Context c, String lottery, boolean on) {
        Data.sp(c).edit().putBoolean("notify_" + lottery, on).apply();
        schedule(c);
    }

    /** Whether the system lets the app post notifications (Android 13+ asks the user). */
    static boolean allowed(Context c) {
        if (Build.VERSION.SDK_INT >= 33
                && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        return nm == null || nm.areNotificationsEnabled();
    }

    static void ensureChannel(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL, T.t("Results"), NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription(T.t("A notification when a result you follow is announced"));
        nm.createNotificationChannel(ch);
    }

    private static long at(LocalDate d, int minutes) {
        return ZonedDateTime.of(d, LocalTime.of(minutes / 60, minutes % 60), BOGOTA).toInstant().toEpochMilli();
    }

    /** When to look next: ten minutes after the next draw, or sooner to retry a result that's late. */
    static long nextWake(Context c, History h, long now) {
        long best = Long.MAX_VALUE;
        LocalDate today = ZonedDateTime.now(BOGOTA).toLocalDate();
        for (Game g : Game.ALL) {
            if (!enabled(c, g.lottery)) continue;
            for (int i = -1; i <= 8; i++) {
                LocalDate d = today.plusDays(i);
                int t = g.timeOn(d);
                if (t < 0) continue;
                long draw = at(d, t), check = draw + 10 * MIN;
                if (check > now) {
                    best = Math.min(best, check);
                    break; // later days only come later
                }
                if (h.on(g, d) == null && now - draw < 4 * 60 * MIN) best = Math.min(best, now + 15 * MIN);
            }
        }
        return best == Long.MAX_VALUE ? now + 6 * 60 * MIN : best;
    }

    /** Followed draws held in the last four hours whose result isn't stored yet. */
    static List<Game> due(Context c, History h, long now) {
        List<Game> out = new ArrayList<>();
        LocalDate today = ZonedDateTime.now(BOGOTA).toLocalDate();
        for (Game g : Game.ALL) {
            if (!enabled(c, g.lottery)) continue;
            for (int i = -1; i <= 0; i++) {
                LocalDate d = today.plusDays(i);
                int t = g.timeOn(d);
                if (t < 0) continue;
                long draw = at(d, t);
                if (now >= draw + 5 * MIN && now - draw < 4 * 60 * MIN && h.on(g, d) == null && !out.contains(g)) out.add(g);
            }
        }
        return out;
    }

    /** Arms the alarm for the next check and makes sure the periodic backstop job exists. Never throws. */
    static void schedule(Context c) {
        try {
            arm(c);
        } catch (RuntimeException e) {
            android.util.Log.w("LotoLab", "couldn't schedule the result check", e);
        }
    }

    private static void arm(Context c) {
        Context app = c.getApplicationContext();
        AlarmManager am = app.getSystemService(AlarmManager.class);
        PendingIntent pi = PendingIntent.getBroadcast(app, 0, new Intent(app, DrawAlarmReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        boolean any = false;
        for (String l : Game.LOTTERIES) any |= enabled(app, l);
        JobScheduler js = app.getSystemService(JobScheduler.class);
        if (!any) {
            if (am != null) am.cancel(pi);
            if (js != null) js.cancel(JOB_PERIODIC);
            return;
        }
        long now = System.currentTimeMillis();
        long when = nextWake(app, Data.history(app), now);
        if (am != null) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Math.max(now + MIN, when), pi);
        if (js != null && js.getPendingJob(JOB_PERIODIC) == null) {
            js.schedule(new JobInfo.Builder(JOB_PERIODIC, new ComponentName(app, ResultJobService.class))
                    .setPeriodic(3 * 60 * MIN)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build());
        }
    }

    /** Runs a check as soon as there's a network (from the alarm). */
    static void runSoon(Context c) {
        try {
            Context app = c.getApplicationContext();
            JobScheduler js = app.getSystemService(JobScheduler.class);
            if (js == null) return;
            js.schedule(new JobInfo.Builder(JOB_NOW, new ComponentName(app, ResultJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build());
        } catch (RuntimeException e) {
            android.util.Log.w("LotoLab", "couldn't start the result check", e);
        }
    }

    /** The background check (off the main thread): fetch what's due, announce it, arm the next alarm. */
    static void check(Context c) {
        T.init(Locale.getDefault());
        History h = Data.history(c);
        List<Game> due = due(c, h, System.currentTimeMillis());
        if (!due.isEmpty()) {
            Fetcher.Status st = Fetcher.latest(c, h, due);
            announce(c, h, st.fresh);
        }
        schedule(c);
    }

    // ------------------------------------------------------------------ announcing

    private static Set<String> seen(SharedPreferences sp) {
        return new HashSet<>(sp.getStringSet("seen", Collections.emptySet()));
    }

    private static void saveSeen(SharedPreferences sp, Set<String> seen) {
        // keep only the last ten days so the set stays small
        String cutoff = LocalDate.now(BOGOTA).minusDays(10).toString();
        Set<String> keep = new HashSet<>();
        for (String k : seen) if (k.substring(k.lastIndexOf('@') + 1).compareTo(cutoff) >= 0) keep.add(k);
        sp.edit().putStringSet("seen", keep).putBoolean("announceReady", true).apply();
    }

    private static String key(Draw d) { return d.game.id + "@" + d.date; }

    /** The app is open and showing them: don't announce these later. */
    static void markSeen(Context c, List<Draw> fresh) {
        if (fresh.isEmpty() && Data.sp(c).getBoolean("announceReady", false)) return;
        SharedPreferences sp = Data.sp(c);
        Set<String> seen = seen(sp);
        for (Draw d : fresh) seen.add(key(d));
        saveSeen(sp, seen);
    }

    static void announce(Context c, History h, List<Draw> fresh) {
        SharedPreferences sp = Data.sp(c);
        Set<String> seen = seen(sp);
        boolean ready = sp.getBoolean("announceReady", false);
        LocalDate yesterday = LocalDate.now(BOGOTA).minusDays(1);
        boolean can = allowed(c);
        if (can) ensureChannel(c);
        for (Draw d : fresh) {
            String k = key(d);
            if (seen.contains(k)) continue;
            seen.add(k);
            // the very first check only learns what's there; old results and unfollowed lotteries stay quiet
            if (!ready || !can || d.date.isBefore(yesterday) || !enabled(c, d.game.lottery)) continue;
            try {
                post(c, h, d);
            } catch (RuntimeException e) {
                android.util.Log.w("LotoLab", "couldn't post a notification", e);
            }
        }
        saveSeen(sp, seen);
    }

    private static void post(Context c, History h, Draw d) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm == null) return;
        // the audit needs the draw before it for the "repeated from the previous draw" check
        List<Draw> all = h.all(d.game);
        int i = -1;
        for (int j = 0; j < all.size(); j++) if (all.get(j).date.equals(d.date)) i = j;
        List<Draw> pair = new ArrayList<>();
        if (i > 0) pair.add(all.get(i - 1));
        pair.add(i >= 0 ? all.get(i) : d);
        List<Game> one = new ArrayList<>();
        one.add(d.game);
        List<Randomness.DrawScore> scores = Randomness.drawScores(new Sample(one, pair));
        Randomness.DrawScore ds = scores.isEmpty() ? null : scores.get(scores.size() - 1);

        String numbers = d.numbersText() + (d.extra >= 0 ? (d.game.digits() ? "  ·  " + T.t("quinta") + " " : "  +  ") + d.extraText() : "");
        String title = d.game.name + "  ·  " + numbers;
        String text = ds == null ? Fmt.dayMonth(d.date)
                : T.f("%s · %s draw (%d/100)", Fmt.dayMonth(d.date), ds.label().toLowerCase(T.L), ds.score);
        String big = ds == null ? text : text + "\n" + String.join(" ", ds.reasons);

        Intent open = new Intent(c, MainActivity.class)
                .putExtra("lottery", d.game.lottery)
                .putExtra("game", d.game.id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int id = key(d).hashCode();
        PendingIntent pi = PendingIntent.getActivity(c, id, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new Notification.Builder(c, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_result)
                .setColor(Theme.AMBER)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(big))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setGroup("results_" + d.game.lottery)
                .setWhen(at(d.date, Math.max(0, d.game.usualTime(d.date))))
                .setShowWhen(true)
                .build();
        nm.notify(id, n);
    }
}
