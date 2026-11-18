package app.loto.analysis;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Where the history lives: the CSV files bundled in the APK (assets/history), plus everything fetched since,
 * saved in the app's own storage in the same format. Both are merged into one History on start.
 */
final class Data {
    private Data() {}

    private static History history;

    static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("loto", Context.MODE_PRIVATE);
    }

    static synchronized History history(Context c) {
        if (history != null) return history;
        History h = new History();
        Context app = c.getApplicationContext();
        for (Game g : Game.ALL) {
            try (InputStream in = app.getAssets().open("history/" + g.id + ".csv")) {
                h.addCsv(g, read(in));
            } catch (IOException ignored) { } // a game without bundled history starts empty
            File f = file(app, g);
            if (f.exists()) {
                try (InputStream in = new FileInputStream(f)) {
                    h.addCsv(g, read(in));
                } catch (IOException ignored) { }
            }
        }
        history = h;
        return h;
    }

    static synchronized void save(Context c, History h) {
        File dir = new File(c.getApplicationContext().getFilesDir(), "history");
        if (!dir.exists() && !dir.mkdirs()) return;
        for (Game g : Game.ALL) {
            File tmp = new File(dir, g.id + ".csv.tmp");
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                out.write(h.toCsv(g).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                continue;
            }
            //noinspection ResultOfMethodCallIgnored
            tmp.renameTo(file(c, g));
        }
    }

    private static File file(Context c, Game g) {
        return new File(new File(c.getApplicationContext().getFilesDir(), "history"), g.id + ".csv");
    }

    static String read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[16384];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
