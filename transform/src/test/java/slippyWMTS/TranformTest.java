package slippyWMTS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import slippyWMTS.area.TileBox;
import slippyWMTS.capabilities.WmtsTileMatrix;
import slippyWMTS.capabilities.WmtsTileMatrixSet;
import slippyWMTS.position.LonLat;
import slippyWMTS.tile.SlippyTile;
import slippyWMTS.tile.WmtsTile;

public class TranformTest {
  @Test
  public void transform() {
    SlippyTile in = new SlippyTile(10, 570, 336);
    
    WmtsTileMatrixSet tileMatrixSet = new WmtsTileMatrixSet();
    tileMatrixSet.metersPerUnit = WmtsTileMatrixSet.metersPerUnitEPSG4326();
    tileMatrixSet.set = new WmtsTileMatrix[12];
    
    int wmtsZ = Transform.getWmtsZ(in);
    tileMatrixSet.set[wmtsZ] = new WmtsTileMatrix();
    
    tileMatrixSet.set[wmtsZ].scaleDenominator = 472470.23809523805;
    tileMatrixSet.set[wmtsZ].tileWidth = 512;
    tileMatrixSet.set[wmtsZ].tileHeight = 512;
    tileMatrixSet.set[wmtsZ].topLeftCorner = new LonLat(12,56);

        
    Transform transform = new Transform(tileMatrixSet);
    // tile.openstreetmap.org/10/570/336.png
    TileBox<WmtsTile> out = transform.transform(in);

    for (WmtsTile wmtsTile : out) {
      assertEquals(wmtsZ, wmtsTile.z);
    }
    assertTrue(out.topRight.x - out.topLeft.x > 0);
    assertTrue(out.topLeft.x - out.topRight.x < 1);
    System.out.println(out);
  }
}
