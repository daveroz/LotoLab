package app.loto.analysis;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.TextView;

/**
 * The look: Fuji-App's crystal glass and controls on Worldly's stage. A night stage in haze, lit by one amber key
 * light, with film grain over everything ({@link StageView}); cards of charcoal glass with a sheen, grain and a
 * hairline rim brightest at the corners, picking up the stage's light behind them ({@link Glass}); a segmented
 * control for sections, equal cells for choices, a switch for on/off, solid amber for the key action, solid icons
 * ({@link Icon}), and small labels in spaced bold capitals. Type is Carbon+, as in Worldly, SAB Monitor and Fuji-App.
 * The palette is Fuji-App's near-neutral charcoal; the analysis adds a cool tone for "cold" numbers and coral for
 * strong deviations.
 */
final class Theme {
    private Theme() {}

    // surfaces
    static final int BG = 0xFF060708;        // the night behind everything
    // hairlines: white at low strength, an edge of light rather than a line of paint
    static final int BORDER_SOFT = 0x14FFFFFF;
    static final int BORDER = 0x1FFFFFFF;
    static final int BORDER_STRONG = 0x33FFFFFF;
    // text
    static final int TEXT = 0xFFF2F3F5;
    static final int TEXT_SOFT = 0xFFCDD0D6;
    static final int MUTED = 0xFF9EA2AA;
    static final int DIM = 0xFF6E727B;
    // the key light
    static final int AMBER = 0xFFEB9438;
    static final int AMBER_MID = 0xFFE08A33;
    static final int AMBER_DEEP = 0xFFC97624;
    /** The key a quarter of the way to white: labels and lines drawn in the light. */
    static final int AMBER_LIGHT = 0xFFF0AE6A;
    /** The key almost white: the hot core of a lamp. */
    static final int LED = 0xFFFDF2E7;
    static final int KEY_GLASS = 0x24EB9438;
    static final int KEY_GLASS_HI = 0x3DEB9438;
    static final int KEY_EDGE = 0x99EB9438;
    static final int GREEN = 0xFF34D399;
    /** Cold numbers and "fewer than expected": a cool blue, the opposite of the warm key. */
    static final int COOL = 0xFF7FB8E6;
    /** Strong deviations. */
    static final int CORAL = 0xFFF2706B;

    private static Typeface uiMedium, uiSemi, uiBold, monoMedium, monoSemi, monoBold;

    private static synchronized void init(Context c) {
        if (uiMedium != null) return;
        Context app = c.getApplicationContext();
        // Carbon+ everywhere: regular for text and numbers, Bold for titles and the big figures.
        Typeface regular = load(app, "fonts/carbonplus.otf", Typeface.create("sans-serif", Typeface.NORMAL));
        Typeface bold = load(app, "fonts/carbonplus-Bold.otf", Typeface.create("sans-serif", Typeface.BOLD));
        uiMedium = regular;
        uiSemi = regular;
        uiBold = bold;
        monoMedium = regular;
        monoSemi = regular;
        monoBold = bold;
    }

    private static Typeface load(Context c, String asset, Typeface fallback) {
        try {
            return Typeface.createFromAsset(c.getAssets(), asset);
        } catch (Exception e) {
            return fallback;
        }
    }

    static Typeface uiMedium(Context c) { init(c); return uiMedium; }
    static Typeface uiSemi(Context c) { init(c); return uiSemi; }
    static Typeface uiBold(Context c) { init(c); return uiBold; }
    static Typeface monoMedium(Context c) { init(c); return monoMedium; }
    static Typeface monoSemi(Context c) { init(c); return monoSemi; }
    static Typeface monoBold(Context c) { init(c); return monoBold; }

    // ---- drawable helpers ----

    static float dp(Context c, float v) { return v * c.getResources().getDisplayMetrics().density; }

    /** A plain rounded box with an optional 1 dp border (stroke = 0 for none), for small solid shapes. */
    static GradientDrawable box(Context c, int fill, int stroke, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(c, radiusDp));
        if (stroke != 0) g.setStroke(Math.max(1, Math.round(dp(c, 1))), stroke);
        return g;
    }

    /** A card of crystal glass ({@link Glass.Pane}). */
    static Drawable glass(Context c, float radiusDp) {
        return new Glass.Pane(c, dp(c, radiusDp), Glass.Tone.PANE, false);
    }

    /** Glass of a given tone; {@code radiusDp} < 0 takes a control's corner. */
    static Drawable glass(Context c, float radiusDp, Glass.Tone tone) {
        return new Glass.Pane(c, radiusDp < 0 ? -1 : dp(c, radiusDp), tone, false);
    }

    /** Glass that takes the key light when it's pressed. */
    static Drawable glassPressable(Context c, float radiusDp, Glass.Tone tone) {
        float r = radiusDp < 0 ? -1 : dp(c, radiusDp);
        return pressable(new Glass.Pane(c, r, tone, false), new Glass.Pane(c, r, tone, true));
    }

    /** The key action: solid amber. */
    static Drawable key(Context c, float radiusDp) {
        float r = radiusDp < 0 ? -1 : dp(c, radiusDp);
        return pressable(new Glass.Key(c, r, false), new Glass.Key(c, r, true));
    }

    /** A recess inside glass, for inputs; it takes the key light when focused. */
    static Drawable well(Context c, float radiusDp, boolean active) {
        return active ? new Glass.Lit(c, radiusDp, false) : glass(c, radiusDp, Glass.Tone.WELL);
    }

    /** Something that's on: an amber wash with the light bleeding in from a glowing edge, within its bounds. */
    static Drawable lit(Context c, float radiusDp, boolean pressed) {
        return new Glass.Lit(c, radiusDp, pressed);
    }

    /** Small label: bold capitals, spaced out, like markings on a piece of equipment. */
    static void label(Context c, TextView t) {
        t.setAllCaps(true);
        t.setTypeface(uiBold(c));
        t.setTextSize(11);
        t.setLetterSpacing(0.22f);
    }

    static StateListDrawable pressable(Drawable normal, Drawable pressed) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, pressed);
        s.addState(new int[]{}, normal);
        return s;
    }
}
