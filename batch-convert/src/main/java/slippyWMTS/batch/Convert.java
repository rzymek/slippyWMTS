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
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Convert {
    private Capabilities getCapabilities(URL url) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("SERVICE", "WMTS");
        params.put("REQUEST", "GetCapabilities");
        URL getCapabilities = UrlBuilder.createURL(url, params);
        URLConnection conn = getCapabilities.openConnection();
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");
        System.out.println(getCapabilities);
        try (InputStream in = conn.getInputStream()) {
            return Capabilities.parse(in);
        }
    }

    public void run() throws IOException {

        String service = "TOPO";
//        URL url = new URL("http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/" + service);


        Capabilities capabilities = getCapabilities(new File("batch-convert/cap.xml").toURL());
        Capabilities.TileMatrixSet tileMatrixSet = capabilities.Contents.getTileMatrixSetByCRS(Pattern.compile(".*:" + Epsg.WGS84.code + "$"));
        Transform transform = new Transform(tileMatrixSet);

        for (int z = 0; z < tileMatrixSet.TileMatrix.length; z++) {
            Capabilities.TileMatrix tileMatrix = tileMatrixSet.TileMatrix[z];

//            LonLat topLeft = new LonLat(21.016667, 52.233333);
//            LonLat bottomRight = new LonLat(22, 51);
            LonLat topLeft = new LonLat( 14.1400, 55.9500);
            LonLat bottomRight = new LonLat(24.1600, 49.0300);

            SlippyTile topLeftSlippy = new SlippyTile(topLeft, z + 6);
            SlippyTile bottomRightSlippy = new SlippyTile(bottomRight, z + 6);
            for (int x = topLeftSlippy.x; x <= bottomRightSlippy.x; x++) {
                for (int y = topLeftSlippy.y; y <= bottomRightSlippy.y; y++) {
                    try {
                        SlippyTile slippyTile = new SlippyTile(z + 6, x, y);
                        Image slippy = generateTile(tileMatrixSet, transform, slippyTile);
                        File dir = new File("slippy/" + slippyTile.z + "/" + slippyTile.x + "/");
                        dir.mkdirs();
                        ImageIO.write((RenderedImage) slippy, "png", new File(dir, slippyTile.y + ".png"));
                    } catch (Exception ex) {
                        System.err.println(ex.toString());
                    }

                }
            }
        }
    }

    private Image compose(List<Composition> compositions, TileBox<DoubleXY> cropBox) {
        double resWidth = cropBox.bottomRight.x;
        double resHeight = cropBox.bottomRight.y;
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
        int tileCountX = (int) (tileBox.bottomRight.x - startX + 1);
        int tileCountY = (int) (tileBox.bottomRight.y - startY + 1);
        int width = tileCountX * tileWidth;
        int height = tileCountY * tileHeight;
        List<Composition> compositions = new ArrayList<>();
        for (int x = startX; x < tileBox.bottomRight.x; x++) {
            for (int y = startY; y < tileBox.bottomRight.y; y++) {
                WmtsTile wmtsTile = new WmtsTile(x, y, z);
                final int col = x - startX;
                final int row = y - startY;
                int pixelX = (int) (col * tileWidth);
                int pixelY = (int) (row * tileHeight);
                BufferedImage image = getImage(wmtsTile);
                System.out.println(wmtsTile + " " + pixelX + "," + pixelY);
                compositions.add(new Composition(image, pixelX, pixelY));
            }
        }
        return compose(compositions, tileTranformation.cropBox);
    }

    private BufferedImage getImage(WmtsTile wmtsTile) throws IOException {
        File wmtsFile = new File("mirror/EPSG4326/" + wmtsTile.z + "/" + wmtsTile.getX() + "/" + wmtsTile.getY() + ".jpg");
        try {
            return ImageIO.read(wmtsFile);
        } catch (IOException ex) {
            throw new RuntimeException(wmtsFile.toString(), ex);
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("http.agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36");
        new Convert().run();
    }
}
