package slippyWMTS.caching;

import java.net.URL;

public interface DataSource<K> {

	K get(URL url) throws Exception;

}
