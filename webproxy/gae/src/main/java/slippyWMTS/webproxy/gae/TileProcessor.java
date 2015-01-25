package slippyWMTS.webproxy.gae;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import slippyWMTS.TileTranformation;
import slippyWMTS.area.TileBox;
import slippyWMTS.caching.Cache;
import slippyWMTS.caching.WeakHashMapCache;
import slippyWMTS.images.Composition;
import slippyWMTS.images.ImageOps;
import slippyWMTS.images.gae.GEAImageService;
import slippyWMTS.tile.WmtsTile;

class TileProcessor {
	private HashMap<String, Service> services = new HashMap<>();

	ImageOps imageOps = new GEAImageService();
	Cache cache = new WeakHashMapCache();

	public void serve(TileRequest tileReq, HttpServletResponse resp) throws IOException {
		Service service = services.get(tileReq.service);
		TileTranformation transformAndCrop = service.transform.transformAndCrop(tileReq.tile);
		TileBox<WmtsTile> tileBox = transformAndCrop.wmtsBox;

		List<Composition> compositions = new ArrayList<>();
		final int startX = (int) tileBox.topLeft.x;
		final int startY = (int) tileBox.topLeft.y;
		final int z = tileBox.topLeft.z;
		for (int x = startX; x < tileBox.bottomRight.x; x++) {
			for (int y = startY; y < tileBox.bottomRight.y; y++) {
				URL url = service.getURL(new WmtsTile(x, y, z));
				byte[] data = cache.get(url);
				final int col = x - startX;
				final int row = y - startY;
				int pixelX = (int) (col * service.tileMatrixSet.TileMatrix[z].TileWidth);
				int pixelY = (int) (row * service.tileMatrixSet.TileMatrix[z].TileHeight);
				compositions.add(new Composition(pixelX, pixelY, data));
			}
		}
		imageOps.composeAndCrop(compositions, transformAndCrop.cropBox, resp);
	}

	public void register(String name, String serviceURL, String layer) {
		try {
			services.put(name, new Service(new URL(serviceURL), layer));
		} catch (Exception e) {
			throw new RuntimeException("Failed to register " + name + " with " + serviceURL + " and layer " + layer, e);
		}
	}
}
