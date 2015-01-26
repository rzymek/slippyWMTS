package slippyWMTS.caching;

import java.net.URL;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import slippyWMTS.images.RawImage;

public class MemcacheSource implements DataSource<RawImage> {
	private DataSource<RawImage> parent;
	private MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
	private AsyncMemcacheService asyncMemcache = MemcacheServiceFactory.getAsyncMemcacheService();
	public MemcacheSource(DataSource<RawImage> parent) {
		this.parent = parent;		
	}
	@Override
	public RawImage get(URL url) throws Exception {
		final String key = url.toString();
		byte[] data = (byte[]) memcache.get(key);
		if(data == null) {
			RawImage img = parent.get(url);
			asyncMemcache.put(key, img.data);
			return img;
		}else{
			return new RawImage(data);
		}
	}

}
