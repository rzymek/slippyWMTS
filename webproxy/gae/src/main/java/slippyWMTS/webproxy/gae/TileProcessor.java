package slippyWMTS.webproxy.gae;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import slippyWMTS.TileTranformation;
import slippyWMTS.area.TileBox;
import slippyWMTS.caching.DataSource;
import slippyWMTS.images.Composition;
import slippyWMTS.images.ImageOps;
import slippyWMTS.tile.Tile;
import slippyWMTS.tile.WmtsTile;

class TileProcessor<I> {
	private HashMap<String, Service> services = new HashMap<>();

	Tile getOrigin(TileBox<? extends WmtsTile> box) {
		Tile tile = new Tile();
		tile.x = (int) box.topLeft.x;
		tile.y = (int) box.topLeft.y;
		tile.z = box.topLeft.z;
		return tile;
	}

	private ImageOps<I> imageOps;
	private DataSource<I> cache;
	
	public TileProcessor(DataSource<I> cache, ImageOps<I> imageOps) {
		this.cache = cache;
		this.imageOps = imageOps;		
	}

	public I get(TileRequest tileReq) throws Exception {
		Service service = services.get(tileReq.service);
		TileTranformation transformAndCrop = service.transform.transformAndCrop(tileReq.tile);
		TileBox<WmtsTile> tileBox = transformAndCrop.wmtsBox;

		List<Composition<I>> compositions = new ArrayList<>();
		final int startX = (int) tileBox.topLeft.x;
		final int startY = (int) tileBox.topLeft.y;
		final int z = tileBox.topLeft.z;
		final int tileWidth = service.tileMatrixSet.TileMatrix[z].TileWidth;
		final int tileHeight = service.tileMatrixSet.TileMatrix[z].TileHeight;
		int tileCountX = (int) (tileBox.bottomRight.x - startX + 1);
		int tileCountY = (int) (tileBox.bottomRight.y - startY + 1);
		int width = tileCountX * tileWidth;
		int height = tileCountY * tileHeight;
		for (int x = startX; x < tileBox.bottomRight.x; x++) {
			for (int y = startY; y < tileBox.bottomRight.y; y++) {
				URL url = service.getURL(new WmtsTile(x, y, z));
				I data = cache.get(url);
				final int col = x - startX;
				final int row = y - startY;
				int pixelX = (int) (col * tileWidth);
				int pixelY = (int) (row * tileHeight);
				compositions.add(new Composition<I>(pixelX, pixelY, data));
			}
		}
		return imageOps.composeAndCrop(compositions, transformAndCrop.cropBox, width, height);
	}

	public void register(String name, String serviceURL, String layer) {
		try {
			services.put(name, new Service(new URL(serviceURL), layer));
		} catch (Exception e) {
			throw new RuntimeException("Failed to register " + name + " with " + serviceURL + " and layer " + layer, e);
		}
	}
}
