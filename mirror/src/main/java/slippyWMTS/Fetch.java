package slippyWMTS;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import slippyWMTS.capabilities.xml.Capabilities;
import slippyWMTS.capabilities.xml.Capabilities.Layer;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixLimits;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSet;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSetLink;

import com.google.common.io.Files;

public class Fetch {
	private URL url;
	private String set;
	private int z;

	private Layer layer;
	private TileMatrixSet tileMatrixSet;
	private String downloadDir;

	public Fetch(String url, int z, String set) throws MalformedURLException {
		this.url = new URL(url);
		this.z = z;
		this.set = set;
		downloadDir = set.replaceAll("\\W+", "");
	}

	private TileMatrixLimits getTileMatrix() throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("SERVICE", "WMTS");
		params.put("REQUEST", "GetCapabilities");
		URL getCapabilities = UrlBuilder.createURL(url, params);
		try (InputStream in = open(getCapabilities)) {
			Capabilities capabilities = Capabilities.parse(in);
			layer = capabilities.Contents.Layer;
			tileMatrixSet = capabilities.Contents.getTileMatrixSetByCRS(Pattern.compile(set));
			final TileMatrixSetLink tileMatrixSetLink = layer.getTileMatrixSet(tileMatrixSet.Identifier);
			return tileMatrixSetLink.TileMatrixSetLimits.TileMatrixLimits[z];
		}
	}

	protected InputStream open(URL getCapabilities) throws IOException {
		URLConnection conn = getCapabilities.openConnection();
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.91 Safari/537.36");
		return conn.getInputStream();
	}

	public void fetch() throws IOException {
		TileMatrixLimits limits = getTileMatrix();
		for (int row = limits.MinTileRow; row <= limits.MaxTileRow; row++) {
			for (int col = limits.MinTileCol; col <= limits.MaxTileCol; col++) {
				fetch(row, col);
			}
		}
	}

	private void fetch(int row, int col) throws IOException {
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
		download(getTile, String.format("%d/%d/%d.jpg", z, col, row));
	}

	private void download(URL getTile, String filename) throws IOException {
		File file = new File(downloadDir, filename);
		file.getParentFile().mkdirs();
		if (file.exists()) {
			return;
		}
		try (final InputStream in = open(getTile)) {
			ReadableByteChannel rbc = Channels.newChannel(in);
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}
			check(file);
		} catch (IOException ex) {
			String msg = "[" + new Date() + "] " + filename + ": " + ex.toString() + "\n";
			Files.append(msg, new File("fetch.log"), StandardCharsets.UTF_8);
		}
	}

	private void check(File file) throws IOException {
		try {
			BufferedImage image = ImageIO.read(file);
			if (image == null) {
				throw new IOException("invalid image file");
			}
			if (image.getWidth() != tileMatrixSet.TileMatrix[z].TileWidth || image.getHeight() != tileMatrixSet.TileMatrix[z].TileHeight) {
				throw new IOException("invalid image dimentions: " + image.getWidth() + "x" + image.getHeight());
			}
			System.out.println(file.getAbsolutePath());
		} catch (IOException e) {
			file.delete();
			throw e;
		}
	}

	public static void main(String[] args) throws Exception {
		String endpoint = "http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/TOPO";
		String[] sets = { ".*EPSG:.*:2180", ".*EPSG:.*:4326" };
		for (String set : sets) {
			for (int i = 0; i <= 10; i++) {
				new Fetch(endpoint, i, set).fetch();
			}
		}
	}
}
