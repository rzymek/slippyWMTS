package slippyWMTS.tile;

import slippyWMTS.position.LonLat;

public class SlippyTile extends Tile {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 256;
    /**
     * https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
     */
    public SlippyTile(LonLat c, int zoom) {
        final double lon = c.getLon();
        final double lat = c.getLat();
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1 << zoom))
            xtile = ((1 << zoom) - 1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1 << zoom))
            ytile = ((1 << zoom) - 1);
        this.z = zoom;
        this.x = xtile;
        this.y = ytile;
    }

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
