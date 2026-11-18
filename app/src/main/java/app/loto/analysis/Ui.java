package app.loto.analysis;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/** Building blocks of every screen, in the Worldly look: glass cards over the stage, lit chips, spaced labels. */
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

    /** Body copy. */
    TextView body(String s) {
        TextView t = text(s, 13, Theme.uiMedium(a), Theme.TEXT_SOFT);
        t.setLineSpacing(0, 1.12f);
        return t;
    }

    /** Quiet explanatory text. */
    TextView note(String s) {
        TextView t = text(s, 12, Theme.uiMedium(a), Theme.DIM);
        t.setLineSpacing(0, 1.1f);
        return t;
    }

    TextView label(String s, int color) {
        TextView t = text(s, 10, Theme.uiBold(a), color);
        Theme.label(a, t);
        t.setTextSize(10);
        return t;
    }

    /** A glass panel over the stage, with its title in spaced capitals in the key light. */
    LinearLayout card(LinearLayout parent, String title) {
        LinearLayout c = vbox();
        c.setBackground(Theme.glass(a, 16));
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        if (title != null) {
            TextView t = text(title, 11, Theme.uiBold(a), Theme.AMBER_LIGHT);
            Theme.label(a, t);
            t.setPadding(dp(2), 0, 0, dp(10));
            c.addView(t);
        }
        parent.addView(c, matchWrap(14));
        return c;
    }

    /** A choice: lit like a lamp when it's the chosen one, a hairline outline otherwise. */
    TextView chip(String label, boolean on, View.OnClickListener l) {
        TextView b = text(label, 12, Theme.uiSemi(a), on ? 0xFFFFFFFF : Theme.TEXT_SOFT);
        b.setGravity(Gravity.CENTER);
        b.setSingleLine(true);
        b.setPadding(dp(14), dp(11), dp(14), dp(11));
        b.setBackground(on ? Theme.lit(a, 14, false)
                : Theme.pressable(Theme.box(a, Theme.GLASS, Theme.BORDER_STRONG, 14), Theme.well(a, 14, true)));
        b.setClickable(true);
        b.setOnClickListener(l);
        return b;
    }

    /** A row of chips that scrolls sideways when it doesn't fit. */
    LinearLayout chipRow(LinearLayout parent, int topDp) {
        HorizontalScrollView hs = new HorizontalScrollView(a);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setClipToPadding(false);
        LinearLayout row = hbox();
        hs.addView(row);
        parent.addView(hs, matchWrap(topDp));
        return row;
    }

    TextView primaryButton(String label, View.OnClickListener l) {
        TextView b = text(label, 13, Theme.uiBold(a), 0xFFFFFFFF);
        Theme.label(a, b);
        b.setTextSize(12);
        b.setLetterSpacing(0.18f);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), dp(15), dp(16), dp(15));
        b.setBackground(Theme.pressable(Theme.lit(a, 16, false), Theme.lit(a, 16, true)));
        b.setClickable(true);
        b.setOnClickListener(l);
        return b;
    }

    TextView secondaryButton(String label, View.OnClickListener l) {
        TextView b = text(label, 12, Theme.uiSemi(a), Theme.TEXT_SOFT);
        Theme.label(a, b);
        b.setTextSize(11);
        b.setLetterSpacing(0.14f);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14), dp(12), dp(14), dp(12));
        b.setBackground(Theme.pressable(Theme.box(a, Theme.GLASS, Theme.BORDER_STRONG, 14), Theme.well(a, 14, true)));
        b.setClickable(true);
        b.setOnClickListener(l);
        return b;
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
        TextView n = text(name, 13, Theme.uiMedium(a), Theme.TEXT);
        row.addView(n, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView v = text(value, 13, Theme.monoSemi(a), valueColor);
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

    /** A status dot, a title and its explanation. */
    LinearLayout status(int color, String title, String detail) {
        LinearLayout row = hbox();
        row.setPadding(0, dp(6), 0, dp(6));
        TextView dot = text("●", 12, Typeface.DEFAULT, color);
        dot.setPadding(0, dp(2), dp(10), 0);
        row.addView(dot);
        LinearLayout txt = vbox();
        txt.addView(text(title, 14, Theme.uiSemi(a), Theme.TEXT));
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
        TextView t = text(s.toUpperCase(Locale.US), 9, Theme.uiBold(a), color);
        t.setLetterSpacing(0.16f);
        t.setPadding(dp(8), dp(3), dp(8), dp(3));
        t.setBackground(Theme.box(a, (color & 0x00FFFFFF) | 0x22000000, (color & 0x00FFFFFF) | 0x88000000, 8));
        return t;
    }

    // ---------------------------------------------------------------- numbers

    /** A combination as balls (lotto) or digit tiles (chance), with the extra ball in its own colour. */
    LinearLayout balls(Game g, int[] nums, int extra, float sizeDp) {
        return balls(g, nums, extra, sizeDp, sizeDp >= 32 ? 6 : 4);
    }

    LinearLayout balls(Game g, int[] nums, int extra, float sizeDp, int gapDp) {
        LinearLayout row = hbox();
        row.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < nums.length; i++) {
            String s = g.digits() ? String.valueOf(nums[i]) : String.format(Locale.US, "%02d", nums[i]);
            row.addView(ball(s, g.digits(), Theme.AMBER, sizeDp), sized(sizeDp, i == 0 ? 0 : gapDp));
        }
        if (extra >= 0) {
            String s = g.digits() ? String.valueOf(extra) : String.format(Locale.US, "%02d", extra);
            int color = g.digits() ? Theme.COOL : Theme.CORAL;
            row.addView(ball(s, g.digits(), color, sizeDp), sized(sizeDp, gapDp * 2));
        }
        return row;
    }

    private LinearLayout.LayoutParams sized(float sizeDp, int leftDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp));
        lp.leftMargin = dp(leftDp);
        return lp;
    }

    /** Scrolls a chip row (from {@link #chipRow}) so the chip at `index` is in view. */
    void reveal(LinearLayout row, int index) {
        row.post(() -> {
            if (index < 0 || index >= row.getChildCount() || !(row.getParent() instanceof HorizontalScrollView)) return;
            View c = row.getChildAt(index);
            HorizontalScrollView hs = (HorizontalScrollView) row.getParent();
            int x = c.getLeft() - dp(24);
            if (c.getRight() - hs.getScrollX() > hs.getWidth() || c.getLeft() < hs.getScrollX()) hs.scrollTo(Math.max(0, x), 0);
        });
    }

    TextView ball(String s, boolean tile, int color, float sizeDp) {
        TextView t = text(s, sizeDp * 0.42f, Theme.monoBold(a), Theme.LED);
        t.setGravity(Gravity.CENTER);
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(tile ? GradientDrawable.RECTANGLE : GradientDrawable.OVAL);
        if (tile) gd.setCornerRadius(dp(8));
        gd.setColor((color & 0x00FFFFFF) | 0x2E000000);
        gd.setStroke(Math.max(1, dp(1.2f)), (color & 0x00FFFFFF) | 0xCC000000);
        t.setBackground(gd);
        t.setIncludeFontPadding(false);
        return t;
    }

    /** Colour for a p-value: green when expected, amber for a raw blip, coral for a corrected deviation. */
    static int pColor(TestResult t) {
        if (!t.enough) return Theme.DIM;
        if (t.flagged()) return Theme.CORAL;
        if (t.p < 0.05) return Theme.AMBER;
        return Theme.GREEN;
    }

    /** Colour for a z score: warm for above expectation, cool for below, neutral near it. */
    static int zColor(double z) {
        if (z >= 2) return Theme.AMBER;
        if (z >= 1) return Theme.AMBER_LIGHT;
        if (z <= -2) return Theme.COOL;
        if (z <= -1) return 0xFFA9CCEA;
        return Theme.TEXT_SOFT;
    }
}
