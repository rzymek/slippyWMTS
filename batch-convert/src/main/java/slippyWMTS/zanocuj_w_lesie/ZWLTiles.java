package slippyWMTS.zanocuj_w_lesie;

import slippyWMTS.batch.store.FileStore;
import slippyWMTS.batch.store.MBTilesStore;
import slippyWMTS.batch.store.Store;
import slippyWMTS.batch.utils.ImageUtils;
import slippyWMTS.batch.utils.MercatorTransform;
import slippyWMTS.position.LonLat;
import slippyWMTS.tile.SlippyTile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import static java.lang.String.format;

public class ZWLTiles {
    FetchImg fetch = new FetchImg();
    double left = 1532421.4519384366;
    double bottom = 6195576.5291813975;
    double right = 2755413.9045009264;
    double top = 7418568.981743888;

    public ZWLTiles() throws MalformedURLException {
    }

    void run() throws Exception {
        LonLat topLeft = new LonLat(14.1400, 55.9500);
        LonLat bottomRight = new LonLat(24.1600, 49.0300);
        try (Store store =
                 new MBTilesStore("zanocuj-w-lesie.mbtiles","Zanocuj w lesie", MBTilesStore.Type.overlay)
        ) {
            int layers = 6;
            for (int z = 0; z <= layers; z++) {
                SlippyTile topLeftSlippy = new SlippyTile(topLeft, z + 6);
                SlippyTile bottomRightSlippy = new SlippyTile(bottomRight, z + 6);

                for (int x = topLeftSlippy.x; x <= bottomRightSlippy.x; x++) {
                    for (int y = topLeftSlippy.y; y <= bottomRightSlippy.y; y++) {
                        int percent = (int) ((x - topLeftSlippy.x) * 100f / (bottomRightSlippy.x - topLeftSlippy.x));
                        System.out.println(format("[%s/%s] - %s%%", z, layers, percent));
                        SlippyTile slippyTile = new SlippyTile(z + 6, x, y);
                        if (store.exists(slippyTile)) {
                            continue;
                        }
                        byte[] slippy = fetchTile(slippyTile);
                        if (slippy == null) {
                            store.saveError(slippyTile);
                        } else if (isBlank(slippy)) {
                            store.saveEmpty(slippyTile);
                        } else {
                            store.save(slippyTile, slippy);
                        }
                    }

                }
            }
        }
    }

    private byte[] fetchTile(SlippyTile tile) throws IOException {
        SlippyTile.BoundingBox bbox = tile.getBBox();
        LonLat topLeft = MercatorTransform.forward(bbox.north, bbox.west);
        LonLat bottomRight = MercatorTransform.forward(bbox.south, bbox.east);
        return this.fetch.fetch(topLeft.x, bottomRight.y, bottomRight.x, topLeft.y);
    }

    private boolean isBlank(byte[] slippy) {
        try {
            return ImageUtils.isFullyTransparent(ImageIO.read(new ByteArrayInputStream(slippy)));
        } catch (IOException e) {
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        new ZWLTiles().run();
    }
}
