package slippyWMTS.tile;

import slippyWMTS.position.LonLat;

public class SlippyTile extends Tile {

  public SlippyTile(int z, int x, int y) {
    this.z = z;
    this.x = x;
    this.y = y;
  }

  /**
   * http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Tile_bounding_box
   */
  public LonLat getTopLeftCoordinates() {
    LonLat coord = new LonLat();
    coord.setLon(x / Math.pow(2.0, z) * 360.0 - 180);
    double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
    coord.setLat(Math.toDegrees(Math.atan(Math.sinh(n))));
    return coord;
  }

}
