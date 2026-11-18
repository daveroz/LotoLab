package app.loto.analysis;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import java.io.InputStream;

/**
 * The lotteries as a row of emblems you swipe through, the chosen one large in the middle with its name, its
 * neighbours smaller and dimmer at the sides. Swipe left or right (it wraps around), or tap a side emblem.
 * Each emblem glows in its lottery's colour, as a light in the Worldly haze. An official logo can replace the
 * built-in emblem: put it at assets/logos/<lottery>.png (e.g. logos/baloto.png).
 */
final class LogoCarousel extends View {
    interface Listener { void onSelected(int index); }

    private final String[] names;
    private final String[] subtitles;
    private final Drawable[] logos;
    private final int[] colors;
    private final Listener listener;
    private final float d;
    private final Paint name = new Paint(Paint.ANTI_ALIAS_FLAG), sub = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG), dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int touchSlop;
    private int index;
    /** Offset of the row in items, while dragging or animating (+1 = moved one item to the right). */
    private float offset;
    private float downX, downY;
    private boolean dragging;
    private VelocityTracker vt;
    private ValueAnimator anim;

    LogoCarousel(Context c, String[] keys, String[] names, String[] subtitles, int[] logoRes, int[] colors, int index, Listener l) {
        super(c);
        this.names = names;
        this.subtitles = subtitles;
        this.colors = colors;
        this.listener = l;
        this.index = index;
        d = getResources().getDisplayMetrics().density;
        touchSlop = ViewConfiguration.get(c).getScaledTouchSlop();
        logos = new Drawable[keys.length];
        for (int i = 0; i < keys.length; i++) {
            Drawable custom = null;
            try (InputStream in = c.getAssets().open("logos/" + keys[i].toLowerCase(java.util.Locale.ROOT) + ".png")) {
                Bitmap b = BitmapFactory.decodeStream(in);
                if (b != null) custom = new BitmapDrawable(getResources(), b);
            } catch (Exception ignored) { } // no official logo supplied: the built-in emblem
            Drawable builtIn = null;
            if (custom == null) {
                try {
                    builtIn = c.getDrawable(logoRes[i]);
                } catch (RuntimeException e) {
                    android.util.Log.e("LotoLab", "emblem " + keys[i] + " couldn't be loaded", e);
                }
            }
            logos[i] = custom != null ? custom : builtIn != null ? builtIn : disc(colors[i]);
        }
        name.setTypeface(Theme.uiBold(c));
        name.setTextAlign(Paint.Align.CENTER);
        name.setColor(Theme.TEXT);
        name.setTextSize(18 * d);
        name.setLetterSpacing(0.04f);
        sub.setTypeface(Theme.uiBold(c));
        sub.setTextAlign(Paint.Align.CENTER);
        sub.setColor(Theme.AMBER_LIGHT);
        sub.setTextSize(9.5f * d);
        sub.setLetterSpacing(0.2f);
        setClickable(true);
        setFocusable(true);
        updateDescription();
    }

    int index() { return index; }

    /** A plain lit disc in the lottery's colour, if its emblem can't be drawn. */
    private static Drawable disc(int color) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        g.setColor(color);
        return g;
    }

    private void updateDescription() {
        setContentDescription(T.f("Lottery: %s. Swipe left or right to change.", names[index]));
    }

    @Override
    protected void onMeasure(int w, int h) {
        setMeasuredDimension(MeasureSpec.getSize(w), Math.round(176 * d));
    }

    private int wrap(int i) {
        int n = names.length;
        return ((i % n) + n) % n;
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), cx = w / 2, cy = 70 * d;
        float spacing = Math.min(w * 0.33f, 150 * d), size = 98 * d;
        int n = names.length;
        // the glow of the light behind the centre, blending the two lotteries while moving between them
        int near = wrap(index - Math.round(offset));
        float frac = Math.abs(offset - Math.round(offset));
        int col = colors[near];
        float r = 92 * d;
        glow.setShader(new RadialGradient(cx, cy, r, (col & 0x00FFFFFF) | ((int) (0x66 * (1 - frac)) << 24),
                col & 0x00FFFFFF, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, glow);
        // side items first, the centre last so it's on top
        int[] order = {-2, 2, -1, 1, 0};
        for (int k : order) {
            float pos = k + offset;
            int i = wrap(index + k);
            if (Math.abs(pos) > 2.2f) continue;
            float a = Math.min(1, Math.abs(pos));
            float scale = 1 - 0.42f * a;
            int alpha = (int) (255 * (1 - 0.6f * Math.min(1, Math.abs(pos))));
            if (Math.abs(pos) > 1.4f) alpha = (int) (alpha * Math.max(0, (2.2f - Math.abs(pos)) / 0.8f));
            float x = cx + pos * spacing, s = size * scale;
            Drawable logo = logos[i];
            logo.setBounds(Math.round(x - s / 2), Math.round(cy - s / 2), Math.round(x + s / 2), Math.round(cy + s / 2));
            logo.setAlpha(alpha);
            logo.draw(c);
            if (Math.abs(pos) < 0.6f) {
                int ta = (int) (255 * (1 - Math.abs(pos) / 0.6f));
                name.setAlpha(ta);
                sub.setAlpha(ta);
                c.drawText(names[i], x, cy + size / 2 + 24 * d, name);
                c.drawText(subtitles[i].toUpperCase(T.L), x, cy + size / 2 + 40 * d, sub);
            }
        }
        // page dots
        float dy = getHeight() - 6 * d, gap = 10 * d, x0 = cx - gap * (n - 1) / 2;
        for (int i = 0; i < n; i++) {
            boolean on = i == near;
            dot.setColor(on ? Theme.AMBER : 0x55AADCFF);
            c.drawCircle(x0 + gap * i, dy, (on ? 3.2f : 2.4f) * d, dot);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (vt == null) vt = VelocityTracker.obtain();
        vt.addMovement(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (anim != null) anim.cancel();
                downX = e.getX();
                downY = e.getY();
                dragging = false;
                return true;
            case MotionEvent.ACTION_MOVE: {
                float dx = e.getX() - downX;
                if (!dragging && Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(e.getY() - downY)) {
                    dragging = true;
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (dragging) {
                    float spacing = Math.min(getWidth() * 0.33f, 150 * d);
                    offset = Math.max(-1.3f, Math.min(1.3f, dx / spacing));
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                vt.computeCurrentVelocity(1000);
                float vx = vt.getXVelocity();
                vt.recycle();
                vt = null;
                if (dragging) {
                    int target = 0;
                    if (offset < -0.3f || vx < -900 * d / 2.6f) target = -1;
                    else if (offset > 0.3f || vx > 900 * d / 2.6f) target = 1;
                    settle(target);
                } else if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                    float w = getWidth();
                    if (e.getX() < w * 0.3f) settle(1);
                    else if (e.getX() > w * 0.7f) settle(-1);
                    else performClick();
                }
                dragging = false;
                return true;
            }
            default:
                return true;
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /** Moves to the neighbour (-1: the next one, from the right; +1: the previous one) or back to the centre. */
    private void settle(final int target) {
        anim = ValueAnimator.ofFloat(offset, target);
        anim.setDuration(240);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            offset = (float) a.getAnimatedValue();
            invalidate();
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            boolean cancelled;
            @Override public void onAnimationCancel(android.animation.Animator a) { cancelled = true; }
            @Override public void onAnimationEnd(android.animation.Animator a) {
                if (cancelled) return;
                offset = 0;
                if (target != 0) {
                    index = wrap(index - target);
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                    updateDescription();
                    if (listener != null) listener.onSelected(index);
                }
                invalidate();
            }
        });
        anim.start();
    }
}
