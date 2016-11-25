package slippyWMTS.batch;

import slippyWMTS.Epsg;
import slippyWMTS.TileTranformation;
import slippyWMTS.Transform;
import slippyWMTS.area.TileBox;
import slippyWMTS.batch.store.MBTilesStore;
import slippyWMTS.batch.store.Store;
import slippyWMTS.batch.utils.ImageUtils;
import slippyWMTS.capabilities.xml.Capabilities;
import slippyWMTS.position.DoubleXY;
import slippyWMTS.position.LonLat;
import slippyWMTS.tile.SlippyTile;
import slippyWMTS.tile.WmtsTile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Convert implements Runnable {
    private final String endpoint;
    private final Capabilities capabilities;
    private int percent;
    private int layer;

    public Convert() {
        String type = "TOPO";
//        String type = "ORTO";
        endpoint = "http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/" + type;
        //http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/ISOK_CIEN
        //http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/BDO
//        endpoint = "http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/BDO";
        try {
            capabilities = Fetch.getCapabilities(new URL(endpoint));
        } catch (IOException e) {
            throw new RuntimeException(endpoint, e);
        }
    }

    private Capabilities getCapabilities() throws IOException {
        try (InputStream in = Convert.class.getResourceAsStream("/cap.xml")) {
            return Capabilities.parse(in);
        }
    }


    public void run() {
        try (Store store =
//                     new FileStore("batch-convert/target/png1/")) {
                     new MBTilesStore("batch-convert/target/osmgeo.mbtiles")) {
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
                    progress(getProgressMsg());
                    for (int y = topLeftSlippy.y; y <= bottomRightSlippy.y; y++) {
                        SlippyTile slippyTile = new SlippyTile(z + 6, x, y);
                        if (store.exists(slippyTile)) {
                            continue;
                        }
                        BufferedImage slippy = generateTile(tileMatrixSet, transform, slippyTile);
                        if (slippy == null) {
                            store.saveError(slippyTile);
                        } else if (ImageUtils.isBlank(slippy)) {
                            store.saveEmpty(slippyTile);
                        } else {
                            store.save(slippyTile, slippy);
                        }
                    }
                }
            }
            progress("Cleaning up");
        } catch (Exception ex) {
            handleError(ex);
        }
        progress("Done.");
    }

    private void handleError(Exception ex) {
        ex.printStackTrace();
        progress("ERR:" + ex.getMessage());
    }

    private SimpleDateFormat format = new SimpleDateFormat("HH:mm.ss");

    protected void progress(String s) {
    }

    private String getProgressMsg() {
        return String.format("[%s][%d][% 3d%%]",
                format.format(new Date()),
                layer,
                percent
        );
    }

    private BufferedImage compose(List<Composition> compositions, TileBox<DoubleXY> cropBox) {
        BufferedImage buf = new BufferedImage((int) cropBox.bottomRight.x, (int) cropBox.bottomRight.y, TYPE_INT_RGB);
        Graphics2D g = buf.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, buf.getWidth(), buf.getHeight());
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        compositions.stream().forEach(it -> {
            g.drawImage(it.image, it.pixelX, it.pixelY, null);
        });
        double x = Math.max(0, cropBox.topLeft.x);
        double y = Math.max(0, cropBox.topLeft.y);
        double w = cropBox.bottomRight.x - x;
        double h = cropBox.bottomRight.y - y;
        BufferedImage cropped = buf.getSubimage(
                (int) x,
                (int) y,
                (int) w,
                (int) h);
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

    private BufferedImage generateTile(Capabilities.TileMatrixSet tileMatrixSet, Transform transform, SlippyTile slippyTile) throws IOException {
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
                try {
                    BufferedImage image = getImage(wmtsTile);
                    if (image == null) {
                        return null;
                    }
                    compositions.add(new Composition(image, pixelX, pixelY));
                } catch (IndexOutOfBoundsException ex) {
                    continue;
                }
            }
        }
        return compose(compositions, tileTranformation.cropBox);
    }


    private static final WeakHashMap<String, BufferedImage> wmtsImages = new WeakHashMap<>();
    private static final Map<Integer, Fetch> layers = new HashMap<>();

    private BufferedImage getImage(WmtsTile wmtsTile) throws IOException {
        BufferedImage cached = wmtsImages.get(wmtsTile.toString());
        if (cached == null) {
            String set = ".*EPSG:.*:4326";

            Fetch fetch = layers.get(wmtsTile.z);
            if (fetch == null) {
                fetch = new Fetch(endpoint, wmtsTile.z, set, capabilities);
                layers.put(wmtsTile.z, fetch);
            }
            cached = fetch.fetch(wmtsTile.getY(), wmtsTile.getX());
            if (cached != null) {
                wmtsImages.put(wmtsTile.toString(), cached);
            }
        }
        return cached;
    }
}
