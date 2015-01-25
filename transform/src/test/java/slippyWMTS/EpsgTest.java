package slippyWMTS;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import org.junit.Test;

public final class EpsgTest {
	@Test
	public void positive() {
		int[] samples = { 3329, 3333, 2180, 4326, 32633 };
		for (int code : samples) {
			assertEquals(code, Epsg.fromURI("urn:ogc:def:crs:EPSG::" + code).code);
			assertEquals(code, Epsg.fromURI("urn:ogc:def:crs:EPSG:43a:" + code).code);
		}
	}

	@Test
	public void nerative() {
		final String str = "urn:ogc:def:crs:EPSG::utm";
		try {
			Epsg lonlat = Epsg.fromURI(str);
			assertEquals(4326, lonlat.code);
			fail("Expected exception");
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage(), containsString(str));
		}
	}
}
