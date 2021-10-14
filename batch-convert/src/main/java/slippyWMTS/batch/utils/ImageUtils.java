package slippyWMTS.batch.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {
    private static final int THRESHOLD = 240;

    public static boolean isBlank(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                Color c = new Color(rgb);
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                if (red < THRESHOLD || green < THRESHOLD || blue < THRESHOLD) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isFullyTransparent(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                if (alpha != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
