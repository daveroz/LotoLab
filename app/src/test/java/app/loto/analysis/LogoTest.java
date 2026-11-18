package app.loto.analysis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

/** Every lottery's SVG logo parses, keeps its proportions, takes the theme's colours and actually draws. */
@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 34)
public class LogoTest {
    @Test
    public void everyLogoDrawsInTheTheme() throws Exception {
        for (String key : new String[]{"dorado", "baloto", "miloto"}) {
            byte[] bytes;
            try (InputStream in = RuntimeEnvironment.getApplication().getAssets().open("logos/" + key + ".svg")) {
                bytes = Data.read(in).getBytes(StandardCharsets.UTF_8);
            }
            Svg plain = Svg.parse(new java.io.ByteArrayInputStream(bytes), null);
            assertTrue(key + " has no shapes", plain.shapeCount() > 0);
            assertTrue(key + " aspect " + plain.aspect(), plain.aspect() > 0.3 && plain.aspect() < 6);
            Svg svg = Svg.themed(bytes);
            assertNotNull(svg.drawable());
            int w = 300, h = Math.round(300 / svg.aspect());
            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            svg.draw(new Canvas(b), new Rect(0, 0, w, h), 255);
            int painted = 0, warm = 0;
            for (int y = 0; y < h; y += 3) {
                for (int x = 0; x < w; x += 3) {
                    int c = b.getPixel(x, y);
                    if ((c >>> 24) > 128) {
                        painted++;
                        if (((c >> 16) & 0xFF) > (c & 0xFF)) warm++; // the theme's amber light, not the original near-black
                    }
                }
            }
            assertTrue(key + " drew " + painted + " pixels", painted > 200);
            assertTrue(key + " isn't in the theme's colours", warm > painted / 2);
        }
    }
}
