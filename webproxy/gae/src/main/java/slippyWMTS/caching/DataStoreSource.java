package slippyWMTS.caching;

import java.net.URL;
import java.util.logging.Logger;

import slippyWMTS.images.RawImage;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

public class DataStoreSource implements DataSource<RawImage> {
	private DataSource<RawImage> parent;

	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private AsyncDatastoreService asyncSatastore = DatastoreServiceFactory.getAsyncDatastoreService();

	public DataStoreSource(DataSource<RawImage> parent) {

		this.parent = parent;
	}

	@Override
	public RawImage get(URL url) throws Exception {
		final String kind = "tile";
		final String propertyName = "data";
		try {
			Entity entity = datastore.get(KeyFactory.createKey(kind, url.toString()));
			final Blob blob = (Blob) entity.getProperty(propertyName);
			Logger.getLogger("cache").info("[DataStore] " + url);
			return new RawImage(blob.getBytes());
		} catch (EntityNotFoundException e) {
			RawImage rawImage = parent.get(url);
			Entity entity = new Entity(kind, url.toString());
			entity.setProperty(propertyName, new Blob(rawImage.data));
			asyncSatastore.put(entity);
			return rawImage;
		}
	}

}
