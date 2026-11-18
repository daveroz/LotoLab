package app.loto.analysis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The surfaces of every screen, as in Fuji-App's crystal style, lit by Worldly's stage.
 *
 * {@link Pane}: liquid glass, kept quiet and a little solid: charcoal that only just lets the stage through, a touch
 * lighter at the top left, deeper at the bottom, a faint sheen, film grain, and a hairline rim that's brightest at
 * the corners that catch the light. It picks up the stage's key light behind it (where the key light is on the
 * screen, the glass glows a little warmer) and its rim warms where the light falls. Its tones: a card, a lighter lens
 * (the chosen segment), a recessed well (behind a control), and a round bubble (small actions).
 *
 * {@link Key}: the one solid amber action. {@link Lit}: something that's on, an amber wash with the light bleeding in
 * from a glowing amber edge, all within its bounds, so a chosen chip is never bigger than the others.
 * {@link Lamp}: a status light, a hot core blooming into the haze.
 */
final class Glass {
    private Glass() {}

    /** A control's corner (buttons, chips, the switch), in dp. */
    static final float CONTROL_RADIUS_DP = 12;
    /** A bubble's corner, as a share of its size: a rounded square, not a circle. */
    static final float ROUND_SHARE = 0.27f;

    /** A rectangle with rounded corners of {@code radius} (reduced to fit): the one shape the app uses. */
    static void rounded(Path path, RectF r, float radius) {
        path.reset();
        if (r.width() <= 0 || r.height() <= 0) return;
        float rad = Math.max(0, Math.min(radius, Math.min(r.width(), r.height()) / 2));
        path.addRoundRect(r, rad, rad, Path.Direction.CW);
    }

    // ---------------------------------------------------------------- the stage's light, as glass picks it up

    /**
     * Where the stage's key light is: high on the right, as {@link StageView} draws it, and its fill low on the left.
     * Glass takes the part of that light that's behind it. The lights drift so slowly that their resting places are
     * used; glass is redrawn when the page scrolls ({@link #refresh}).
     */
    private static final Set<Pane> PANES = Collections.newSetFromMap(new WeakHashMap<>());

    /** Redraw every pane of glass (after the page scrolled), so each shows the light now behind it. */
    static void refresh() {
        for (Pane p : new ArrayList<>(PANES)) p.invalidateSelf();
    }

    /** The view a drawable is drawn for, through any state list around it. */
    static View hostOf(Drawable d) {
        Object cb = d.getCallback();
        while (cb instanceof Drawable) cb = ((Drawable) cb).getCallback();
        return cb instanceof View ? (View) cb : null;
    }

    /** The key light's strength at a point of the window (0 to about 0.16), as the stage lays it down. */
    static float keyAt(float x, float y, int w, int h) {
        float big = Math.max(w, h), r = big * 0.42f;
        float d = (float) Math.hypot(x - w * 0.82f, y - h * 0.15f);
        return d >= r ? 0 : 0.16f * (1 - d / r);
    }

    private static Bitmap grainTile;

    /** The stage's film grain, as a tile glass lays over itself. */
    static synchronized Bitmap grain() {
        if (grainTile == null) grainTile = StageView.grain();
        return grainTile;
    }

    // ---------------------------------------------------------------- glass

    enum Tone { PANE, LENS, WELL, BUBBLE }

    /** A pane of glass; see the class notes. {@code radius} in pixels; a bubble rounds by its size. */
    static final class Pane extends Drawable {
        private final float density, radius;
        private final Tone tone;
        private final boolean pressed;
        private final RectF rect = new RectF(), rimRect = new RectF();
        private final Path shape = new Path(), rimShape = new Path();
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint sheen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint depth = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint light = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint grain = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint key = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Matrix m = new Matrix();
        private final int[] loc = new int[2];
        /** The key light and the fill, as round glows of radius 1, placed where they are on the screen at each draw. */
        private final Shader keyLight = new RadialGradient(0, 0, 1, new int[]{0x26EB9438, 0x0FEB9438, 0x00EB9438},
                new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP);
        private final Shader fillLight = new RadialGradient(0, 0, 1, 0x12EB9438, 0x00EB9438, Shader.TileMode.CLAMP);
        private float rimWarmth = -1;

        Pane(Context c, float radius, Tone tone, boolean pressed) {
            density = c.getResources().getDisplayMetrics().density;
            this.radius = radius;
            this.tone = tone;
            this.pressed = pressed;
            grain.setShader(new BitmapShader(grain(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
            grain.setAlpha(tone == Tone.WELL ? 60 : 90);
            key.setColor(0x2EEB9438);
            rim.setStyle(Paint.Style.STROKE);
            rim.setStrokeWidth(Math.max(1, density * 0.6f));
            if (tone == Tone.PANE || tone == Tone.BUBBLE) PANES.add(this);
        }

        @Override
        protected void onBoundsChange(Rect b) {
            rect.set(b);
            float inset = rim.getStrokeWidth() / 2;
            rimRect.set(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset);
            rounded(shape, rect, corner(rect));
            rounded(rimShape, rimRect, Math.max(0, corner(rimRect) - inset));
            float w = rect.width(), h = rect.height();
            int top, mid, bottom;
            switch (tone) {
                case LENS: top = 0x29FFFFFF; mid = 0x21FFFFFF; bottom = 0x1CFFFFFF; break;
                case WELL: top = 0x99000000; mid = 0x8C000000; bottom = 0x85000000; break;
                case BUBBLE: top = 0xF21B1C20; mid = 0xF3161719; bottom = 0xF5121316; break;
                default: top = 0xDE191A1E; mid = 0xE3131417; bottom = 0xEB0F1013;
            }
            // charcoal, barely cool: lighter at the top left, deepest at the bottom right
            fill.setShader(new LinearGradient(rect.left, rect.top, rect.left + w * 0.35f, rect.bottom, new int[]{top, mid, bottom},
                    new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
            sheen.setShader(tone == Tone.WELL ? null : new RadialGradient(rect.left + w * 0.15f, rect.top, Math.max(1, Math.max(w, h) * 0.75f),
                    tone == Tone.LENS ? 0x0DFFFFFF : 0x08FFFFFF, 0, Shader.TileMode.CLAMP));
            depth.setShader(tone == Tone.WELL
                    ? new LinearGradient(0, rect.top, 0, rect.top + Math.min(h, 14 * density), 0x66000000, 0, Shader.TileMode.CLAMP)
                    : new LinearGradient(0, rect.bottom - Math.min(h * 0.4f, 36 * density), 0, rect.bottom, 0, 0x1F000000, Shader.TileMode.CLAMP));
            if (tone == Tone.LENS) {
                // the key light, faintly, under a lens
                light.setShader(new RadialGradient(rect.centerX(), rect.bottom + h * 0.2f, Math.max(1, Math.max(w, h) * 0.7f),
                        0x0FEB9438, 0, Shader.TileMode.CLAMP));
            }
            rimWarmth = -1;
        }

        private float corner(RectF r) {
            if (tone == Tone.BUBBLE) return Math.min(r.width(), r.height()) * ROUND_SHARE;
            return radius < 0 ? CONTROL_RADIUS_DP * density : radius;
        }

        @Override
        public void draw(Canvas canvas) {
            if (rect.isEmpty()) return;
            canvas.drawPath(shape, fill);
            float warm = 0;
            View host = tone == Tone.PANE || tone == Tone.BUBBLE ? hostOf(this) : null;
            if (host != null && host.getRootView() != null) {
                // the stage's light behind this glass: the key high on the right, the fill low on the left
                host.getLocationInWindow(loc);
                View root = host.getRootView();
                int w = root.getWidth(), h = root.getHeight();
                if (w > 0 && h > 0) {
                    float big = Math.max(w, h);
                    m.setScale(big * 0.42f, big * 0.42f);
                    m.postTranslate(w * 0.82f - loc[0], h * 0.15f - loc[1]);
                    keyLight.setLocalMatrix(m);
                    light.setShader(keyLight);
                    canvas.drawPath(shape, light);
                    m.setScale(big * 0.4f, big * 0.4f);
                    m.postTranslate(w * 0.16f - loc[0], h * 0.9f - loc[1]);
                    fillLight.setLocalMatrix(m);
                    light.setShader(fillLight);
                    canvas.drawPath(shape, light);
                    warm = Math.min(0.25f, 2 * keyAt(loc[0] + rect.right - 12 * density, loc[1] + rect.top + 12 * density, w, h));
                }
            } else if (tone == Tone.LENS) {
                canvas.drawPath(shape, light);
            }
            if (pressed) canvas.drawPath(shape, key);
            if (sheen.getShader() != null) canvas.drawPath(shape, sheen);
            canvas.drawPath(shape, depth);
            canvas.drawPath(shape, grain);
            if (pressed) warm = 0.6f;
            if (Math.abs(warm - rimWarmth) > 0.03f) {
                rimWarmth = warm;
                rim.setShader(rimShader(warm));
            }
            canvas.drawPath(rimShape, rim);
        }

        /** A hairline, brightest at the corners that catch the light; at the top right it takes the key's warmth. */
        private Shader rimShader(float warm) {
            int strong = tone == Tone.WELL ? 0x12 : tone == Tone.BUBBLE ? 0x33 : tone == Tone.LENS ? 0x40 : 0x36;
            int soft = tone == Tone.WELL ? 0x0A : tone == Tone.BUBBLE ? 0x21 : 0x1A;
            int edge = (strong << 24) | 0xFFFFFF, faint = (soft << 24) | 0xFFFFFF, mid = (((strong + soft) / 2) << 24) | 0xFFFFFF;
            int corner = faint;
            if (warm > 0.01f) {
                int a = Math.max(0, Math.min(255, Math.round(soft + (0x48 - soft) * warm / 0.6f)));
                corner = (a << 24) | 0xFFD2A6;
            }
            // clockwise from the right: bottom right, bottom left and top left catch the light; the top right is warm
            return new SweepGradient(rimRect.centerX(), rimRect.centerY(),
                    new int[]{faint, mid, faint, mid, faint, edge, faint, corner, faint},
                    new float[]{0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f});
        }

        @Override public void setAlpha(int alpha) { }
        @Override public void setColorFilter(ColorFilter cf) { }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    // ---------------------------------------------------------------- the key action

    /** The key action: solid amber, a rounded square, lit a little from above. Text on it is {@link #INK}. */
    static final class Key extends Drawable {
        static final int INK = 0xFF1A0F04;
        private final float density, radius;
        private final boolean pressed;
        private final Path path = new Path();
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint glint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Key(Context c, float radius, boolean pressed) {
            density = c.getResources().getDisplayMetrics().density;
            this.radius = radius;
            this.pressed = pressed;
        }

        @Override
        protected void onBoundsChange(Rect b) {
            RectF r = new RectF(b);
            rounded(path, r, radius < 0 ? CONTROL_RADIUS_DP * density : radius);
            p.setShader(new LinearGradient(0, r.top, 0, r.bottom, pressed ? 0xFFE99A4C : 0xFFF2A65C, pressed ? Theme.AMBER_DEEP : 0xFFDB862F,
                    Shader.TileMode.CLAMP));
            // a warm glint along the top, the light catching the amber
            glint.setShader(new RadialGradient(r.centerX(), r.top, Math.max(1, r.width() * 0.6f), 0x33FFF4E6, 0, Shader.TileMode.CLAMP));
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawPath(path, p);
            canvas.drawPath(path, glint);
        }

        @Override public void setAlpha(int alpha) { }
        @Override public void setColorFilter(ColorFilter cf) { }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    // ---------------------------------------------------------------- lit

    /**
     * Something that's on: an amber wash on the glass, the light bleeding in from a glowing amber edge (Worldly's
     * bloom, kept inside), and the edge itself. It fills its bounds exactly. Drawn once into a picture per size,
     * since a blur can't be drawn live everywhere.
     */
    static final class Lit extends Drawable {
        private final float radius, line;
        private final int fill;
        private Bitmap picture;

        Lit(Context c, float radiusDp, boolean pressed) {
            float d = c.getResources().getDisplayMetrics().density;
            radius = radiusDp * d;
            line = Math.max(1, 1.2f * d);
            fill = pressed ? Theme.KEY_GLASS_HI : Theme.KEY_GLASS;
        }

        @Override
        protected void onBoundsChange(Rect b) {
            picture = null;
        }

        @Override
        public void draw(Canvas canvas) {
            Rect b = getBounds();
            if (b.width() <= 0 || b.height() <= 0) return;
            if (picture == null) {
                picture = Bitmap.createBitmap(b.width(), b.height(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(picture);
                RectF r = new RectF(0, 0, b.width(), b.height());
                Path shape = new Path();
                rounded(shape, r, radius);
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
                p.setColor(fill);
                c.drawPath(shape, p);
                // the light bleeding in from the edge, deeper amber, as light seen through haze
                c.save();
                c.clipPath(shape);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(line * 6);
                p.setColor(0x47D8750B);
                p.setMaskFilter(new BlurMaskFilter(line * 4, BlurMaskFilter.Blur.NORMAL));
                c.drawPath(shape, p);
                p.setMaskFilter(null);
                // and a warm glint low in the middle, where the light pools
                p.setStyle(Paint.Style.FILL);
                p.setShader(new RadialGradient(r.centerX(), r.bottom, Math.max(1, Math.max(r.width(), r.height()) * 0.6f), 0x1FFFB64E, 0,
                        Shader.TileMode.CLAMP));
                c.drawRect(r, p);
                p.setShader(null);
                c.restore();
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(line);
                p.setColor(Theme.KEY_EDGE);
                float h = line / 2;
                Path edge = new Path();
                rounded(edge, new RectF(h, h, r.right - h, r.bottom - h), Math.max(0, radius - h));
                c.drawPath(edge, p);
            }
            canvas.drawBitmap(picture, b.left, b.top, null);
        }

        @Override public void setAlpha(int alpha) { }
        @Override public void setColorFilter(ColorFilter cf) { }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    // ---------------------------------------------------------------- a lamp

    /** A status lamp: a near-white core in its colour, blooming into the haze. Give the view about three times the core. */
    static final class Lamp extends Drawable {
        private final int color;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        Lamp(int color) {
            this.color = color;
        }

        @Override
        protected void onBoundsChange(Rect b) {
            float r = Math.min(b.width(), b.height()) / 2f;
            if (r <= 0) return;
            int rgb = color & 0x00FFFFFF;
            p.setShader(new RadialGradient(b.exactCenterX(), b.exactCenterY(), r,
                    new int[]{0xFFFFF4E6, color | 0xFF000000, color | 0xFF000000, (0x73 << 24) | rgb, (0x1A << 24) | rgb, rgb},
                    new float[]{0f, 0.12f, 0.3f, 0.38f, 0.66f, 1f}, Shader.TileMode.CLAMP));
        }

        @Override
        public void draw(Canvas canvas) {
            Rect b = getBounds();
            canvas.drawCircle(b.exactCenterX(), b.exactCenterY(), Math.min(b.width(), b.height()) / 2f, p);
        }

        @Override public void setAlpha(int alpha) { }
        @Override public void setColorFilter(ColorFilter cf) { }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
