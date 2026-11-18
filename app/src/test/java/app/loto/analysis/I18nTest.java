package app.loto.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Test;

/** Every text the app shows has a Spanish version, with the same placeholders; Spanish formats numbers and dates its way. */
public class I18nTest {

    @After
    public void english() {
        T.init(Locale.US);
    }

    /** The English keys passed to T.t(...) and T.f(...) in the sources, string literals joined with + included. */
    static List<String> keys() throws Exception {
        File dir = new File("src/main/java/app/loto/analysis");
        if (!dir.isDirectory()) dir = new File("app/src/main/java/app/loto/analysis");
        List<String> out = new ArrayList<>();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".java") && !n.equals("Es.java"));
        assertTrue("sources not found from " + new File(".").getAbsolutePath(), files != null && files.length > 10);
        Pattern call = Pattern.compile("\\bT\\.[tf]\\(\\s*\"");
        for (File f : files) {
            String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            Matcher m = call.matcher(s);
            while (m.find()) {
                int i = m.end() - 1;
                StringBuilder key = new StringBuilder();
                while (true) {
                    int j = i + 1;
                    while (s.charAt(j) != '"') {
                        char c = s.charAt(j);
                        if (c == '\\') {
                            char n = s.charAt(j + 1);
                            key.append(n == 'n' ? '\n' : n == 't' ? '\t' : n);
                            j += 2;
                        } else {
                            key.append(c);
                            j++;
                        }
                    }
                    i = j + 1;
                    Matcher more = Pattern.compile("\\G\\s*\\+\\s*\"").matcher(s);
                    if (more.find(i)) {
                        i = more.end() - 1;
                        continue;
                    }
                    break;
                }
                out.add(key.toString());
            }
        }
        return out;
    }

    private static List<String> specs(String s) {
        List<String> out = new ArrayList<>();
        Matcher m = Pattern.compile("%(?:\\d+\\$)?[-+ #0,(]*\\d*(?:\\.\\d+)?[a-zA-Z%]").matcher(s);
        while (m.find()) out.add(m.group());
        return out;
    }

    @Test
    public void everyTextHasSpanish() throws Exception {
        List<String> keys = keys();
        assertTrue(keys.size() > 200);
        List<String> missing = new ArrayList<>();
        for (String k : keys) {
            String es = Es.MAP.get(k);
            if (es == null) missing.add(k);
            else assertEquals("placeholders of: " + k, specs(k), specs(es));
        }
        assertTrue("missing Spanish for: " + missing, missing.isEmpty());
    }

    @Test
    public void spanishFormatsNumbersAndDates() {
        T.init(Locale.forLanguageTag("es-CO"));
        assertEquals("1 entre 15.401.568", Combi.oneIn(1 / 15401568.0));
        assertEquals("3:30 p. m.", Fmt.time(Game.hm(15, 30)));
        assertEquals("11:00 a. m.", Fmt.time(Game.hm(11, 0)));
        assertEquals("lun, mié, sáb 11:00 p. m.", Game.BALOTO.scheduleText());
        assertEquals("lun–sáb 11:00 a. m. (no juega los festivos)", Game.DORADO_MANANA.scheduleText());
        assertEquals("Martes 17 de noviembre de 2026", Fmt.dayHeading(LocalDate.of(2026, 11, 17)));
        assertEquals("mar 17 nov 2026", Fmt.date(LocalDate.of(2026, 11, 17)));
        assertEquals("0,43", Combi.pText(0.4312));
        assertEquals("Ordinario", Randomness.drawLabel(80));
        T.init(Locale.US);
        assertEquals("1 in 15,401,568", Combi.oneIn(1 / 15401568.0));
        assertEquals("Ordinary", Randomness.drawLabel(80));
    }

    @Test
    public void engineRunsInSpanish() {
        T.init(Locale.forLanguageTag("es-CO"));
        History h = new History();
        java.util.Random r = new java.util.Random(5);
        LocalDate d = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < 300; i++) {
            h.put(new Draw(Game.DORADO_MANANA, d.plusDays(i), "", new int[]{r.nextInt(10), r.nextInt(10), r.nextInt(10), r.nextInt(10)}, -1));
        }
        List<Game> one = new ArrayList<>();
        one.add(Game.DORADO_MANANA);
        List<Randomness.DrawScore> scores = Randomness.drawScores(h.sample(one, 0));
        assertEquals(300, scores.size());
        boolean ordinary = false;
        for (Randomness.DrawScore ds : scores) ordinary |= ds.reasons.contains("Nada fuera de lo común.");
        assertTrue(ordinary);
    }
}
