package slippyWMTS;

import java.util.HashMap;
import java.util.Map;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import slippyWMTS.position.LonLat;

public class TranformEpsg {
	public static final CRSFactory CRS_FACTORY = new CRSFactory();
	private static CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
	private CoordinateReferenceSystem EPSG4326;
	
	public TranformEpsg() {
		EPSG4326 = CRS_FACTORY.createFromParameters("EPSG:4326","+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs ");
	}

	private static Map<Epsg, CoordinateTransform> cache = new HashMap<>();
	
	public LonLat toLonLat(double x, double y, Epsg crs) {
		if (crs == Epsg.WGS84) {
			return new LonLat(x, y);
		} else {
			CoordinateTransform  transform = getTranformationTo(crs);
			ProjCoordinate result = transform.transform(new ProjCoordinate(x, y), new ProjCoordinate());
			return new LonLat(result.x, result.y);
		}
	}

	private CoordinateTransform getTranformationTo(Epsg crs) {
		CoordinateTransform transform = cache.get(crs);
		if(transform == null) {
			final CoordinateReferenceSystem source = CRS_FACTORY.createFromName(crs.toString());
			transform = coordinateTransformFactory.createTransform(source, EPSG4326);
			cache.put(crs, transform);
		}
		return transform;
	}

	public void define(Epsg epsg, String paramStr) {
		CoordinateReferenceSystem crs = CRS_FACTORY.createFromParameters(epsg.toString(), paramStr);
		cache.put(epsg, coordinateTransformFactory.createTransform(crs, EPSG4326));
	}
}
