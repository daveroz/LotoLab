package app.loto.analysis;

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
import android.graphics.drawable.Drawable;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A small SVG renderer (no libraries, like the rest of the app) for the lottery logos in assets/logos/*.svg.
 * It draws paths (every command, arcs included), rect, circle, ellipse, line, polyline and polygon, nested
 * groups with transforms, opacity, linear and radial gradients and clip paths. Text isn't drawn: convert text
 * to paths when exporting a logo. Every colour can be passed through a {@link Recolor}, which is how the logos
 * take on the app's amber light while keeping their shapes and proportions.
 */
final class Svg {
    /** Maps a colour (ARGB) to another. */
    interface Recolor { int map(int argb); }

    /**
     * The app's theme: each colour becomes the tone of the same brightness on a ramp from the night through the
     * amber key light to its warm white, alpha kept. Light and dark parts stay light and dark, so the design reads
     * the same, only in the app's light.
     */
    static final Recolor THEME = argb -> {
        int a = argb >>> 24;
        float r = ((argb >> 16) & 0xFF) / 255f, g = ((argb >> 8) & 0xFF) / 255f, b = (argb & 0xFF) / 255f;
        float l = 0.2126f * r + 0.7152f * g + 0.0722f * b;
        int[] ramp = {0xFF161C28, 0xFF5A3410, 0xFFB86A1E, 0xFFEB9438, 0xFFF4BC7C, 0xFFFFF4E6};
        float[] at = {0f, 0.18f, 0.38f, 0.58f, 0.8f, 1f};
        int i = 0;
        while (i < at.length - 2 && l > at[i + 1]) i++;
        float t = Math.max(0, Math.min(1, (l - at[i]) / (at[i + 1] - at[i])));
        return (a << 24) | (mixRgb(ramp[i], ramp[i + 1], t) & 0x00FFFFFF);
    };

    private static int mixRgb(int c0, int c1, float t) {
        int r = Math.round(((c0 >> 16) & 0xFF) + (((c1 >> 16) & 0xFF) - ((c0 >> 16) & 0xFF)) * t);
        int g = Math.round(((c0 >> 8) & 0xFF) + (((c1 >> 8) & 0xFF) - ((c0 >> 8) & 0xFF)) * t);
        int b = Math.round((c0 & 0xFF) + ((c1 & 0xFF) - (c0 & 0xFF)) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static final class Shape {
        Path path;
        Paint fill, stroke;
        Path clip;
    }

    private static final class Gradient {
        boolean radial, userSpace;
        float x1, y1, x2 = 1, y2, cx = 0.5f, cy = 0.5f, r = 0.5f;
        final List<Float> offsets = new ArrayList<>();
        final List<Integer> colors = new ArrayList<>();
        String href;
        Matrix transform;
    }

    /** The drawing attributes a group passes on to its children. */
    private static final class Style {
        String fill = "#000000", stroke = "none";
        float strokeWidth = 1, opacity = 1, fillOpacity = 1, strokeOpacity = 1;
        boolean evenOdd;
        Matrix m = new Matrix();
        Path clip;

        Style copy() {
            Style s = new Style();
            s.fill = fill;
            s.stroke = stroke;
            s.strokeWidth = strokeWidth;
            s.opacity = opacity;
            s.fillOpacity = fillOpacity;
            s.strokeOpacity = strokeOpacity;
            s.evenOdd = evenOdd;
            s.m = new Matrix(m);
            s.clip = clip;
            return s;
        }
    }

    final RectF viewBox = new RectF(0, 0, 100, 100);
    private final List<Shape> shapes = new ArrayList<>();
    private final Map<String, Gradient> gradients = new HashMap<>();
    private final Map<String, Path> clips = new HashMap<>();
    private final List<Object[]> pending = new ArrayList<>(); // shapes whose gradient is defined later
    private final Recolor recolor;

    private Svg(Recolor recolor) {
        this.recolor = recolor;
    }

    static Svg parse(InputStream in, Recolor recolor) throws Exception {
        Svg svg = new Svg(recolor);
        XmlPullParser p = Xml.newPullParser();
        p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        p.setInput(in, null);
        Deque<Style> styles = new ArrayDeque<>();
        styles.push(new Style());
        Gradient grad = null;
        String clipId = null;
        Path clipPath = null;
        int defsDepth = 0;
        for (int ev = p.getEventType(); ev != XmlPullParser.END_DOCUMENT; ev = p.next()) {
            if (ev == XmlPullParser.START_TAG) {
                String tag = local(p.getName());
                Style parent = styles.peek();
                Style st = parent.copy();
                apply(p, st);
                String cp = p.getAttributeValue(null, "clip-path");
                if (cp != null && cp.startsWith("url(#") && cp.indexOf(')') > 5) {
                    Path c = svg.clips.get(cp.substring(5, cp.indexOf(')')));
                    if (c != null) st.clip = c;
                }
                switch (tag) {
                    case "svg": {
                        String vb = p.getAttributeValue(null, "viewBox");
                        if (vb != null) {
                            float[] v = numbers(vb);
                            if (v.length == 4) svg.viewBox.set(v[0], v[1], v[0] + v[2], v[1] + v[3]);
                        } else {
                            float w = len(p.getAttributeValue(null, "width"), 100), h = len(p.getAttributeValue(null, "height"), 100);
                            svg.viewBox.set(0, 0, w, h);
                        }
                        break;
                    }
                    case "defs":
                    case "symbol":
                    case "mask":
                        defsDepth++;
                        break;
                    case "linearGradient":
                    case "radialGradient":
                        grad = new Gradient();
                        grad.radial = tag.equals("radialGradient");
                        grad.userSpace = "userSpaceOnUse".equals(p.getAttributeValue(null, "gradientUnits"));
                        grad.x1 = coord(p, "x1", 0);
                        grad.y1 = coord(p, "y1", 0);
                        grad.x2 = coord(p, "x2", grad.userSpace ? 0 : 1);
                        grad.y2 = coord(p, "y2", 0);
                        grad.cx = coord(p, "cx", 0.5f);
                        grad.cy = coord(p, "cy", 0.5f);
                        grad.r = coord(p, "r", 0.5f);
                        String gt = p.getAttributeValue(null, "gradientTransform");
                        if (gt != null) grad.transform = transform(gt);
                        String href = p.getAttributeValue(null, "xlink:href");
                        if (href == null) href = p.getAttributeValue(null, "href");
                        if (href != null && href.startsWith("#")) grad.href = href.substring(1);
                        String id = p.getAttributeValue(null, "id");
                        if (id != null) svg.gradients.put(id, grad);
                        break;
                    case "stop":
                        if (grad != null) {
                            String off = p.getAttributeValue(null, "offset");
                            float o = off == null ? 0 : off.trim().endsWith("%") ? num(off.trim().replace("%", "")) / 100f : num(off);
                            Map<String, String> sm = styleMap(p.getAttributeValue(null, "style"));
                            String col = first(sm.get("stop-color"), p.getAttributeValue(null, "stop-color"), "#000000");
                            String op = first(sm.get("stop-opacity"), p.getAttributeValue(null, "stop-opacity"), "1");
                            grad.offsets.add(Math.max(0, Math.min(1, o)));
                            grad.colors.add(withAlpha(color(col), num(op)));
                        }
                        break;
                    case "clipPath":
                        clipId = p.getAttributeValue(null, "id");
                        clipPath = new Path();
                        break;
                    default: {
                        Path shape = geometry(tag, p);
                        if (shape == null) break;
                        shape.transform(st.m);
                        if (clipPath != null) {
                            clipPath.addPath(shape);
                        } else if (defsDepth == 0) {
                            svg.add(shape, st);
                        }
                    }
                }
                styles.push(st);
            } else if (ev == XmlPullParser.END_TAG) {
                String tag = local(p.getName());
                styles.pop();
                if (tag.equals("defs") || tag.equals("symbol") || tag.equals("mask")) defsDepth--;
                if (tag.equals("linearGradient") || tag.equals("radialGradient")) grad = null;
                if (tag.equals("clipPath")) {
                    if (clipId != null) svg.clips.put(clipId, clipPath);
                    clipId = null;
                    clipPath = null;
                }
            }
        }
        svg.resolvePending();
        return svg;
    }

    private void add(Path shape, Style st) {
        Shape s = new Shape();
        s.path = shape;
        s.path.setFillType(st.evenOdd ? Path.FillType.EVEN_ODD : Path.FillType.WINDING);
        s.clip = st.clip;
        if (!"none".equals(st.fill)) {
            s.fill = new Paint(Paint.ANTI_ALIAS_FLAG);
            s.fill.setStyle(Paint.Style.FILL);
            paint(s.fill, st.fill, st.opacity * st.fillOpacity, shape, st.m);
        }
        if (!"none".equals(st.stroke) && st.strokeWidth > 0) {
            s.stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            s.stroke.setStyle(Paint.Style.STROKE);
            s.stroke.setStrokeWidth(st.strokeWidth * scaleOf(st.m));
            s.stroke.setStrokeCap(Paint.Cap.ROUND);
            s.stroke.setStrokeJoin(Paint.Join.ROUND);
            paint(s.stroke, st.stroke, st.opacity * st.strokeOpacity, shape, st.m);
        }
        shapes.add(s);
    }

    private static float scaleOf(Matrix m) {
        float[] v = new float[9];
        m.getValues(v);
        return (float) Math.sqrt(Math.abs(v[Matrix.MSCALE_X] * v[Matrix.MSCALE_Y] - v[Matrix.MSKEW_X] * v[Matrix.MSKEW_Y]));
    }

    private void paint(Paint paint, String spec, float opacity, Path shape, Matrix m) {
        spec = spec.trim();
        if (spec.startsWith("url(#")) {
            String id = spec.substring(5, spec.indexOf(')'));
            Gradient g = gradients.get(id);
            if (g == null) {
                pending.add(new Object[]{paint, id, opacity, shape, new Matrix(m)});
                paint.setColor(0);
                return;
            }
            shade(paint, g, opacity, shape, m);
            return;
        }
        int c = color(spec);
        float a = ((c >>> 24) / 255f) * opacity;
        paint.setColor(withAlpha(recolor == null ? c : recolor.map(c), a));
    }

    private void resolvePending() {
        for (Object[] o : pending) {
            Gradient g = gradients.get((String) o[1]);
            if (g != null) shade((Paint) o[0], g, (Float) o[2], (Path) o[3], (Matrix) o[4]);
        }
    }

    private void shade(Paint paint, Gradient g, float opacity, Path shape, Matrix m) {
        Gradient stops = g;
        int guard = 0;
        while (stops.colors.isEmpty() && stops.href != null && gradients.containsKey(stops.href) && guard++ < 8) stops = gradients.get(stops.href);
        int n = stops.colors.size();
        if (n == 0) {
            paint.setColor(0);
            return;
        }
        int[] cols = new int[Math.max(2, n)];
        float[] pos = new float[Math.max(2, n)];
        for (int i = 0; i < n; i++) {
            int c = stops.colors.get(i);
            int mapped = recolor == null ? c : (recolor.map(c) & 0x00FFFFFF) | (c & 0xFF000000);
            cols[i] = withAlpha(mapped, ((c >>> 24) / 255f) * opacity);
            pos[i] = stops.offsets.get(i);
        }
        if (n == 1) {
            cols[1] = cols[0];
            pos[1] = 1;
        }
        Shader sh;
        if (g.radial) sh = new RadialGradient(g.cx, g.cy, Math.max(0.0001f, g.r), cols, pos, Shader.TileMode.CLAMP);
        else sh = new LinearGradient(g.x1, g.y1, g.x2, g.y2, cols, pos, Shader.TileMode.CLAMP);
        Matrix lm = new Matrix();
        if (g.userSpace) {
            if (g.transform != null) lm.set(g.transform);
            lm.postConcat(m);
        } else {
            // objectBoundingBox: gradient coordinates 0..1 span the shape's bounds (already transformed)
            RectF b = new RectF();
            shape.computeBounds(b, true);
            if (g.transform != null) lm.set(g.transform);
            lm.postScale(Math.max(1e-3f, b.width()), Math.max(1e-3f, b.height()));
            lm.postTranslate(b.left, b.top);
        }
        sh.setLocalMatrix(lm);
        paint.setShader(sh);
        paint.setColor(0xFFFFFFFF);
    }

    /** Draws the picture into the bounds, centred and keeping its proportions. */
    void draw(Canvas c, Rect bounds, int alpha) {
        if (bounds.width() <= 0 || bounds.height() <= 0) return;
        float s = Math.min(bounds.width() / viewBox.width(), bounds.height() / viewBox.height());
        float dx = bounds.left + (bounds.width() - viewBox.width() * s) / 2 - viewBox.left * s;
        float dy = bounds.top + (bounds.height() - viewBox.height() * s) / 2 - viewBox.top * s;
        int save = c.save();
        c.translate(dx, dy);
        c.scale(s, s);
        for (Shape sh : shapes) {
            if (sh.clip != null) {
                c.save();
                c.clipPath(sh.clip);
            }
            if (sh.fill != null) {
                int a = sh.fill.getAlpha();
                sh.fill.setAlpha(a * alpha / 255);
                c.drawPath(sh.path, sh.fill);
                sh.fill.setAlpha(a);
            }
            if (sh.stroke != null) {
                int a = sh.stroke.getAlpha();
                sh.stroke.setAlpha(a * alpha / 255);
                c.drawPath(sh.path, sh.stroke);
                sh.stroke.setAlpha(a);
            }
            if (sh.clip != null) c.restore();
        }
        c.restoreToCount(save);
    }

    int shapeCount() { return shapes.size(); }

    /** Width over height of the picture. */
    float aspect() { return viewBox.height() > 0 ? viewBox.width() / viewBox.height() : 1; }

    /** Whether every shape is filled with one flat colour (a silhouette logo) and nothing is stroked or shaded. */
    boolean monochrome() {
        Integer c = null;
        for (Shape sh : shapes) {
            if (sh.stroke != null) return false;
            if (sh.fill == null) continue;
            if (sh.fill.getShader() != null) return false;
            int v = sh.fill.getColor() & 0x00FFFFFF;
            if (c == null) c = v;
            else if (c != v) return false;
        }
        return c != null;
    }

    /** Paints every fill with a vertical gradient of the theme's light, from `top` to `bottom`, alpha kept. */
    void tint(int top, int bottom) {
        for (Shape sh : shapes) {
            if (sh.fill == null) continue;
            int a = sh.fill.getAlpha();
            sh.fill.setShader(new LinearGradient(0, viewBox.top, 0, viewBox.bottom, top, bottom, Shader.TileMode.CLAMP));
            sh.fill.setColor(0xFFFFFFFF);
            sh.fill.setAlpha(a);
        }
    }

    /**
     * A logo in the app's look. A one-colour silhouette (the official logos are) is painted in the warm-white to
     * amber light of the theme; a multi-colour design keeps its shading with every colour moved onto the theme.
     */
    static Svg themed(byte[] svg) throws Exception {
        Svg plain = parse(new java.io.ByteArrayInputStream(svg), null);
        if (plain.monochrome()) {
            plain.tint(Theme.LED, Theme.AMBER);
            return plain;
        }
        return parse(new java.io.ByteArrayInputStream(svg), THEME);
    }

    /** The picture as a Drawable, so it fits wherever an image goes. */
    Drawable drawable() {
        final Svg svg = this;
        return new Drawable() {
            int alpha = 255;
            @Override public void draw(Canvas canvas) { svg.draw(canvas, getBounds(), alpha); }
            @Override public void setAlpha(int a) { alpha = a; }
            @Override public void setColorFilter(ColorFilter cf) { }
            @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        };
    }

    // ------------------------------------------------------------------ attributes

    private static String local(String name) {
        int i = name.indexOf(':');
        return i >= 0 ? name.substring(i + 1) : name;
    }

    private static String first(String... v) {
        for (String s : v) if (s != null) return s;
        return null;
    }

    private static Map<String, String> styleMap(String style) {
        Map<String, String> m = new HashMap<>();
        if (style == null) return m;
        for (String part : style.split(";")) {
            int i = part.indexOf(':');
            if (i > 0) m.put(part.substring(0, i).trim(), part.substring(i + 1).trim());
        }
        return m;
    }

    /** Reads presentation attributes and the style attribute into the inherited style. */
    private static void apply(XmlPullParser p, Style st) {
        Map<String, String> sm = styleMap(p.getAttributeValue(null, "style"));
        String v;
        if ((v = first(sm.get("fill"), p.getAttributeValue(null, "fill"))) != null) st.fill = v;
        if ((v = first(sm.get("stroke"), p.getAttributeValue(null, "stroke"))) != null) st.stroke = v;
        if ((v = first(sm.get("stroke-width"), p.getAttributeValue(null, "stroke-width"))) != null) st.strokeWidth = len(v, 1);
        if ((v = first(sm.get("opacity"), p.getAttributeValue(null, "opacity"))) != null) st.opacity *= num(v);
        if ((v = first(sm.get("fill-opacity"), p.getAttributeValue(null, "fill-opacity"))) != null) st.fillOpacity = num(v);
        if ((v = first(sm.get("stroke-opacity"), p.getAttributeValue(null, "stroke-opacity"))) != null) st.strokeOpacity = num(v);
        if ((v = first(sm.get("fill-rule"), p.getAttributeValue(null, "fill-rule"))) != null) st.evenOdd = v.equals("evenodd");
        String t = p.getAttributeValue(null, "transform");
        if (t != null) st.m.preConcat(transform(t));
        if ("currentColor".equals(st.fill)) st.fill = "#000000";
    }

    private static float coord(XmlPullParser p, String name, float def) {
        String v = p.getAttributeValue(null, name);
        if (v == null) return def;
        v = v.trim();
        return v.endsWith("%") ? num(v.substring(0, v.length() - 1)) / 100f : len(v, def);
    }

    private static float len(String v, float def) {
        if (v == null) return def;
        v = v.trim().replaceAll("(px|pt|mm|cm|in)$", "");
        try {
            return Float.parseFloat(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static float num(String v) {
        try {
            return Float.parseFloat(v.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    static float[] numbers(String s) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[-+]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?").matcher(s);
        List<Float> out = new ArrayList<>();
        while (m.find()) out.add(Float.parseFloat(m.group()));
        float[] a = new float[out.size()];
        for (int i = 0; i < a.length; i++) a[i] = out.get(i);
        return a;
    }

    private static final Map<String, Integer> NAMED = new HashMap<>();
    static {
        NAMED.put("black", 0xFF000000);
        NAMED.put("white", 0xFFFFFFFF);
        NAMED.put("red", 0xFFFF0000);
        NAMED.put("green", 0xFF008000);
        NAMED.put("blue", 0xFF0000FF);
        NAMED.put("yellow", 0xFFFFFF00);
        NAMED.put("orange", 0xFFFFA500);
        NAMED.put("gold", 0xFFFFD700);
        NAMED.put("gray", 0xFF808080);
        NAMED.put("grey", 0xFF808080);
        NAMED.put("transparent", 0x00000000);
    }

    static int color(String s) {
        s = s.trim().toLowerCase(Locale.ROOT);
        try {
            if (s.startsWith("#")) {
                String h = s.substring(1);
                if (h.length() == 3) h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
                if (h.length() == 6) return 0xFF000000 | Integer.parseInt(h, 16);
                if (h.length() == 8) { // #rrggbbaa
                    long v = Long.parseLong(h, 16);
                    return (int) (((v & 0xFF) << 24) | (v >> 8));
                }
            } else if (s.startsWith("rgb")) {
                float[] v = numbers(s);
                int a = v.length > 3 ? Math.round(Math.max(0, Math.min(1, v[3])) * 255) : 255;
                boolean pct = s.contains("%");
                int r = Math.round(pct ? v[0] * 2.55f : v[0]), g = Math.round(pct ? v[1] * 2.55f : v[1]), b = Math.round(pct ? v[2] * 2.55f : v[2]);
                return (a << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
            } else if (NAMED.containsKey(s)) {
                return NAMED.get(s);
            }
        } catch (RuntimeException ignored) { }
        return 0xFF000000;
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private static int withAlpha(int c, float a) {
        return (Math.round(Math.max(0, Math.min(1, a)) * 255) << 24) | (c & 0x00FFFFFF);
    }

    static Matrix transform(String s) {
        Matrix m = new Matrix();
        java.util.regex.Matcher mt = java.util.regex.Pattern.compile("(matrix|translate|scale|rotate|skewX|skewY)\\s*\\(([^)]*)\\)").matcher(s);
        while (mt.find()) {
            float[] v = numbers(mt.group(2));
            Matrix t = new Matrix();
            switch (mt.group(1)) {
                case "matrix":
                    if (v.length == 6) t.setValues(new float[]{v[0], v[2], v[4], v[1], v[3], v[5], 0, 0, 1});
                    break;
                case "translate":
                    t.setTranslate(v.length > 0 ? v[0] : 0, v.length > 1 ? v[1] : 0);
                    break;
                case "scale":
                    t.setScale(v.length > 0 ? v[0] : 1, v.length > 1 ? v[1] : (v.length > 0 ? v[0] : 1));
                    break;
                case "rotate":
                    if (v.length >= 3) t.setRotate(v[0], v[1], v[2]);
                    else if (v.length > 0) t.setRotate(v[0]);
                    break;
                case "skewX":
                    t.setSkew((float) Math.tan(Math.toRadians(v.length > 0 ? v[0] : 0)), 0);
                    break;
                default:
                    t.setSkew(0, (float) Math.tan(Math.toRadians(v.length > 0 ? v[0] : 0)));
            }
            m.preConcat(t);
        }
        return m;
    }

    // ------------------------------------------------------------------ geometry

    private static Path geometry(String tag, XmlPullParser p) {
        Path path = new Path();
        switch (tag) {
            case "path": {
                String d = p.getAttributeValue(null, "d");
                if (d == null) return null;
                PathData.parse(d, path);
                return path;
            }
            case "rect": {
                float x = coord(p, "x", 0), y = coord(p, "y", 0), w = coord(p, "width", 0), h = coord(p, "height", 0);
                float rx = coord(p, "rx", -1), ry = coord(p, "ry", -1);
                if (rx < 0) rx = ry < 0 ? 0 : ry;
                if (ry < 0) ry = rx;
                if (rx > 0 || ry > 0) path.addRoundRect(new RectF(x, y, x + w, y + h), Math.min(rx, w / 2), Math.min(ry, h / 2), Path.Direction.CW);
                else path.addRect(x, y, x + w, y + h, Path.Direction.CW);
                return path;
            }
            case "circle":
                path.addCircle(coord(p, "cx", 0), coord(p, "cy", 0), coord(p, "r", 0), Path.Direction.CW);
                return path;
            case "ellipse": {
                float cx = coord(p, "cx", 0), cy = coord(p, "cy", 0), rx = coord(p, "rx", 0), ry = coord(p, "ry", 0);
                path.addOval(new RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CW);
                return path;
            }
            case "line":
                path.moveTo(coord(p, "x1", 0), coord(p, "y1", 0));
                path.lineTo(coord(p, "x2", 0), coord(p, "y2", 0));
                return path;
            case "polyline":
            case "polygon": {
                float[] v = numbers(String.valueOf(p.getAttributeValue(null, "points")));
                for (int i = 0; i + 1 < v.length; i += 2) {
                    if (i == 0) path.moveTo(v[0], v[1]);
                    else path.lineTo(v[i], v[i + 1]);
                }
                if (tag.equals("polygon")) path.close();
                return path;
            }
            default:
                return null;
        }
    }

    /** SVG path data, every command, arcs converted to cubic curves. */
    static final class PathData {
        private final String s;
        private int i;

        private PathData(String s) { this.s = s; }

        static void parse(String d, Path out) {
            new PathData(d).run(out);
        }

        private void skip() {
            while (i < s.length() && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ',')) i++;
        }

        private boolean numberNext() {
            skip();
            if (i >= s.length()) return false;
            char c = s.charAt(i);
            return (c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.';
        }

        private float n() {
            skip();
            int start = i;
            if (i < s.length() && (s.charAt(i) == '-' || s.charAt(i) == '+')) i++;
            boolean dot = false, exp = false;
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c >= '0' && c <= '9') i++;
                else if (c == '.' && !dot && !exp) { dot = true; i++; }
                else if ((c == 'e' || c == 'E') && !exp) {
                    exp = true;
                    i++;
                    if (i < s.length() && (s.charAt(i) == '-' || s.charAt(i) == '+')) i++;
                } else break;
            }
            return start == i ? 0 : Float.parseFloat(s.substring(start, i));
        }

        /** Arc flags may be written without separators ("a1 1 0 011 1"). */
        private boolean flag() {
            skip();
            char c = i < s.length() ? s.charAt(i) : '0';
            i++;
            return c == '1';
        }

        private void run(Path p) {
            float x = 0, y = 0, sx = 0, sy = 0, cx = 0, cy = 0;
            char cmd = 'M', prev = ' ';
            while (true) {
                skip();
                if (i >= s.length()) break;
                int at = i;
                char c = s.charAt(i);
                if (Character.isLetter(c) && c != 'e' && c != 'E') {
                    cmd = c;
                    i++;
                } else if (prev == ' ') {
                    break; // data must start with a command
                }
                boolean rel = Character.isLowerCase(cmd);
                switch (Character.toUpperCase(cmd)) {
                    case 'M': {
                        float nx = n(), ny = n();
                        if (rel) { nx += x; ny += y; }
                        p.moveTo(nx, ny);
                        x = sx = cx = nx;
                        y = sy = cy = ny;
                        cmd = rel ? 'l' : 'L'; // further pairs are line-tos
                        break;
                    }
                    case 'L': {
                        float nx = n(), ny = n();
                        if (rel) { nx += x; ny += y; }
                        p.lineTo(nx, ny);
                        x = cx = nx;
                        y = cy = ny;
                        break;
                    }
                    case 'H': {
                        float nx = n();
                        if (rel) nx += x;
                        p.lineTo(nx, y);
                        x = cx = nx;
                        cy = y;
                        break;
                    }
                    case 'V': {
                        float ny = n();
                        if (rel) ny += y;
                        p.lineTo(x, ny);
                        y = cy = ny;
                        cx = x;
                        break;
                    }
                    case 'C': {
                        float x1 = n(), y1 = n(), x2 = n(), y2 = n(), nx = n(), ny = n();
                        if (rel) { x1 += x; y1 += y; x2 += x; y2 += y; nx += x; ny += y; }
                        p.cubicTo(x1, y1, x2, y2, nx, ny);
                        cx = x2;
                        cy = y2;
                        x = nx;
                        y = ny;
                        break;
                    }
                    case 'S': {
                        float x2 = n(), y2 = n(), nx = n(), ny = n();
                        if (rel) { x2 += x; y2 += y; nx += x; ny += y; }
                        boolean smooth = "CcSs".indexOf(prev) >= 0;
                        float x1 = smooth ? 2 * x - cx : x, y1 = smooth ? 2 * y - cy : y;
                        p.cubicTo(x1, y1, x2, y2, nx, ny);
                        cx = x2;
                        cy = y2;
                        x = nx;
                        y = ny;
                        break;
                    }
                    case 'Q': {
                        float x1 = n(), y1 = n(), nx = n(), ny = n();
                        if (rel) { x1 += x; y1 += y; nx += x; ny += y; }
                        p.quadTo(x1, y1, nx, ny);
                        cx = x1;
                        cy = y1;
                        x = nx;
                        y = ny;
                        break;
                    }
                    case 'T': {
                        float nx = n(), ny = n();
                        if (rel) { nx += x; ny += y; }
                        boolean smooth = "QqTt".indexOf(prev) >= 0;
                        float x1 = smooth ? 2 * x - cx : x, y1 = smooth ? 2 * y - cy : y;
                        p.quadTo(x1, y1, nx, ny);
                        cx = x1;
                        cy = y1;
                        x = nx;
                        y = ny;
                        break;
                    }
                    case 'A': {
                        float rx = n(), ry = n(), rot = n();
                        boolean large = flag(), sweep = flag();
                        float nx = n(), ny = n();
                        if (rel) { nx += x; ny += y; }
                        arc(p, x, y, nx, ny, rx, ry, rot, large, sweep);
                        x = cx = nx;
                        y = cy = ny;
                        break;
                    }
                    case 'Z':
                        p.close();
                        x = cx = sx;
                        y = cy = sy;
                        break;
                    default:
                        return; // unknown command: stop rather than draw garbage
                }
                if (i == at) break; // nothing consumed: malformed data, stop here
                prev = cmd;
            }
        }

        /** Endpoint arc to cubic Béziers (SVG implementation notes, F.6). */
        static void arc(Path p, float x0, float y0, float x, float y, float rx, float ry, float angle, boolean large, boolean sweep) {
            if (rx == 0 || ry == 0) {
                p.lineTo(x, y);
                return;
            }
            rx = Math.abs(rx);
            ry = Math.abs(ry);
            double phi = Math.toRadians(angle % 360), cos = Math.cos(phi), sin = Math.sin(phi);
            double dx2 = (x0 - x) / 2.0, dy2 = (y0 - y) / 2.0;
            double x1p = cos * dx2 + sin * dy2, y1p = -sin * dx2 + cos * dy2;
            double lambda = (x1p * x1p) / (rx * rx) + (y1p * y1p) / (ry * ry);
            if (lambda > 1) {
                rx *= Math.sqrt(lambda);
                ry *= Math.sqrt(lambda);
            }
            double num = rx * rx * ry * ry - rx * rx * y1p * y1p - ry * ry * x1p * x1p;
            double den = rx * rx * y1p * y1p + ry * ry * x1p * x1p;
            double coef = (large == sweep ? -1 : 1) * Math.sqrt(Math.max(0, num / den));
            double cxp = coef * rx * y1p / ry, cyp = -coef * ry * x1p / rx;
            double cx = cos * cxp - sin * cyp + (x0 + x) / 2.0, cy = sin * cxp + cos * cyp + (y0 + y) / 2.0;
            double th1 = Math.atan2((y1p - cyp) / ry, (x1p - cxp) / rx);
            double dth = Math.atan2((-y1p - cyp) / ry, (-x1p - cxp) / rx) - th1;
            if (sweep && dth < 0) dth += 2 * Math.PI;
            else if (!sweep && dth > 0) dth -= 2 * Math.PI;
            int segs = (int) Math.ceil(Math.abs(dth) / (Math.PI / 2));
            double delta = dth / segs, t = 4.0 / 3.0 * Math.tan(delta / 4);
            double th = th1;
            for (int k = 0; k < segs; k++) {
                double c1 = Math.cos(th), s1 = Math.sin(th), c2 = Math.cos(th + delta), s2 = Math.sin(th + delta);
                double e1x = c1 - t * s1, e1y = s1 + t * c1, e2x = c2 + t * s2, e2y = s2 - t * c2;
                p.cubicTo(
                        (float) (cx + rx * cos * e1x - ry * sin * e1y), (float) (cy + rx * sin * e1x + ry * cos * e1y),
                        (float) (cx + rx * cos * e2x - ry * sin * e2y), (float) (cy + rx * sin * e2x + ry * cos * e2y),
                        (float) (cx + rx * cos * c2 - ry * sin * s2), (float) (cy + rx * sin * c2 + ry * cos * s2));
                th += delta;
            }
        }
    }
}
