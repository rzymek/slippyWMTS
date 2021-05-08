package slippyWMTS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import slippyWMTS.area.TileBox;
import slippyWMTS.capabilities.xml.Capabilities;
import slippyWMTS.tile.SlippyTile;
import slippyWMTS.tile.WmtsTile;

public class TranformTest {
  @Test
  public void transform() {
    SlippyTile in = new SlippyTile(10, 570, 336);

    Capabilities capabilities = Capabilities.parse(getClass().getResourceAsStream("/GetCapabilities.xml"));
    int wmtsZ = Transform.getWmtsZ(in);

    Transform transform = new Transform(capabilities.Contents.TileMatrixSet[1]);
    // tile.openstreetmap.org/10/570/336.png
    TileBox<WmtsTile> out = transform.transform(in);

//    System.out.println(out);
    for (WmtsTile wmtsTile : out) {
      assertEquals(wmtsZ, wmtsTile.z);
      assertTrue("x>13.7", wmtsTile.x > 13.7);
      assertTrue("x<14.4", wmtsTile.x < 14.4);
      assertTrue("y>5.7", wmtsTile.y > 5.7);
      assertTrue("y<6.2", wmtsTile.y < 6.2);
    }
    assertTrue(out.topRight.x - out.topLeft.x > 0);
    assertTrue(out.topLeft.x - out.topRight.x < 1);
  }
}
