package app.loto.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

/**
 * Opens the app the way a phone does (Android 14), in Spanish and in English, and shows every lottery's Summary
 * and every tab, so a crash on opening, on the main thread or a background one, fails the build.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class LaunchTest {
    private final List<Throwable> crashes = Collections.synchronizedList(new ArrayList<>());

    @Test
    @Config(qualifiers = "es-rCO")
    public void opensInSpanish() throws Exception {
        walk();
    }

    @Test
    @Config(qualifiers = "en-rUS")
    public void opensInEnglish() throws Exception {
        walk();
    }

    /**
     * Android 14 throws (and the app crashes on opening) when a job with a network constraint is scheduled without
     * ACCESS_NETWORK_STATE; Robolectric doesn't enforce that, so the manifest is checked directly.
     */
    @Test
    public void declaresWhatTheBackgroundCheckNeeds() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).get();
        PackageInfo pi = a.getPackageManager().getPackageInfo(a.getPackageName(), PackageManager.GET_PERMISSIONS);
        List<String> p = Arrays.asList(pi.requestedPermissions);
        for (String need : new String[]{"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.RECEIVE_BOOT_COMPLETED", "android.permission.POST_NOTIFICATIONS"}) {
            assertTrue("missing " + need, p.contains(need));
        }
    }

    private void walk() throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> crashes.add(e));
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        assertNotNull(a);
        settle(a);
        for (String lottery : Game.LOTTERIES) {
            for (int tab = 0; tab <= MainActivity.STATS; tab++) show(a, lottery, 0, tab);
        }
        show(a, "Dorado", 1, MainActivity.SUMMARY); // Tarde
        show(a, "Dorado", 2, MainActivity.STATS);   // Noche
        show(a, "Baloto", 1, MainActivity.SUMMARY); // Revancha
        show(a, "Baloto", 1, MainActivity.STATS);
        // every amount in El Dorado's prize list
        for (double w : new double[]{200, 500, 1000, 2000, 5000}) {
            a.checkWager = w;
            show(a, "Dorado", 0, MainActivity.SUMMARY);
        }
    }

    private void show(MainActivity a, String lottery, int game, int tab) throws Exception {
        a.lottery = lottery;
        a.gameIdx = game;
        a.tab = tab;
        a.buildSelectors();
        a.render();
        settle(a);
        View root = a.getWindow().getDecorView();
        assertTrue(lottery + " tab " + tab + " shows nothing", textCount(root) > 3);
        String all = text(root);
        assertFalse(lottery + " tab " + tab + " failed: " + all, all.contains(T.t("SOMETHING WENT WRONG")));
    }

    /** Waits for the background work to finish (the loading card to go), then for the main thread. */
    private void settle(MainActivity a) throws Exception {
        java.lang.reflect.Field f = MainActivity.class.getDeclaredField("body");
        f.setAccessible(true);
        ViewGroup body = (ViewGroup) f.get(a);
        String working = T.t("Working it out…");
        for (int i = 0; i < 1600; i++) {
            Thread.sleep(25);
            ShadowLooper.idleMainLooper();
            if (i < 8 || body == null || body.getChildCount() == 0) continue;
            String first = text(body.getChildAt(0));
            if (!first.contains(working)) break;
        }
        ShadowLooper.idleMainLooper();
        if (!crashes.isEmpty()) throw new AssertionError("crash", crashes.get(0));
    }

    private static String text(View v) {
        if (v instanceof TextView) return ((TextView) v).getText().toString();
        StringBuilder sb = new StringBuilder();
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) sb.append(text(g.getChildAt(i))).append('\n');
        }
        return sb.toString();
    }

    private static int textCount(View v) {
        if (v instanceof TextView) return 1;
        int n = 0;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) n += textCount(g.getChildAt(i));
        }
        return n;
    }
}
