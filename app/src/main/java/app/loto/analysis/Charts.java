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

    /** One bar per value against the expected count of a fair draw. */
    static final class Bars extends Base {
        private final String[] labels;
        private final double[] obs, exp, z;
        private final int labelEvery;

        Bars(Context c, String[] labels, double[] obs, double[] exp, double[] z, int labelEvery) {
            super(c, 150);
            this.labels = labels;
            this.obs = obs;
            this.exp = exp;
            this.z = z;
            this.labelEvery = Math.max(1, labelEvery);
        }

        @Override
        protected void onDraw(Canvas c) {
            int n = obs.length;
            if (n == 0) return;
            float w = getWidth(), h = getHeight(), bottom = h - 14 * d, top = 8 * d;
            double max = 0;
            for (int i = 0; i < n; i++) max = Math.max(max, Math.max(obs[i], exp[i]));
            if (max <= 0) max = 1;
            max *= 1.1;
            float slot = w / n, bw = Math.max(1.5f * d, slot * 0.62f);
            for (int i = 0; i < n; i++) {
                float x = slot * i + (slot - bw) / 2;
                float y = (float) (bottom - (bottom - top) * obs[i] / max);
                int col = warm(z[i]);
                glow.setColor((col & 0x00FFFFFF) | 0x55000000);
                c.drawRoundRect(new RectF(x, y, x + bw, bottom), bw / 3, bw / 3, glow);
                p.setColor((col & 0x00FFFFFF) | 0xDD000000);
                c.drawRoundRect(new RectF(x, y, x + bw, bottom), bw / 3, bw / 3, p);
                if (i % labelEvery == 0 && !labels[i].isEmpty()) c.drawText(labels[i], slot * i + slot / 2, h - 3 * d, txt);
            }
            // expected level: a line of light (the same for every value in a frequency chart)
            boolean flat = true;
            for (int i = 1; i < n; i++) if (Math.abs(exp[i] - exp[0]) > 1e-9) flat = false;
            if (flat) {
                float y = (float) (bottom - (bottom - top) * exp[0] / max);
                dashed(c, 0, w, y, Theme.LED & 0xCCFFFFFF);
            } else {
                Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
                dot.setColor(Theme.LED);
                for (int i = 0; i < n; i++) {
                    float y = (float) (bottom - (bottom - top) * exp[i] / max);
                    c.drawCircle(slot * i + slot / 2, y, 2.2f * d, dot);
                }
            }
        }
    }

    /** Observed draws per category (bars) against a fair draw's expectation (lit dots joined by a line). */
    static final class Dist extends Base {
        private final String[] labels;
        private final double[] obs, exp;

        Dist(Context c, String[] labels, double[] obs, double[] exp) {
            super(c, 140);
            this.labels = labels;
            this.obs = obs;
            this.exp = exp;
        }

        @Override
        protected void onDraw(Canvas c) {
            int n = obs.length;
            if (n == 0) return;
            float w = getWidth(), h = getHeight(), bottom = h - 16 * d, top = 10 * d;
            double max = 0;
            for (int i = 0; i < n; i++) max = Math.max(max, Math.max(obs[i], exp[i]));
            if (max <= 0) max = 1;
            max *= 1.12;
            float slot = w / n, bw = slot * 0.6f;
            for (int i = 0; i < n; i++) {
                float x = slot * i + (slot - bw) / 2;
                float y = (float) (bottom - (bottom - top) * obs[i] / max);
                glow.setColor(0x44EB9438);
                c.drawRoundRect(new RectF(x, y, x + bw, bottom), 4 * d, 4 * d, glow);
                p.setColor(0xB0E08A33);
                c.drawRoundRect(new RectF(x, y, x + bw, bottom), 4 * d, 4 * d, p);
                c.drawText(labels[i], slot * i + slot / 2, h - 4 * d, txt);
            }
            Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
            line.setColor(Theme.LED & 0xAAFFFFFF);
            line.setStrokeWidth(1.3f * d);
            Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
            dot.setColor(Theme.LED);
            float px = 0, py = 0;
            for (int i = 0; i < n; i++) {
                float x = slot * i + slot / 2, y = (float) (bottom - (bottom - top) * exp[i] / max);
                if (i > 0) c.drawLine(px, py, x, y, line);
                c.drawCircle(x, y, 2.6f * d, dot);
                px = x;
                py = y;
            }
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

    /** The randomness score as a lit arc. */
    static final class Gauge extends Base {
        private final int score;
        private final int color;

        Gauge(Context c, int score, int color) {
            super(c, 132);
            this.score = score;
            this.color = color;
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            float r = Math.min(w / 2, h * 0.62f) - 10 * d;
            float cx = w / 2, cy = h * 0.62f;
            RectF box = new RectF(cx - r, cy - r, cx + r, cy + r);
            Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
            track.setStyle(Paint.Style.STROKE);
            track.setStrokeCap(Paint.Cap.ROUND);
            track.setStrokeWidth(9 * d);
            track.setColor(0x22AADCFF);
            c.drawArc(box, 160, 220, false, track);
            if (score >= 0) {
                float sweep = 220 * score / 100f;
                Paint g = new Paint(Paint.ANTI_ALIAS_FLAG);
                g.setStyle(Paint.Style.STROKE);
                g.setStrokeCap(Paint.Cap.ROUND);
                g.setStrokeWidth(14 * d);
                g.setColor((color & 0x00FFFFFF) | 0x55000000);
                g.setMaskFilter(new BlurMaskFilter(7 * d, BlurMaskFilter.Blur.NORMAL));
                c.drawArc(box, 160, sweep, false, g);
                track.setColor(color);
                c.drawArc(box, 160, sweep, false, track);
            }
            Paint big = new Paint(Paint.ANTI_ALIAS_FLAG);
            big.setTypeface(Theme.monoBold(getContext()));
            big.setTextAlign(Paint.Align.CENTER);
            big.setTextSize(38 * d);
            big.setColor(Theme.LED);
            c.drawText(score >= 0 ? String.valueOf(score) : "–", cx, cy + 4 * d, big);
            txt.setTextSize(10 * d);
            txt.setColor(Theme.MUTED);
            c.drawText(T.t("OF 100"), cx, cy + 20 * d, txt);
        }
    }

    /** Groups a long distribution (sums of lotto draws) into about `bins` wider bins. */
    static double[][] rebin(double[] obs, double[] exp, int bins, String[] outLabels) {
        int first = 0, last = exp.length - 1;
        while (first < last && exp[first] == 0) first++;
        while (last > first && exp[last] == 0) last--;
        // trim tails holding less than 0.5% of the expectation so the bins cover the interesting part
        double total = 0;
        for (double v : exp) total += v;
        double acc = 0;
        while (first < last && acc + exp[first] < total * 0.005) acc += exp[first++];
        acc = 0;
        while (last > first && acc + exp[last] < total * 0.005) acc += exp[last--];
        int width = Math.max(1, (int) Math.ceil((last - first + 1) / (double) bins));
        int n = (int) Math.ceil((last - first + 1) / (double) width);
        double[] o = new double[n], e = new double[n];
        for (int i = 0; i < obs.length; i++) {
            int b = Math.max(0, Math.min(n - 1, (i - first) / width));
            o[b] += obs[i];
            e[b] += exp[i];
        }
        for (int b = 0; b < n && b < outLabels.length; b++) outLabels[b] = String.valueOf(first + b * width + width / 2);
        return new double[][]{o, e, {n}};
    }
}
