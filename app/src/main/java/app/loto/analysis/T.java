package app.loto.analysis;

import java.util.Locale;

/**
 * The app's language: Spanish when the device is set to Spanish, English otherwise. Every text shown passes
 * through t() or f(), with the English sentence as its own key; {@link Es} holds the Spanish versions. Numbers
 * follow the language too (15,401,568 in English, 15.401.568 in Spanish). A key without a Spanish entry falls
 * back to English, and a unit test checks that none is missing.
 */
final class T {
    private T() {}

    static boolean es;
    /** Locale for numbers in text: US or Colombian Spanish. */
    static Locale L = Locale.US;

    static void init(Locale device) {
        es = device != null && "es".equals(device.getLanguage());
        L = es ? Locale.forLanguageTag("es-CO") : Locale.US;
    }

    static String t(String en) {
        if (!es) return en;
        String s = Es.MAP.get(en);
        return s != null ? s : en;
    }

    static String f(String en, Object... args) {
        return String.format(L, t(en), args);
    }
}
