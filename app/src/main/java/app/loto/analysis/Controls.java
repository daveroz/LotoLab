package app.loto.analysis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/** The two controls made in code, as in Fuji-App: an on/off switch, and choices laid out as equal cells. */
final class Controls {
    private Controls() {}

    /** An on/off switch: a glass well, and a warm knob on an amber wash when on. It fills its bounds. */
    static final class Switch extends View {
        private final float d;
        private final Glass.Pane well;
        private final Paint on = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint knob = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Path track = new Path(), knobOn = new Path(), knobOff = new Path();
        private Shader knobOnShader, knobOffShader;
        private boolean checked;

        Switch(Context c) {
            super(c);
            d = c.getResources().getDisplayMetrics().density;
            well = new Glass.Pane(c, -1, Glass.Tone.WELL, false);
            well.setCallback(this);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); // its row speaks for it
        }

        void setChecked(boolean value) {
            checked = value;
            invalidate();
        }

        @Override
        protected boolean verifyDrawable(Drawable who) {
            return who == well || super.verifyDrawable(who);
        }

        @Override
        protected void onMeasure(int w, int h) {
            setMeasuredDimension(Math.round(50 * d), Math.round(30 * d));
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            RectF r = new RectF(0, 0, w, h);
            well.setBounds(0, 0, w, h);
            float rad = r.height() / 2, kr = rad - 3 * d, ky = r.centerY();
            on.setShader(new LinearGradient(0, 0, 0, h, 0x99EB9438, 0x66C97624, Shader.TileMode.CLAMP));
            knobOnShader = new RadialGradient(r.right - rad, ky - kr * 0.6f, kr * 1.8f, 0xFFFFF8EF, 0xFFF0D2AE, Shader.TileMode.CLAMP);
            knobOffShader = new RadialGradient(r.left + rad, ky - kr * 0.6f, kr * 1.8f, 0xFF4E5157, 0xFF1E1F23, Shader.TileMode.CLAMP);
            Glass.rounded(track, r, Glass.CONTROL_RADIUS_DP * d);
            RectF k = new RectF(r.right - rad - kr, ky - kr, r.right - rad + kr, ky + kr);
            Glass.rounded(knobOn, k, kr * 2 * Glass.ROUND_SHARE);
            k.offsetTo(r.left + rad - kr, ky - kr);
            Glass.rounded(knobOff, k, kr * 2 * Glass.ROUND_SHARE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            well.draw(canvas);
            if (checked) canvas.drawPath(track, on);
            knob.setShader(checked ? knobOnShader : knobOffShader);
            canvas.drawPath(checked ? knobOn : knobOff, knob);
        }
    }

    /**
     * Equal cells in even rows: every child the same width and {@code cellHeight}, edge to edge with {@code gap}
     * between them, in the fewest rows whose cells stay at least 56 dp wide (and at most five to a row).
     */
    static final class ChipGrid extends ViewGroup {
        private static final int MAX_COLUMNS = 5;
        private final int gap, cellHeight, minCell;
        private int cols = 1, cellWidth;

        ChipGrid(Context c, int gap, int cellHeight) {
            super(c);
            this.gap = gap;
            this.cellHeight = cellHeight;
            minCell = Math.round(56 * c.getResources().getDisplayMetrics().density);
        }

        private List<View> kids() {
            List<View> out = new ArrayList<>();
            for (int i = 0; i < getChildCount(); i++) if (getChildAt(i).getVisibility() != GONE) out.add(getChildAt(i));
            return out;
        }

        @Override
        protected void onMeasure(int widthSpec, int heightSpec) {
            List<View> kids = kids();
            int n = kids.size();
            if (n == 0) {
                setMeasuredDimension(0, 0);
                return;
            }
            int width = MeasureSpec.getSize(widthSpec) - getPaddingLeft() - getPaddingRight();
            cols = 1;
            for (int rows = 1; rows <= n; rows++) {
                int c = (n + rows - 1) / rows;
                if (c <= MAX_COLUMNS && (width - gap * (c - 1)) / c >= minCell) {
                    cols = c;
                    break;
                }
            }
            cellWidth = Math.max(0, (width - gap * (cols - 1)) / cols);
            int rows = (n + cols - 1) / cols;
            for (View v : kids) {
                v.measure(MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY));
            }
            setMeasuredDimension(width + getPaddingLeft() + getPaddingRight(),
                    rows * cellHeight + (rows - 1) * gap + getPaddingTop() + getPaddingBottom());
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int i = 0;
            for (View v : kids()) {
                int x = getPaddingLeft() + (i % cols) * (cellWidth + gap);
                int y = getPaddingTop() + (i / cols) * (cellHeight + gap);
                v.layout(x, y, x + cellWidth, y + cellHeight);
                i++;
            }
        }
    }
}
