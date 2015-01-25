package slippyWMTS.webproxy.gae;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import slippyWMTS.Epsg;
import slippyWMTS.Transform;
import slippyWMTS.capabilities.xml.Capabilities;
import slippyWMTS.capabilities.xml.Capabilities.Layer;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSet;
import slippyWMTS.tile.WmtsTile;

class Service {
	private final URL url;
	public final Transform transform;
	private final Layer layer;
	private final TileMatrixSet tileMatrixSet;

	public Service(URL url, String layer) throws IOException {
		this.url = url;

		Map<String, String> params = new HashMap<>();
		params.put("SERVICE", "WMTS");
		params.put("REQUEST", "GetCapabilities");
		URL getCapabilities = UrlBuilder.createURL(url, params);
		InputStream in = getCapabilities.openStream();
		Capabilities capabilities = Capabilities.parse(in);
		//TODO: improve:
		this.layer = capabilities.Contents.Layer;
		this.tileMatrixSet = capabilities.Contents.getTileMatrixSetByCRS(Pattern.compile(".*:" + Epsg.WGS84.code + "$"));
		transform = new Transform(tileMatrixSet);
	}
	public URL getURL(WmtsTile tile) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("SERVICE", "WMTS");
		params.put("REQUEST", "GetTile");
		params.put("VERSION", "1.0.0");
		params.put("LAYER", layer.Identifier);
		params.put("STYLE", "default");
		params.put("FORMAT", "image/jpeg");
		params.put("TILEMATRIXSET", tileMatrixSet.Identifier);
		params.put("TILEMATRIX", tileMatrixSet.TileMatrix[tile.z].Identifier);
		params.put("TILEROW", Integer.toString((int) tile.y));
		params.put("TILECOL", Integer.toString((int) tile.x));
		return UrlBuilder.createURL(url, params);			
	}
}