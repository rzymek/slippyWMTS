package slippyWMTS.webproxy.gae;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class UrlBuilderTest {
	@Test
	public void setQuery() throws Exception {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("tab","newest");
		params.put("q","UrlBuilder");
		final URL base = new URL("https://stackoverflow.com/search");
		URL result = UrlBuilder.createURL(base, params);
		assertEquals("https://stackoverflow.com/search?tab=newest&q=UrlBuilder", result.toString());
	}
	@Test
	public void appendQuery() throws Exception {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("q","UrlBuilder");
		final URL base = new URL("https://stackoverflow.com/search?tab=newest");
		URL result = UrlBuilder.createURL(base, params);
		assertEquals("https://stackoverflow.com/search?tab=newest&q=UrlBuilder", result.toString());
	}
}
