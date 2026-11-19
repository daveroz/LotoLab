package app.loto.analysis;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * Building blocks of every screen, in Fuji-App's crystal style over Worldly's stage: cards of glass with their titles
 * above, segmented controls, equal-cell chips, a switch, solid amber for the key action, icons in bubbles of glass,
 * lamps for status, and spaced capital labels.
 */
final class Ui {
    final Activity a;

    Ui(Activity a) { this.a = a; }

    int dp(float v) { return Math.round(Theme.dp(a, v)); }

    LinearLayout vbox() {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    LinearLayout hbox() {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    LinearLayout.LayoutParams matchWrap(int topDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(topDp);
        return lp;
    }

    LinearLayout.LayoutParams wrap(int leftDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = dp(leftDp);
        return lp;
    }

    TextView text(String s, float sp, Typeface tf, int color) {
        TextView t = new TextView(a);
        t.setText(s);
        t.setTextSize(sp);
        t.setTypeface(tf);
        t.setTextColor(color);
        return t;
    }

    // Text sizes (sp), set for small phones: readable without crowding.
    static final float BODY = 14.5f, NOTE = 13f, LABEL = 11f, SMALL = 12f;

    /** Body copy. */
    TextView body(String s) {
        TextView t = text(s, BODY, Theme.uiMedium(a), Theme.TEXT_SOFT);
        t.setLineSpacing(0, 1.14f);
        return t;
    }

    /** Quiet explanatory text. */
    TextView note(String s) {
        TextView t = text(s, NOTE, Theme.uiMedium(a), Theme.MUTED);
        t.setLineSpacing(0, 1.12f);
        return t;
    }

    TextView label(String s, int color) {
        TextView t = text(s, LABEL, Theme.uiBold(a), color);
        Theme.label(a, t);
        t.setTextSize(LABEL);
        return t;
    }

    // ---------------------------------------------------------------- cards

    /** Card corners, as in Fuji-App: rounded squares kept tight. */
    static final float CARD_RADIUS = 13;

    /** A section title above its card: spaced capitals in the key light. */
    private TextView sectionTitle(String title) {
        TextView t = text(title, 11.5f, Theme.uiBold(a), Theme.AMBER_LIGHT);
        Theme.label(a, t);
        t.setTextSize(11.5f);
        t.setMaxLines(1);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        return t;
    }

    /** A card of glass with its title above it, as Fuji-App lays out its sections. Returns the card to fill. */
    LinearLayout card(LinearLayout parent, String title) {
        return card(parent, title, null);
    }

    /** A card whose title carries what kind of figures it holds: descriptive, theoretical or predictive evidence. */
    LinearLayout card(LinearLayout parent, String title, String kind) {
        if (title != null) {
            LinearLayout head = hbox();
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.setPadding(dp(4), 0, dp(2), 0);
            head.addView(sectionTitle(title), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            if (kind != null) head.addView(tag(kind, Theme.COOL), wrap(8));
            LinearLayout.LayoutParams hp = matchWrap(24);
            hp.bottomMargin = dp(10);
            parent.addView(head, hp);
        }
        LinearLayout c = vbox();
        c.setBackground(Theme.glass(a, CARD_RADIUS));
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        parent.addView(c, matchWrap(title == null ? 14 : 0));
        return c;
    }

    /**
     * Details that stay folded until tapped, so a card shows its key finding first and keeps the depth one tap
     * away: a chevron and the title in the key light. Returns the container to fill.
     */
    LinearLayout details(LinearLayout parent, String title) {
        final LinearLayout box = vbox();
        box.setVisibility(View.GONE);
        LinearLayout head = hbox();
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(0, dp(10), 0, dp(6));
        final Icon chevron = new Icon(a, Icon.Kind.CHEVRON_RIGHT, Theme.AMBER_LIGHT);
        head.addView(chevron, new LinearLayout.LayoutParams(dp(14), dp(14)));
        TextView t = text(title, 13, Theme.uiSemi(a), Theme.AMBER_LIGHT);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.leftMargin = dp(8);
        head.addView(t, tp);
        head.setClickable(true);
        head.setOnClickListener(v -> {
            boolean open = box.getVisibility() != View.VISIBLE;
            box.setVisibility(open ? View.VISIBLE : View.GONE);
            chevron.setRotation(open ? 90 : 0);
        });
        parent.addView(head, matchWrap(4));
        parent.addView(box, matchWrap(0));
        return box;
    }

    // ---------------------------------------------------------------- choices

    /**
     * Sections or filters in a glass capsule (a segmented control): a recessed well, the chosen one a lighter lens of
     * glass with its name in white, the others muted. Every segmented control is the same size.
     */
    LinearLayout segmented(LinearLayout parent, String[] options, int selected, java.util.function.IntConsumer onPick, int topDp) {
        LinearLayout track = hbox();
        track.setBackground(Theme.glass(a, 13, Glass.Tone.WELL));
        int pad = dp(4);
        track.setPadding(pad, pad, pad, pad);
        for (int i = 0; i < options.length; i++) {
            final int idx = i;
            boolean on = i == selected;
            TextView t = label(options[i], on ? Theme.TEXT : Theme.MUTED);
            t.setTextSize(10.5f);
            t.setLetterSpacing(0.2f);
            t.setGravity(Gravity.CENTER);
            t.setMaxLines(1);
            t.setAutoSizeTextTypeUniformWithConfiguration(7, 11, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
            t.setMinHeight(dp(38));
            t.setPadding(dp(4), 0, dp(4), 0);
            if (on) t.setBackground(Theme.glass(a, 9, Glass.Tone.LENS));
            t.setClickable(true);
            t.setFocusable(true);
            if (on) t.setSelected(true);
            t.setOnClickListener(v -> onPick.accept(idx));
            track.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        parent.addView(track, matchWrap(topDp));
        return track;
    }

    /**
     * A set of choices as equal cells, edge to edge across the card in even rows: the chosen one lit, never bigger or
     * smaller than the rest. Labels shrink to fit their cell.
     */
    Controls.ChipGrid chips(LinearLayout parent, String[] options, int selected, java.util.function.IntConsumer onPick, int topDp) {
        Controls.ChipGrid grid = new Controls.ChipGrid(a, dp(8), dp(38));
        for (int i = 0; i < options.length; i++) {
            final int idx = i;
            boolean on = i == selected;
            TextView t = label(options[i], on ? Theme.AMBER_LIGHT : Theme.MUTED);
            t.setLetterSpacing(0.12f);
            t.setGravity(Gravity.CENTER);
            t.setMaxLines(1);
            t.setAutoSizeTextTypeUniformWithConfiguration(7, 11, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
            t.setPadding(dp(6), 0, dp(6), 0);
            t.setBackground(on ? Theme.lit(a, 10, false) : Theme.glassPressable(a, 10, Glass.Tone.PANE));
            t.setClickable(true);
            t.setFocusable(true);
            if (on) t.setSelected(true);
            t.setOnClickListener(v -> onPick.accept(idx));
            grid.addView(t);
        }
        parent.addView(grid, matchWrap(topDp));
        return grid;
    }

    // ---------------------------------------------------------------- buttons

    /** The key action of a screen: solid amber, its label in the night's warm ink. */
    TextView primaryButton(String label, Icon.Kind icon, View.OnClickListener l) {
        return button(label, icon, Glass.Key.INK, Glass.Key.INK, Theme.key(a, -1), l);
    }

    /** A button of glass, its icon in the key light. */
    TextView secondaryButton(String label, Icon.Kind icon, View.OnClickListener l) {
        return button(label, icon, Theme.TEXT, Theme.AMBER_LIGHT, Theme.glassPressable(a, -1, Glass.Tone.PANE), l);
    }

    TextView secondaryButton(String label, View.OnClickListener l) {
        return secondaryButton(label, null, l);
    }

    private TextView button(String label, Icon.Kind icon, int color, int iconColor, android.graphics.drawable.Drawable bg, View.OnClickListener l) {
        TextView b = new Centred(a);
        b.setText(label);
        b.setTextColor(color);
        Theme.label(a, b);
        b.setTextSize(12);
        b.setMaxLines(1);
        b.setAutoSizeTextTypeUniformWithConfiguration(7, 12, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        if (icon != null) {
            Icon.Glyph g = new Icon.Glyph(icon, iconColor);
            g.setBounds(0, 0, dp(17), dp(17));
            b.setCompoundDrawablesRelative(g, null, null, null);
            b.setCompoundDrawablePadding(dp(9));
        }
        b.setGravity(Gravity.CENTER);
        b.setMinHeight(dp(48));
        b.setPadding(dp(16), 0, dp(16), 0);
        b.setBackground(bg);
        b.setClickable(true);
        b.setFocusable(true);
        b.setOnClickListener(l);
        return b;
    }

    /** A button's label with its icon beside it, the two centred together (a plain TextView keeps the icon at the edge). */
    static final class Centred extends TextView {
        private final int least;

        Centred(android.content.Context c) {
            super(c);
            least = Math.round(Theme.dp(c, 16));
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            android.graphics.drawable.Drawable icon = getCompoundDrawablesRelative()[0];
            if (icon == null) return;
            CharSequence shown = getTransformationMethod() != null ? getTransformationMethod().getTransformation(getText(), this) : getText();
            float text = getPaint().measureText(shown, 0, shown.length());
            int content = icon.getBounds().width() + getCompoundDrawablePadding() + Math.round(text);
            int side = Math.max(least, (getWidth() - content) / 2 - least / 4); // a little slack, so the label never has to shrink
            if (Math.abs(getPaddingLeft() - side) > 1) post(() -> setPaddingRelative(side, getPaddingTop(), side, getPaddingBottom()));
        }
    }

    /**
     * A setting that's just on or off: its name in spaced amber capitals, as a section title, and a switch beside it;
     * the whole row toggles it. {@code description} is what a screen reader says.
     */
    LinearLayout switchRow(LinearLayout parent, String title, String description, boolean on, View.OnClickListener l) {
        LinearLayout row = hbox();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), 0, dp(2), 0);
        row.addView(sectionTitle(title), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Controls.Switch sw = new Controls.Switch(a);
        sw.setChecked(on);
        row.addView(sw);
        row.setClickable(true);
        row.setFocusable(true);
        row.setContentDescription(description);
        if (android.os.Build.VERSION.SDK_INT >= 30) row.setStateDescription(on ? "on" : "off"); // Android 11+
        row.setOnClickListener(l);
        parent.addView(row, matchWrap(24));
        return row;
    }

    EditText input(String hint, boolean digitsOnly) {
        final EditText e = new EditText(a);
        e.setInputType(digitsOnly ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        e.setSingleLine(true);
        e.setHint(hint);
        e.setTextSize(15);
        e.setTypeface(Theme.monoMedium(a));
        e.setTextColor(Theme.TEXT);
        e.setHintTextColor(Theme.DIM);
        e.setHighlightColor(0x55EB9438);
        e.setPadding(dp(12), dp(11), dp(12), dp(11));
        e.setBackground(Theme.well(a, 12, false));
        e.setOnFocusChangeListener((v, focused) -> e.setBackground(Theme.well(a, 12, focused)));
        return e;
    }

    void addWeighted(LinearLayout row, View v, int leftMarginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.leftMargin = dp(leftMarginDp);
        row.addView(v, lp);
    }

    /** A hairline between rows. */
    /**
     * One row of a small table: equal-height cells with the given weights; the first cell left-aligned, the rest
     * right-aligned figures. Header rows are dim labels.
     */
    LinearLayout row(String[] cells, float[] weights, boolean header, int[] colors) {
        LinearLayout r = hbox();
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(header ? 2 : 5), 0, dp(header ? 4 : 5));
        for (int i = 0; i < cells.length; i++) {
            TextView t = header ? text(cells[i], LABEL, Theme.uiBold(a), Theme.DIM)
                    : text(cells[i], i == 0 ? NOTE : NOTE, i == 0 ? Theme.uiMedium(a) : Theme.monoMedium(a),
                    colors != null && colors[i] != 0 ? colors[i] : i == 0 ? Theme.TEXT_SOFT : Theme.TEXT);
            t.setGravity(i == 0 ? Gravity.START : Gravity.END);
            r.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weights[i]));
        }
        return r;
    }

    View divider() {
        View v = new View(a);
        v.setBackgroundColor(Theme.BORDER_SOFT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(1) / 2 + 1));
        lp.topMargin = dp(8);
        lp.bottomMargin = dp(8);
        v.setLayoutParams(lp);
        return v;
    }

    /** Name on the left, figure on the right, optional note below. */
    LinearLayout kv(String name, String value, int valueColor, String note) {
        LinearLayout box = vbox();
        box.setPadding(0, dp(5), 0, dp(5));
        LinearLayout row = hbox();
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView n = text(name, 14, Theme.uiMedium(a), Theme.TEXT);
        row.addView(n, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView v = text(value, 14, Theme.monoSemi(a), valueColor);
        v.setGravity(Gravity.END);
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vp.leftMargin = dp(10);
        row.addView(v, vp);
        box.addView(row);
        if (note != null && !note.isEmpty()) {
            TextView t = note(note);
            t.setPadding(0, dp(2), 0, 0);
            box.addView(t);
        }
        return box;
    }

    /** A status lamp blooming in its colour, a title and its explanation. */
    LinearLayout status(int color, String title, String detail) {
        LinearLayout row = hbox();
        row.setPadding(0, dp(6), 0, dp(6));
        View lamp = new View(a);
        lamp.setBackground(new Glass.Lamp(color));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(18), dp(18));
        lp.rightMargin = dp(8);
        lp.leftMargin = -dp(4); // the lamp's glow reaches past the margin; its core lines up with the text
        row.addView(lamp, lp);
        LinearLayout txt = vbox();
        txt.addView(text(title, 14.5f, Theme.uiSemi(a), Theme.TEXT));
        if (detail != null && !detail.isEmpty()) {
            TextView d = note(detail);
            d.setPadding(0, dp(2), 0, 0);
            txt.addView(d);
        }
        row.addView(txt, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    /** A small lit tag ("AS EXPECTED", "DEVIATION"). */
    TextView tag(String s, int color) {
        TextView t = text(s.toUpperCase(T.L), 10, Theme.uiBold(a), color);
        t.setLetterSpacing(0.16f);
        t.setPadding(dp(8), dp(4), dp(8), dp(4));
        t.setBackground(Theme.box(a, (color & 0x00FFFFFF) | 0x1F000000, (color & 0x00FFFFFF) | 0x73000000, 7));
        return t;
    }

    // ---------------------------------------------------------------- numbers

    /** The three number sizes used everywhere (dp): large for the headline result and pick, medium, small for lists. */
    static final float NUM_L = 44, NUM_M = 36, NUM_S = 30;

    /** A combination as rounded-square tiles, the same for every lottery; the extra number in its own colour. */
    LinearLayout balls(Game g, int[] nums, int extra, float sizeDp) {
        return balls(g, nums, extra, sizeDp, sizeDp >= NUM_M ? 6 : 5);
    }

    LinearLayout balls(Game g, int[] nums, int extra, float sizeDp, int gapDp) {
        sizeDp = fit(sizeDp, nums.length + (extra >= 0 ? 1 : 0), gapDp);
        LinearLayout row = hbox();
        row.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < nums.length; i++) {
            String s = g.digits() ? String.valueOf(nums[i]) : String.format(Locale.US, "%02d", nums[i]);
            row.addView(ball(s, true, Theme.AMBER, sizeDp), sized(sizeDp, i == 0 ? 0 : gapDp));
        }
        if (extra >= 0) {
            String s = g.digits() ? String.valueOf(extra) : String.format(Locale.US, "%02d", extra);
            int color = g.digits() ? Theme.COOL : Theme.CORAL;
            row.addView(ball(s, true, color, sizeDp), sized(sizeDp, gapDp * 2));
        }
        return row;
    }

    /** The largest tile size up to `sizeDp` that lets `n` tiles fit the card's width on this screen. */
    float fit(float sizeDp, int n, int gapDp) {
        android.util.DisplayMetrics m = a.getResources().getDisplayMetrics();
        float avail = m.widthPixels / m.density - 64; // screen minus the page and card margins
        float gaps = (n - 1) * gapDp + gapDp;            // the extra number has a double gap
        return Math.max(22, Math.min(sizeDp, (avail - gaps) / n));
    }

    private LinearLayout.LayoutParams sized(float sizeDp, int leftDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp));
        lp.leftMargin = dp(leftDp);
        return lp;
    }

    TextView ball(String s, boolean tile, int color, float sizeDp) {
        TextView t = text(s, sizeDp * 0.44f, Theme.monoBold(a), Theme.LED);
        t.setGravity(Gravity.CENTER);
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(tile ? GradientDrawable.RECTANGLE : GradientDrawable.OVAL);
        if (tile) gd.setCornerRadius(dp(sizeDp * 0.26f));
        gd.setColor((color & 0x00FFFFFF) | 0x2E000000);
        gd.setStroke(Math.max(1, dp(1.2f)), (color & 0x00FFFFFF) | 0xCC000000);
        t.setBackground(gd);
        t.setIncludeFontPadding(false);
        return t;
    }

    /** Colour for a p-value: green when expected, amber for a raw blip, coral for a corrected deviation. */
    static int zColor(double z) {
        if (z >= 2) return Theme.AMBER;
        if (z >= 1) return Theme.AMBER_LIGHT;
        if (z <= -2) return Theme.COOL;
        if (z <= -1) return 0xFFA9CCEA;
        return Theme.TEXT_SOFT;
    }
}
