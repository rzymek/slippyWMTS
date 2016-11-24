package slippyWMTS.batch.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class ImageUtils {
    public static boolean isBlank(BufferedImage img) {
        int S = 2;
        BufferedImage buf = new BufferedImage(S, S, TYPE_INT_RGB);
        Graphics2D g = buf.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, S, S, null);
        g.dispose();
        for (int x = 0; x < S; x++) {
            for (int y = 0; y < S; y++) {
                int rgb = buf.getRGB(x, y);
                if (rgb != 0xffffffff) {
                    return false;
                }
            }
        }
        return true;
    }
}
