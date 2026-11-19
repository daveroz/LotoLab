package app.loto.analysis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Solid icons, drawn in code as in Fuji-App: filled shapes with their details cut out, plus a few bold strokes, in
 * units of the icon's size from its centre.
 */
final class Icon extends View {
    enum Kind { CHEVRON_RIGHT, PLUS }

    private final Glyph glyph;

    Icon(Context c, Kind kind, int color) {
        super(c);
        glyph = new Glyph(kind, color);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setTint(int color) {
        glyph.color = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        glyph.setBounds(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        glyph.draw(canvas);
    }

    /** The icon as a drawable, for a button's compound drawable. */
    static final class Glyph extends Drawable {
        private final Kind kind;
        int color;
        private final Paint solid = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint bold = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path shape = new Path(), strokes = new Path();
        private float s, ox, oy;

        Glyph(Kind kind, int color) {
            this.kind = kind;
            this.color = color;
            solid.setStyle(Paint.Style.FILL);
            bold.setStyle(Paint.Style.STROKE);
            bold.setStrokeCap(Paint.Cap.ROUND);
            bold.setStrokeJoin(Paint.Join.ROUND);
        }

        @Override
        protected void onBoundsChange(Rect b) {
            build(b.width(), b.height());
        }

        @Override
        public void draw(Canvas canvas) {
            Rect b = getBounds();
            if (b.width() <= 0 || b.height() <= 0) return;
            solid.setColor(color);
            bold.setColor(color);
            canvas.save();
            canvas.translate(b.left, b.top);
            canvas.drawPath(shape, solid);
            canvas.drawPath(strokes, bold);
            canvas.restore();
        }

        private float x(float u) { return ox + u * s; }
        private float y(float u) { return oy + u * s; }

        private Path rr(float l, float t, float r, float b, float rad) {
            Path p = new Path();
            p.addRoundRect(new RectF(x(l), y(t), x(r), y(b)), rad * s, rad * s, Path.Direction.CW);
            return p;
        }

        private Path circle(float cx, float cy, float r) {
            Path p = new Path();
            p.addCircle(x(cx), y(cy), r * s, Path.Direction.CW);
            return p;
        }

        private Path poly(float... pts) {
            Path p = new Path();
            p.moveTo(x(pts[0]), y(pts[1]));
            for (int i = 2; i < pts.length; i += 2) p.lineTo(x(pts[i]), y(pts[i + 1]));
            p.close();
            return p;
        }

        private void add(Path p) { shape.op(p, Path.Op.UNION); }
        private void cut(Path p) { shape.op(p, Path.Op.DIFFERENCE); }

        private void line(float x0, float y0, float x1, float y1) {
            strokes.moveTo(x(x0), y(y0));
            strokes.lineTo(x(x1), y(y1));
        }

        private void arc(float cx, float cy, float r, float start, float sweep) {
            strokes.arcTo(new RectF(x(cx - r), y(cy - r), x(cx + r), y(cy + r)), start, sweep, true);
        }

        private void build(float w, float h) {
            shape.reset();
            strokes.reset();
            if (w <= 0 || h <= 0) return;
            s = Math.min(w, h);
            ox = w / 2;
            oy = h / 2;
            bold.setStrokeWidth(Math.max(2, s * 0.12f));
            switch (kind) {
                case CHEVRON_RIGHT:
                    bold.setStrokeWidth(Math.max(2.4f, s * 0.15f));
                    line(-0.12f, -0.28f, 0.15f, 0);
                    line(0.15f, 0, -0.12f, 0.28f);
                    break;
                case PLUS:
                    bold.setStrokeWidth(Math.max(2.2f, s * 0.13f));
                    line(-0.3f, 0, 0.3f, 0);
                    line(0, -0.3f, 0, 0.3f);
                    break;
            }
        }

        @Override public void setAlpha(int alpha) { }
        @Override public void setColorFilter(ColorFilter cf) { }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
