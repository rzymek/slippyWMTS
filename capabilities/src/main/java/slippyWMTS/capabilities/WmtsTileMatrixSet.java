package slippyWMTS.capabilities;

public class WmtsTileMatrixSet {

  public double metersPerUnit;
  public WmtsTileMatrix[] set;

  public WmtsTileMatrix getTileMatrixForZ(int wmtsZ) {
    return set[wmtsZ];
  }

  public static double metersPerUnitEPSG4326() {
    double R = 6371 * 1000; // meteres
    double dLat = Math.toRadians(1);
    double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0);
    double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
    return R * c;
  }

}
