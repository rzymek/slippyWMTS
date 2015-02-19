package slippyWMTS;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class UrlBuilder {
	private static final String UTF8 = StandardCharsets.UTF_8.name();

	private UrlBuilder() {
	}

	public static URL createURL(URL base, Map<String, String> parameters) {
		try {
			StringBuilder url = new StringBuilder(base.toString());
			if (base.getQuery() == null)
				url.append('?');
			else
				url.append('&');
			Iterator<Entry<String, String>> it = parameters.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> pair = it.next();
				url.append(URLEncoder.encode(pair.getKey(), UTF8)).append('=').append(URLEncoder.encode(pair.getValue(), UTF8));
				if (it.hasNext()) {
					url.append("&");
				}
			}
			return new URL(url.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("JRE Inconsistency: no UTF-8?");
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Error creating url " + base + "  with " + parameters, ex);
		}
	}
}
