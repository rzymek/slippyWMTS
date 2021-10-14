package slippyWMTS.zanocuj_w_lesie;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import slippyWMTS.batch.UrlBuilder;
import slippyWMTS.batch.utils.RxUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class FetchImg {
    double left = 1571557.2104204353;
    double bottom = 6246942.2121890215;
    double right = 2690595.3045151136;
    double top = 7365980.3062837;
    static final int tileSize = 256;
    static final int WGS_84_WEB_MERCATOR = 102100;
    static final int SR = WGS_84_WEB_MERCATOR;
    URL url = new URL("https://mapserver.bdl.lasy.gov.pl/ArcGIS/rest/services/Mapa_turystyczna/MapServer/export");
    final OkHttpClient client = new OkHttpClient();

    public FetchImg() throws MalformedURLException {
    }

    protected byte[] open(URL url) throws IOException {
        try (Response resp = client.newCall(new Request.Builder()
            .url(url).get().header("Referer", "https://www.bdl.lasy.gov.pl/")
            .build()).execute()) {
            return resp.body().bytes();
        }
    }

    public byte[] fetch(double left, double bottom, double right, double top) throws IOException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("dpi", "96");
        params.put("transparent", "true");
        params.put("format", "png8");
        params.put("layers", "show:76,77");
        params.put("bbox", format("%s,%s,%s,%s", left, bottom, right, top));
        params.put("bboxSR", format("%s", SR));
        params.put("imageSR", format("%s", SR));
        params.put("size", format("%s,%s", tileSize, tileSize));
        params.put("f", "image");
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

    private byte[] download(URL url) {
        try {
            log(url);
            byte[] bytes = open(url);
            readImage(new ByteArrayInputStream(bytes));
            return bytes;
        } catch (IOException ex) {
            throw new RuntimeException(url.toString(), ex);
        }
    }

    private void log(Object log) {
//        System.out.println(log);
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
        if (image.getWidth() != tileSize || image.getHeight() != tileSize) {
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
