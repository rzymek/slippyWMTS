package slippyWMTS.batch;

import rx.Observable;
import slippyWMTS.batch.utils.RxUtils;
import slippyWMTS.capabilities.xml.Capabilities;
import slippyWMTS.capabilities.xml.Capabilities.Layer;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixLimits;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSet;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSetLink;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Fetch {
    private static final int AVG_TILE_SIZE = 33000;
    private final TileMatrixLimits limits;
    private URL url;
    private String set;
    private int z;

    private Layer layer;
    private TileMatrixSet tileMatrixSet;
    private final Capabilities capabilities;

    public Fetch(String url, int z, String set, Capabilities capabilities) throws IOException {
        this.capabilities = capabilities;
        this.url = new URL(url);
        this.z = z;
        this.set = set;
        limits = getTileMatrix();
    }


    public static Capabilities getCapabilities(URL url) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("SERVICE", "WMTS");
        params.put("REQUEST", "GetCapabilities");
        URL getCapabilities = UrlBuilder.createURL(url, params);
        try (InputStream in = open(getCapabilities)) {
            return Capabilities.parse(in);
        }
    }

    private TileMatrixLimits getTileMatrix() throws IOException {
        Capabilities capabilities = this.capabilities;
        layer = capabilities.Contents.Layer;
        tileMatrixSet = capabilities.Contents.getTileMatrixSetByCRS(Pattern.compile(set));
        final TileMatrixSetLink tileMatrixSetLink = layer.getTileMatrixSet(tileMatrixSet.Identifier);
        return tileMatrixSetLink.TileMatrixSetLimits.TileMatrixLimits[z];
    }

    protected static InputStream open(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.91 Safari/537.36");
        return conn.getInputStream();
    }

    public BufferedImage fetch(int row, int col) throws IOException {
        if (!(limits.MinTileRow <= row && row <= limits.MaxTileRow)) {
            String msg = String.format("%s < %s < %s", limits.MinTileRow, row, limits.MaxTileRow);
            throw new IndexOutOfBoundsException(msg);
        }
        if (!(limits.MinTileCol <= col && col <= limits.MaxTileCol)) {
            String msg = String.format("%s < %s < %s", limits.MinTileCol, col, limits.MaxTileCol);
            throw new IndexOutOfBoundsException(msg);
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("SERVICE", "WMTS");
        params.put("REQUEST", "GetTile");
        params.put("VERSION", "1.0.0");
        params.put("LAYER", layer.Identifier);
        params.put("STYLE", "default");
        params.put("FORMAT", "image/jpeg");
        params.put("TILEMATRIXSET", tileMatrixSet.Identifier);
        params.put("TILEMATRIX", tileMatrixSet.TileMatrix[z].Identifier);
        params.put("TILEROW", Integer.toString(row));
        params.put("TILECOL", Integer.toString(col));
        URL getTile = UrlBuilder.createURL(url, params);
        return Observable.just(getTile)
                .map(this::download)
                .retry((iteration, ex) -> {
                    if (iteration >= 10) {
                        return false;
                    } else {
                        System.out.println(ex + ": Retrying in " + iteration + " sec.");
                        RxUtils.sleep(iteration, TimeUnit.SECONDS);
                        return true;
                    }
                })
                .onErrorReturn(e -> null)
                .toBlocking()
                .first();
    }

    private BufferedImage download(URL url) {
        try (final InputStream in = open(url)) {
            System.out.println(url);
            return readImage(in);
        } catch (IOException ex) {
            throw new RuntimeException(url.toString(), ex);
        }
    }

    private BufferedImage readImage(InputStream is) throws IOException {
        final ImageInputStream imageInputStream = ImageIO.createImageInputStream(is);
        final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
        if (!imageReaders.hasNext()) {
            throw new IOException("no image reader");
        }
        final ImageReader imageReader = imageReaders.next();
        imageReader.setInput(imageInputStream);
        final BufferedImage image = imageReader.read(0);
        if (image == null) {
            throw new IOException("invalid image file");
        }
        if (image.getWidth() != tileMatrixSet.TileMatrix[z].TileWidth || image.getHeight() != tileMatrixSet.TileMatrix[z].TileHeight) {
            throw new IOException("invalid image dimentions: " + image.getWidth() + "x" + image.getHeight());
        }
        image.flush();
        if (imageReader.getFormatName().equals("JPEG")) {
            imageInputStream.seek(imageInputStream.getStreamPosition() - 2);
            final byte[] lastTwoBytes = new byte[2];
            imageInputStream.read(lastTwoBytes);
            if (lastTwoBytes[0] != (byte) 0xff && lastTwoBytes[1] != (byte) 0xd9) {
                throw new IOException("truncated file");
            }
        }
        return image;
    }
}
