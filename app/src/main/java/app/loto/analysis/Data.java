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
import java.util.List;

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

    // ------------------------------------------------------------------ recommendation log

    /**
     * Keeps every recommendation the app produced, one line per change: when it was generated, for which draw
     * (game and date), by which model, and the numbers. The first line for a target keeps its generation time while
     * the recommendation stays the same; new data that changes it adds a line.
     */
    static synchronized long logRec(Context c, Recommender.Rec r, long now) {
        if (r.nums == null || r.target == null) return now;
        String nums = new Draw(r.game, r.target, "", r.nums, r.extra).numbersText() + (r.extra > 0 ? "+" + r.extra : "");
        List<String[]> log = recLog(c);
        for (int i = log.size() - 1; i >= 0; i--) {
            String[] row = log.get(i);
            if (row[1].equals(r.game.id) && row[2].equals(r.target.toString())) {
                if (row[4].equals(nums) && row[3].equals(r.model.name())) {
                    try {
                        return Long.parseLong(row[0]);
                    } catch (NumberFormatException e) {
                        break;
                    }
                }
                break;
            }
        }
        String line = now + "," + r.game.id + "," + r.target + "," + r.model.name() + "," + nums + "\n";
        File f = new File(c.getApplicationContext().getFilesDir(), "recs.csv");
        try (FileOutputStream out = new FileOutputStream(f, true)) {
            out.write(line.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) { }
        return now;
    }

    /** {generatedAt millis, game id, target date, model, numbers} rows, oldest first. */
    static synchronized List<String[]> recLog(Context c) {
        List<String[]> out = new java.util.ArrayList<>();
        File f = new File(c.getApplicationContext().getFilesDir(), "recs.csv");
        if (!f.exists()) return out;
        try (InputStream in = new FileInputStream(f)) {
            for (String line : read(in).split("\n")) {
                String[] p = line.split(",", -1);
                if (p.length == 5) out.add(p);
            }
        } catch (IOException ignored) { }
        return out;
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
