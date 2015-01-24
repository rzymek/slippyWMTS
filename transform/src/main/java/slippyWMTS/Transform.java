package slippyWMTS;

import slippyWMTS.area.TileBox;
import slippyWMTS.capabilities.WmtsTileMatrix;
import slippyWMTS.capabilities.WmtsTileMatrixSet;
import slippyWMTS.position.DoubleXY;
import slippyWMTS.position.LonLat;
import slippyWMTS.tile.SlippyTile;
import slippyWMTS.tile.WmtsTile;

public class Transform {
  private final WmtsTileMatrixSet tileMatrixSet;

  public Transform(WmtsTileMatrixSet tileMatrixSet) {
    super();
    this.tileMatrixSet = tileMatrixSet;
  }

  public TileTranformation transformAndCrop(SlippyTile tile) {
    TileBox<WmtsTile> wmtsBox = transform(tile);
    int wmtsZ = wmtsBox.topLeft.z;
    WmtsTileMatrix tileMatrix = tileMatrixSet.getTileMatrixForZ(wmtsZ);

    TileBox<DoubleXY> cropBox = new TileBox<DoubleXY>();
    cropBox.topLeft = getPixel(wmtsBox.topLeft, wmtsBox.topLeft, tileMatrix);
    cropBox.topRight = getPixel(wmtsBox.topLeft, wmtsBox.topRight, tileMatrix);
    cropBox.bottomLeft = getPixel(wmtsBox.topLeft, wmtsBox.bottomLeft, tileMatrix);
    cropBox.bottomRight = getPixel(wmtsBox.topLeft, wmtsBox.bottomRight, tileMatrix);

    TileTranformation tranformation = new TileTranformation();
    tranformation.wmtsBox = wmtsBox;
    tranformation.cropBox = cropBox;
    return tranformation;
  }

  private DoubleXY getPixel(WmtsTile topLeft, WmtsTile tile, WmtsTileMatrix tileMatrix) {
    DoubleXY pixel = new DoubleXY();
    pixel.x = (tile.x - topLeft.getX()) * tileMatrix.tileWidth;
    pixel.y = (tile.y - topLeft.getY()) * tileMatrix.tileHeight;
    return pixel;
  }

  private DoubleXY getDimentions(TileBox<WmtsTile> box) {
    DoubleXY dim = new DoubleXY();
    dim.x = box.topRight.x + 1 - box.topLeft.x;
    dim.y = box.bottomLeft.y + 1 - box.topLeft.y;
    return dim;
  }

  public TileBox<WmtsTile> transform(SlippyTile tile) {
    SlippyTile topLeftSlippy = tile;
    SlippyTile topRightSlippy = new SlippyTile(tile.z, tile.x + 1, tile.y);
    SlippyTile bottomRightSlippy = new SlippyTile(tile.z, tile.x + 1, tile.y + 1);
    SlippyTile bottomLeftSlippy = new SlippyTile(tile.z, tile.x, tile.y + 1);

    int wmtsZ = getWmtsZ(tile);

    TileBox<WmtsTile> wmtsBox = new TileBox<WmtsTile>();
    wmtsBox.topLeft = getWmtsTile(topLeftSlippy, wmtsZ);
    wmtsBox.topRight = getWmtsTile(topRightSlippy, wmtsZ);
    wmtsBox.bottomRight = getWmtsTile(bottomRightSlippy, wmtsZ);
    wmtsBox.bottomLeft = getWmtsTile(bottomLeftSlippy, wmtsZ);
    return wmtsBox;
  }

  private WmtsTile getWmtsTile(SlippyTile tile, int wmtsZ) {
    return getWmtsTile(tile.getTopLeftCoordinates(), wmtsZ);
  }

  private WmtsTile getWmtsTile(LonLat topLeft, int wmtsZ) {
    double x = topLeft.getLon();
    double y = topLeft.getLat();

    WmtsTileMatrix tileMatrix = tileMatrixSet.getTileMatrixForZ(wmtsZ);

    double pixelSpan = tileMatrix.scaleDenominator * 0.28e-3 / tileMatrixSet.metersPerUnit;

    double tileSpanX = (tileMatrix.tileWidth * pixelSpan);
    double tileSpanY = (tileMatrix.tileHeight * pixelSpan);
    double tileMatrixMinX = tileMatrix.topLeftCorner.x;
    double tileMatrixMaxY = tileMatrix.topLeftCorner.y;

    double col = (x - tileMatrixMinX) / tileSpanX;
    double row = (tileMatrixMaxY - y) / tileSpanY;
    return new WmtsTile(col, row, wmtsZ);
  }

  protected static int getWmtsZ(SlippyTile tile) {
    return tile.z - 6;
  }
}
