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

    /** A glass panel over the stage, with its title in spaced capitals in the key light. */
    LinearLayout card(LinearLayout parent, String title) {
        LinearLayout c = vbox();
        c.setBackground(Theme.glass(a, 16));
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        if (title != null) {
            TextView t = text(title, 11.5f, Theme.uiBold(a), Theme.AMBER_LIGHT);
            Theme.label(a, t);
            t.setTextSize(11.5f);
            t.setPadding(dp(2), 0, 0, dp(10));
            c.addView(t);
        }
        parent.addView(c, matchWrap(14));
        return c;
    }

    /** A card whose title carries what kind of figures it holds: descriptive, theoretical or predictive evidence. */
    LinearLayout card(LinearLayout parent, String title, String kind) {
        LinearLayout c = card(parent, null);
        LinearLayout head = hbox();
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(2), 0, 0, dp(10));
        TextView t = text(title, 11.5f, Theme.uiBold(a), Theme.AMBER_LIGHT);
        Theme.label(a, t);
        t.setTextSize(11.5f);
        head.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (kind != null) head.addView(tag(kind, Theme.COOL), wrap(8));
        c.addView(head);
        return c;
    }

    /**
     * Details that stay folded until tapped, so a card shows its key finding first and keeps the depth one tap
     * away. Returns the container to fill.
     */
    LinearLayout details(LinearLayout parent, String title) {
        final LinearLayout box = vbox();
        box.setVisibility(View.GONE);
        final TextView head = text("▸  " + title, 13, Theme.uiSemi(a), Theme.AMBER_LIGHT);
        head.setPadding(0, dp(10), 0, dp(6));
        head.setClickable(true);
        head.setOnClickListener(v -> {
            boolean open = box.getVisibility() != View.VISIBLE;
            box.setVisibility(open ? View.VISIBLE : View.GONE);
            head.setText((open ? "▾  " : "▸  ") + title);
        });
        parent.addView(head, matchWrap(4));
        parent.addView(box, matchWrap(0));
        return box;
    }

    /** A choice: lit like a lamp when it's the chosen one, a hairline outline otherwise. */
    TextView chip(String label, boolean on, View.OnClickListener l) {
        TextView b = text(label, 13, Theme.uiSemi(a), on ? 0xFFFFFFFF : Theme.TEXT_SOFT);
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
        return chipRow(parent, topDp, false);
    }

    /** center: the chips sit in the middle when they fit (the draws under the lottery's emblem). */
    LinearLayout chipRow(LinearLayout parent, int topDp, boolean center) {
        HorizontalScrollView hs = new HorizontalScrollView(a);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setClipToPadding(false);
        hs.setFillViewport(center);
        LinearLayout row = hbox();
        if (center) row.setGravity(Gravity.CENTER_HORIZONTAL);
        hs.addView(row, new ViewGroup.LayoutParams(center ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
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

    /** A status dot, a title and its explanation. */
    LinearLayout status(int color, String title, String detail) {
        LinearLayout row = hbox();
        row.setPadding(0, dp(6), 0, dp(6));
        TextView dot = text("●", 12, Typeface.DEFAULT, color);
        dot.setPadding(0, dp(2), dp(10), 0);
        row.addView(dot);
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
        t.setPadding(dp(8), dp(3), dp(8), dp(3));
        t.setBackground(Theme.box(a, (color & 0x00FFFFFF) | 0x22000000, (color & 0x00FFFFFF) | 0x88000000, 8));
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

    /** Individual number boxes for entering a combination: one per digit or ball, the extra one in its own colour. */
    static final class Slots {
        final LinearLayout row;
        final EditText[] boxes;
        Slots(LinearLayout row, EditText[] boxes) {
            this.row = row;
            this.boxes = boxes;
        }
        String[] values() {
            String[] v = new String[boxes.length];
            for (int i = 0; i < v.length; i++) v[i] = boxes[i].getText().toString().trim();
            return v;
        }
    }

    /**
     * `n` boxes taking `maxLen` digits each, sharing the width equally so they fit narrow screens; typing a full box
     * moves to the next, deleting in an empty box moves back. `extraAt` is the index of the extra-number box, or -1.
     */
    Slots slots(int n, int maxLen, int extraAt, String[] initial) {
        LinearLayout row = hbox();
        final EditText[] boxes = new EditText[n];
        for (int i = 0; i < n; i++) {
            final int idx = i;
            final EditText e = new EditText(a);
            e.setInputType(InputType.TYPE_CLASS_NUMBER);
            e.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(maxLen)});
            e.setSingleLine(true);
            e.setGravity(Gravity.CENTER);
            e.setTextSize(maxLen > 1 ? 19 : 22);
            e.setTypeface(Theme.monoBold(a));
            e.setTextColor(Theme.LED);
            e.setHintTextColor(Theme.DIM);
            e.setHint(maxLen > 1 ? "—" : "·");
            e.setPadding(0, dp(10), 0, dp(10));
            e.setSelectAllOnFocus(true);
            int color = i == extraAt ? Theme.CORAL : Theme.AMBER;
            e.setBackground(slotBox(color, false));
            e.setOnFocusChangeListener((v, focused) -> e.setBackground(slotBox(color, focused)));
            if (initial != null && i < initial.length && initial[i] != null) e.setText(initial[i]);
            e.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int af) { }
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }
                @Override public void afterTextChanged(android.text.Editable s) {
                    if (s.length() >= maxLen && idx + 1 < boxes.length) boxes[idx + 1].requestFocus();
                }
            });
            e.setOnKeyListener((v, code, ev) -> {
                if (code == android.view.KeyEvent.KEYCODE_DEL && ev.getAction() == android.view.KeyEvent.ACTION_DOWN
                        && e.getText().length() == 0 && idx > 0) {
                    boxes[idx - 1].requestFocus();
                    boxes[idx - 1].setText("");
                    return true;
                }
                return false;
            });
            boxes[i] = e;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(54), 1f);
            lp.leftMargin = i == 0 ? 0 : dp(i == extraAt ? 12 : 6);
            row.addView(e, lp);
        }
        return new Slots(row, boxes);
    }

    private GradientDrawable slotBox(int color, boolean focused) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(12));
        g.setColor((color & 0x00FFFFFF) | (focused ? 0x33000000 : 0x1A000000));
        g.setStroke(Math.max(1, dp(focused ? 1.6f : 1.1f)), (color & 0x00FFFFFF) | (focused ? 0xFF000000 : 0x99000000));
        return g;
    }

    /** Marks a slot as wrong (coral edge) or right. */
    void markSlot(EditText e, boolean bad) {
        GradientDrawable g = slotBox(bad ? Theme.CORAL : Theme.AMBER, bad);
        e.setBackground(g);
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
