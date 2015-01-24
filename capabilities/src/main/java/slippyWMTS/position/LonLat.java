package slippyWMTS.position;

public class LonLat extends DoubleXY {

  public LonLat(double lon, double lat) {
    setLon(lon);
    setLat(lat);
  }

  public LonLat() {
  }

  public double getLon() {
    return x;
  }

  public double getLat() {
    return y;
  }

  public void setLon(double lon) {
    x = lon;
  }

  public void setLat(double lat) {
    y = lat;
  }

}
