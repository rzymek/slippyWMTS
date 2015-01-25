package slippyWMTS.caching;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.WeakHashMap;

public class WeakHashMapCache implements Cache {
	private WeakHashMap<URL, byte[]> cache = new WeakHashMap<>();

	@Override
	public byte[] get(URL url) {
		byte[] data = cache.get(url);
		if (data == null) {
			data = download(url);
			cache.put(url, data);
		}
		return data;
	}

	private byte[] download(URL url) {
		try {
			try (DataInputStream in = new DataInputStream(url.openStream())) {
				return readFully(in);
			}
		} catch (Exception ex) {
			System.err.println(ex);
			return null;
		}
	}

	private byte[] readFully(DataInputStream is) throws IOException {
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
