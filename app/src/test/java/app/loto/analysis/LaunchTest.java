package app.loto.analysis;

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
        settle();
        for (String lottery : Game.LOTTERIES) show(a, lottery, 0, MainActivity.SUMMARY);
        for (int tab = 0; tab <= MainActivity.ODDS; tab++) show(a, "Dorado", 3, tab);
        show(a, "Baloto", 2, MainActivity.HISTORY);
    }

    private void show(MainActivity a, String lottery, int game, int tab) throws Exception {
        a.lottery = lottery;
        a.gameIdx = game;
        a.tab = tab;
        a.check = Game.of(lottery).get(0).digits() ? "2820" : "5 13 19 25 29 + 7";
        a.buildSelectors();
        a.render();
        settle();
        assertTrue(lottery + " tab " + tab + " shows nothing", textCount(a.getWindow().getDecorView()) > 3);
    }

    private void settle() throws Exception {
        for (int i = 0; i < 50; i++) {
            Thread.sleep(25);
            ShadowLooper.idleMainLooper();
        }
        if (!crashes.isEmpty()) throw new AssertionError("crash", crashes.get(0));
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
