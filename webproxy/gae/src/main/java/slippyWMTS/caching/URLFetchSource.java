package slippyWMTS.caching;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import slippyWMTS.images.RawImage;

public class URLFetchSource implements DataSource<RawImage> {

	public RawImage get(URL url) throws Exception {
		Logger.getLogger("cache").info("[UrlFetch] " + url);
		try (DataInputStream in = new DataInputStream(url.openStream())) {
			return new RawImage(readFully(in));
		}
	}

	protected byte[] readFully(DataInputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}

}
