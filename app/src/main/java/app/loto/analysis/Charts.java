package app.loto.analysis;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import java.util.Locale;

/**
 * Charts drawn in the key light: bars glow softly, the fair-draw expectation is a dashed line of light, values
 * above expectation are warm and those below it cool. Each view has a fixed height and fills the card's width.
 */
final class Charts {
    private Charts() {}

    static int warm(double z) {
        if (z >= 2) return Theme.AMBER;
        if (z >= 1) return Theme.AMBER_MID;
        if (z <= -2) return Theme.COOL;
        if (z <= -1) return 0xFF6B9CC4;
        return 0xFF8B98A8;
    }

    private abstract static class Base extends View {
        final float d;
        final int heightPx;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint txt = new Paint(Paint.ANTI_ALIAS_FLAG);

        Base(Context c, float heightDp) {
            super(c);
            d = getResources().getDisplayMetrics().density;
            heightPx = Math.round(heightDp * d);
            setLayerType(LAYER_TYPE_SOFTWARE, null); // the blur of the glow needs it
            glow.setMaskFilter(new BlurMaskFilter(4 * d, BlurMaskFilter.Blur.NORMAL));
            txt.setTypeface(Theme.monoMedium(c));
            txt.setColor(Theme.MUTED);
            txt.setTextSize(9 * d);
            txt.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        protected void onMeasure(int w, int h) {
            setMeasuredDimension(MeasureSpec.getSize(w), heightPx);
        }

        void dashed(Canvas c, float x0, float x1, float y, int color) {
            Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
            line.setStyle(Paint.Style.STROKE);
            line.setStrokeWidth(1.2f * d);
            line.setColor(color);
            line.setPathEffect(new DashPathEffect(new float[]{4 * d, 3 * d}, 0));
            c.drawLine(x0, y, x1, y, line);
        }
    }

    /** Chance: the four positions side by side, one cell per digit, warm or cool by how far from fair. */
    static final class Heat extends Base {
        private final Analysis.Cell[][] cells;

        Heat(Context c, Analysis.Cell[][] cells) {
            super(c, 10 * 25 + 22);
            this.cells = cells;
            txt.setTextSize(10 * d);
        }

        @Override
        protected void onDraw(Canvas c) {
            int cols = cells.length;
            float w = getWidth(), head = 18 * d, labelW = 22 * d;
            float cw = (w - labelW) / cols, ch = (getHeight() - head) / 10f;
            Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
            for (int col = 0; col < cols; col++) {
                txt.setColor(Theme.MUTED);
                c.drawText(Analysis.pos(col), labelW + cw * col + cw / 2, 12 * d, txt);
            }
            for (int v = 0; v < 10; v++) {
                txt.setColor(Theme.DIM);
                c.drawText(String.valueOf(v), labelW / 2, head + ch * v + ch / 2 + 3.5f * d, txt);
                for (int col = 0; col < cols; col++) {
                    Analysis.Cell cell = cells[col][v];
                    double z = cell.z;
                    int base = z >= 0 ? Theme.AMBER : Theme.COOL;
                    int alpha = (int) Math.min(200, 26 + Math.abs(z) * 60);
                    fill.setColor((base & 0x00FFFFFF) | (alpha << 24));
                    RectF r = new RectF(labelW + cw * col + 2 * d, head + ch * v + 2 * d, labelW + cw * (col + 1) - 2 * d,
                            head + ch * (v + 1) - 2 * d);
                    c.drawRoundRect(r, 5 * d, 5 * d, fill);
                    txt.setColor(Math.abs(z) >= 2 ? Theme.LED : Theme.TEXT_SOFT);
                    c.drawText(String.format(Locale.US, "%d", Math.round(cell.observed)), r.centerX(), r.centerY() + 3.5f * d, txt);
                }
            }
        }
    }
}
