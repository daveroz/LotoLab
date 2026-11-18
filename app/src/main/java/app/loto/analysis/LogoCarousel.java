package app.loto.analysis;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import java.io.InputStream;

/**
 * The lotteries' logos in a row you swipe through: the chosen one large in the middle, its neighbours smaller and
 * dimmer at the edges, wrapping around. Swipe left or right, or tap a side logo. The logos are the SVG files in
 * assets/logos/<lottery>.svg (dorado.svg, baloto.svg, miloto.svg), drawn by {@link Svg} in the theme's light:
 * a one-colour logo takes the warm-white to amber gradient, keeping its exact shapes and proportions.
 */
final class LogoCarousel extends View {
    interface Listener { void onSelected(int index); }

    private final String[] subtitles, names;
    private final Drawable[] logos;
    private final float[] aspect;
    private final int[] colors;
    private final Listener listener;
    private final float d;
    private final Paint sub = new Paint(Paint.ANTI_ALIAS_FLAG), glow = new Paint(Paint.ANTI_ALIAS_FLAG),
            dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int touchSlop;
    private int index;
    /** Offset of the row in items while dragging or animating (+1 = moved one item to the right). */
    private float offset;
    private float downX, downY;
    private boolean dragging;
    private VelocityTracker vt;
    private ValueAnimator anim;

    LogoCarousel(Context c, String[] keys, String[] subtitles, int[] colors, int index, Listener l) {
        super(c);
        this.names = keys;
        this.subtitles = subtitles;
        this.colors = colors;
        this.listener = l;
        this.index = index;
        d = getResources().getDisplayMetrics().density;
        touchSlop = ViewConfiguration.get(c).getScaledTouchSlop();
        logos = new Drawable[keys.length];
        aspect = new float[keys.length];
        for (int i = 0; i < keys.length; i++) {
            String file = "logos/" + keys[i].toLowerCase(java.util.Locale.ROOT) + ".svg";
            try (InputStream in = c.getAssets().open(file)) {
                Svg svg = Svg.themed(Data.read(in).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                if (svg.shapeCount() > 0) {
                    logos[i] = svg.drawable();
                    aspect[i] = svg.aspect();
                }
            } catch (Exception e) {
                android.util.Log.w("LotoLab", file + " couldn't be drawn", e);
            }
            if (logos[i] == null) { // no usable logo: a plain lit disc in its place
                GradientDrawable g = new GradientDrawable();
                g.setShape(GradientDrawable.OVAL);
                g.setColor(colors[i]);
                logos[i] = g;
                aspect[i] = 1;
            }
        }
        sub.setTypeface(Theme.uiBold(c));
        sub.setTextAlign(Paint.Align.CENTER);
        sub.setColor(Theme.AMBER_LIGHT);
        sub.setTextSize(11 * d);
        sub.setLetterSpacing(0.18f);
        setClickable(true);
        setFocusable(true);
        updateDescription();
    }

    int index() { return index; }

    private void updateDescription() {
        setContentDescription(T.f("Lottery: %s. Swipe left or right to change.", names[index]));
    }

    @Override
    protected void onMeasure(int w, int h) {
        setMeasuredDimension(MeasureSpec.getSize(w), Math.round(150 * d));
    }

    private int wrap(int i) {
        int n = names.length;
        return ((i % n) + n) % n;
    }

    /** The logo's box at full size: two thirds of the width, at most 280dp wide and 92dp tall. */
    private float[] box(int i) {
        float w = Math.min(getWidth() * 0.66f, 280 * d), h = w / aspect[i];
        if (h > 92 * d) {
            h = 92 * d;
            w = h * aspect[i];
        }
        return new float[]{w, h};
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), cx = w / 2, cy = 58 * d;
        float spacing = w * 0.6f;
        int near = wrap(index - Math.round(offset));
        float frac = Math.abs(offset - Math.round(offset));
        // the light behind the logo in the middle, wider than tall like the logos
        int col = colors[near];
        float r = 100 * d;
        glow.setShader(new RadialGradient(0, 0, r, (col & 0x00FFFFFF) | ((int) (0x55 * (1 - frac)) << 24),
                col & 0x00FFFFFF, Shader.TileMode.CLAMP));
        c.save();
        c.translate(cx, cy);
        c.scale(1.7f, 0.75f);
        c.drawCircle(0, 0, r, glow);
        c.restore();
        int[] order = {-2, 2, -1, 1, 0};
        for (int k : order) {
            float pos = k + offset;
            if (Math.abs(pos) > 2.2f) continue;
            int i = wrap(index + k);
            float a = Math.min(1, Math.abs(pos));
            float scale = 1 - 0.45f * a;
            int alpha = (int) (255 * (1 - 0.62f * a));
            if (Math.abs(pos) > 1.4f) alpha = (int) (alpha * Math.max(0, (2.2f - Math.abs(pos)) / 0.8f));
            float[] b = box(i);
            float x = cx + pos * spacing, bw = b[0] * scale, bh = b[1] * scale;
            Drawable logo = logos[i];
            logo.setBounds(Math.round(x - bw / 2), Math.round(cy - bh / 2), Math.round(x + bw / 2), Math.round(cy + bh / 2));
            logo.setAlpha(alpha);
            logo.draw(c);
            if (Math.abs(pos) < 0.6f) {
                sub.setAlpha((int) (255 * (1 - Math.abs(pos) / 0.6f)));
                c.drawText(subtitles[i].toUpperCase(T.L), x, cy + 46 * d + 20 * d, sub);
            }
        }
        float dy = getHeight() - 8 * d, gap = 12 * d, x0 = cx - gap * (names.length - 1) / 2;
        for (int i = 0; i < names.length; i++) {
            boolean on = i == near;
            dot.setColor(on ? Theme.AMBER : 0x55AADCFF);
            c.drawCircle(x0 + gap * i, dy, (on ? 3.4f : 2.6f) * d, dot);
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
                    offset = Math.max(-1.3f, Math.min(1.3f, dx / (getWidth() * 0.6f)));
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
                    if (offset < -0.25f || vx < -350 * d) target = -1;
                    else if (offset > 0.25f || vx > 350 * d) target = 1;
                    settle(target);
                } else if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                    float w = getWidth();
                    if (e.getX() < w * 0.22f) settle(1);
                    else if (e.getX() > w * 0.78f) settle(-1);
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
