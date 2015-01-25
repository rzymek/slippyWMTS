package slippyWMTS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Epsg {
	public static final Epsg WGS84 = new Epsg(4326);

	public final int code;

	private Epsg(int code) {
		this.code = code;
	}

	private static final Pattern CRS_URI = Pattern.compile("urn:ogc:def:crs:EPSG:.*:([0-9]+)");

	public static Epsg fromURI(String crsURI) {
		final Matcher matcher = CRS_URI.matcher(crsURI);
		if (matcher.matches()) {
			int code = Integer.parseInt(matcher.group(1));
			if (code == WGS84.code) {
				return WGS84;
			} else {
				return new Epsg(code);
			}
		} else {
			throw new IllegalArgumentException(crsURI + " does not match " + CRS_URI);
		}
	}

	@Override
	public int hashCode() {
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Epsg other = (Epsg) obj;
		if (code != other.code)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EPSG:" + code;
	}

	public static Epsg fromCode(int code) {
		//TODO: possibly cache objects?
		return new Epsg(code);
	}
}
