package slippyWMTS.caching;

import java.net.URL;

public interface Cache {

	byte[] get(URL url);

}
