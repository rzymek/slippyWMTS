package slippyWMTS.batch;

import com.google.common.io.Files;
import slippyWMTS.capabilities.xml.Capabilities;
import slippyWMTS.capabilities.xml.Capabilities.Layer;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixLimits;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSet;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSetLink;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Fetch {
    private static final int AVG_TILE_SIZE = 33000;
    private final TileMatrixLimits limits;
    private URL url;
    private String set;
    private int z;

    private Layer layer;
    private TileMatrixSet tileMatrixSet;
    public StatusListener status = new StatusListener() {
        @Override
        public void progress(double percent) {
        }

        @Override
        public void text(String string) {
            // TODO Auto-generated method stub

        }
    };
    private Capabilities capabilities;

    public Fetch(String url, int z, String set) throws IOException {
        this.url = new URL(url);
        this.z = z;
        this.set = set;
        limits = getTileMatrix();
    }


    public Capabilities getCapabilities() throws IOException {
        if (capabilities != null)
            return capabilities;
        Map<String, String> params = new HashMap<>();
        params.put("SERVICE", "WMTS");
        params.put("REQUEST", "GetCapabilities");
        URL getCapabilities = UrlBuilder.createURL(url, params);
        try (InputStream in = open(getCapabilities)) {
            return capabilities = Capabilities.parse(in);
        }
    }

    private TileMatrixLimits getTileMatrix() throws IOException {
        Capabilities capabilities = getCapabilities();
        layer = capabilities.Contents.Layer;
        tileMatrixSet = capabilities.Contents.getTileMatrixSetByCRS(Pattern.compile(set));
        final TileMatrixSetLink tileMatrixSetLink = layer.getTileMatrixSet(tileMatrixSet.Identifier);
        return tileMatrixSetLink.TileMatrixSetLimits.TileMatrixLimits[z];
    }

    protected InputStream open(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.91 Safari/537.36");
        return conn.getInputStream();
    }

    public static String humanReadableByteCount(long bytes) {
        boolean si = true;
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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
        return download(getTile);
    }

    private BufferedImage download(URL getTile) throws IOException {
        try (final InputStream in = open(getTile)) {
            return readImage(in);
        } catch (IOException ex) {
            throw new IOException(getTile.toString(), ex);
        }
    }

    protected void error(String msg) throws IOException {
        Files.append(msg, new File("fetch.log"), StandardCharsets.UTF_8);
    }

    private void check(File file) throws IOException {
        try (final InputStream is = new FileInputStream(file)) {
            readImage(is);
        } catch (IOException e) {
            file.delete();
            throw e;
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
