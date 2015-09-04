package slippyWMTS.batch;

import slippyWMTS.Epsg;
import slippyWMTS.TileTranformation;
import slippyWMTS.Transform;
import slippyWMTS.area.TileBox;
import slippyWMTS.capabilities.xml.Capabilities;
import slippyWMTS.position.DoubleXY;
import slippyWMTS.position.LonLat;
import slippyWMTS.tile.SlippyTile;
import slippyWMTS.tile.WmtsTile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Convert implements Runnable {
    private static final String EXT = "jpg";
    private int percent;
    private int layer;
    private String dir;

    public Convert(String dir) {
        this.dir = dir;
    }

    private Capabilities getCapabilities() throws IOException {
        try (InputStream in = Convert.class.getResourceAsStream("/cap.xml")) {
            return Capabilities.parse(in);
        }
    }


    public void run() {
        try {
            Capabilities capabilities = getCapabilities();
            Capabilities.TileMatrixSet tileMatrixSet = capabilities.Contents.getTileMatrixSetByCRS(Pattern.compile(".*:" + Epsg.WGS84.code + "$"));
            Transform transform = new Transform(tileMatrixSet);

            for (int z = 0; z <= 9; z++) {
                layer = z;

                LonLat topLeft = new LonLat(14.1400, 55.9500);
                LonLat bottomRight = new LonLat(24.1600, 49.0300);

                SlippyTile topLeftSlippy = new SlippyTile(topLeft, z + 6);
                SlippyTile bottomRightSlippy = new SlippyTile(bottomRight, z + 6);
                for (int x = topLeftSlippy.x; x <= bottomRightSlippy.x; x++) {
                    percent = (x - topLeftSlippy.x) * 100 / (bottomRightSlippy.x - topLeftSlippy.x);
                    progress("");
                    for (int y = topLeftSlippy.y; y <= bottomRightSlippy.y; y++) {
                        try {
                            SlippyTile slippyTile = new SlippyTile(z + 6, x, y);
                            Image slippy = generateTile(tileMatrixSet, transform, slippyTile);
                            File dir = new File("osmgeo/" + slippyTile.z + "/" + slippyTile.x + "/");
                            dir.mkdirs();
                            File output = new File(dir, slippyTile.y + "." + EXT);
                            if (output.exists() && output.length() > 0) {
                                continue;
                            }
                            ImageIO.write((RenderedImage) slippy, EXT, output);
                        } catch (Exception ex) {
                            progress("ERR:" + ex.getMessage());
                        }

                    }
                }
            }
        } catch (Exception ex) {
            progress("ERR:" + ex.getMessage());
        }
    }

    private SimpleDateFormat format = new SimpleDateFormat("HH:mm.ss");

    protected void progress(String s) {
        System.out.println(String.format("[%s][%d][% 3d%%] %s",
                format.format(new Date()),
                layer,
                percent,
                s
        ));
    }

    private Image compose(List<Composition> compositions, TileBox<DoubleXY> cropBox) {
        BufferedImage buf = new BufferedImage((int) cropBox.bottomRight.x, (int) cropBox.bottomRight.y, TYPE_INT_RGB);
        Graphics2D g = buf.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        compositions.stream().forEach(it -> {
            g.drawImage(it.image, it.pixelX, it.pixelY, null);
        });
        BufferedImage cropped = buf.getSubimage((int) cropBox.topLeft.x, (int) cropBox.topLeft.y,
                (int) (cropBox.bottomRight.x - cropBox.topLeft.x),
                (int) (cropBox.bottomRight.y - cropBox.topLeft.y));
        BufferedImage slippy = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = slippy.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(cropped, 0, 0, 256, 256, null);
        return slippy;
    }

    private static class Composition {
        private final BufferedImage image;
        private final int pixelX;
        private final int pixelY;

        public Composition(BufferedImage image, int pixelX, int pixelY) {
            this.image = image;
            this.pixelX = pixelX;
            this.pixelY = pixelY;
        }
    }

    private Image generateTile(Capabilities.TileMatrixSet tileMatrixSet, Transform transform, SlippyTile slippyTile) throws IOException {
        TileTranformation tileTranformation = transform.transformAndCrop(slippyTile);
        TileBox<WmtsTile> tileBox = tileTranformation.wmtsBox;
        final int startX = (int) tileBox.topLeft.x;
        final int startY = (int) tileBox.topLeft.y;
        final int z = tileBox.topLeft.z;
        final int tileWidth = tileMatrixSet.TileMatrix[z].TileWidth;
        final int tileHeight = tileMatrixSet.TileMatrix[z].TileHeight;
        List<Composition> compositions = new ArrayList<>();
        for (int x = startX; x < tileBox.bottomRight.x; x++) {
            for (int y = startY; y < tileBox.bottomRight.y; y++) {
                WmtsTile wmtsTile = new WmtsTile(x, y, z);
                final int col = x - startX;
                final int row = y - startY;
                int pixelX = col * tileWidth;
                int pixelY = row * tileHeight;
                BufferedImage image = getImage(wmtsTile);
                compositions.add(new Composition(image, pixelX, pixelY));
            }
        }
        return compose(compositions, tileTranformation.cropBox);
    }

    private BufferedImage getImage(WmtsTile wmtsTile) throws IOException {
        File wmtsFile = new File(dir+"/" + wmtsTile.z + "/" + wmtsTile.getX() + "/" + wmtsTile.getY() + ".jpg");
        try {
            return ImageIO.read(wmtsFile);
        } catch (IOException ex) {
            throw new RuntimeException(wmtsFile + " " + ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) throws Exception {
        new Convert(args[0]).run();
    }
}
