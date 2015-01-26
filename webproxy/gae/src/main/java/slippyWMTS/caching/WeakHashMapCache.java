package slippyWMTS.caching;

import java.net.URL;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import slippyWMTS.images.RawImage;

public class WeakHashMapCache implements DataSource<RawImage> {

	private DataSource<RawImage> parent;

	public WeakHashMapCache(DataSource<RawImage> parent) {
		this.parent = parent;
	}

	private WeakHashMap<URL, RawImage> cache = new WeakHashMap<>();

	@Override
	public RawImage get(URL url) throws Exception {
		RawImage data = cache.get(url);
		if (data == null) {
			data = parent.get(url);
			cache.put(url, data);
		} else {
			Logger.getLogger("cache").info("[WeakMap] " + url);
		}
		return data;
	}

}
