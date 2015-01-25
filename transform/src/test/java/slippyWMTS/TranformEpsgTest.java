package slippyWMTS;

import static org.junit.Assert.*;

import org.junit.Test;

import slippyWMTS.position.LonLat;

public class TranformEpsgTest {
	@Test
	public void wsg84() {
		TranformEpsg tranform = new TranformEpsg();
		double x = 21.56654364;
		double y = 52.12312312;
		LonLat lonLat = tranform.toLonLat(x, y, Epsg.WGS84);
		assertEquals(x, lonLat.x, 0E-10);
		assertEquals(y, lonLat.y, 0E-10);
	}
	
	@Test
	public void merkator() {
		final Epsg merkator = Epsg.fromCode(3857);
		TranformEpsg tranform = new TranformEpsg();
		tranform.define(merkator, "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs");
		
		int x = 2442557;
		int y = 6908949;
		LonLat lonLat = tranform.toLonLat(x, y, merkator);
		assertEquals(21.94186285433126, lonLat.x, 0E-8);
		assertEquals(52.59781864599655, lonLat.y, 0E-8);
	}
}
