package slippyWMTS;

import slippyWMTS.area.TileBox;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrix;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSet;
import slippyWMTS.position.DoubleXY;
import slippyWMTS.position.LonLat;
import slippyWMTS.tile.SlippyTile;
import slippyWMTS.tile.WmtsTile;

public class Transform {
	private final TileMatrixSet tileMatrixSet;
	private final double metersPerUnit;
	private Epsg supportedCRS;
	protected TranformEpsg tranformEpsg = new TranformEpsg();

	public Transform(TileMatrixSet tileMatrixSet) {
		this.tileMatrixSet = tileMatrixSet;
		supportedCRS = Epsg.fromURI(tileMatrixSet.SupportedCRS);
		metersPerUnit = getMetersPerUnit(supportedCRS);
	}

	protected double getMetersPerUnit(Epsg crs) {
		if (crs == Epsg.WGS84) {
			double R = 6371 * 1000; // meteres
			double dLat = Math.toRadians(1);
			double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0);
			double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
			return R * c;
		} else {
			return 1; //warn: could not be the case!
		}
	}

	public TileTranformation transformAndCrop(SlippyTile tile) {
		TileBox<WmtsTile> wmtsBox = transform(tile);
		int wmtsZ = wmtsBox.topLeft.z;
		TileMatrix tileMatrix = tileMatrixSet.TileMatrix[wmtsZ];

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

	private DoubleXY getPixel(WmtsTile topLeft, WmtsTile tile, TileMatrix tileMatrix) {
		DoubleXY pixel = new DoubleXY();
		pixel.x = (tile.x - topLeft.getX()) * tileMatrix.TileWidth;
		pixel.y = (tile.y - topLeft.getY()) * tileMatrix.TileHeight;
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

	/**
	 * Based on  "OpenGIS Web Map Tile Service Implementation Standard"; version:1.0.0; #07-057r7
	 * Chapter 6.1 Tile matrix set – the geometry of the tiled space, page 8 
	 * http://www.opengeospatial.org/standards/wmts
	 */
	private WmtsTile getWmtsTile(LonLat topLeft, int wmtsZ) {
		double x = topLeft.getLon();
		double y = topLeft.getLat();

		TileMatrix tileMatrix = tileMatrixSet.TileMatrix[wmtsZ];

		double pixelSpan = tileMatrix.ScaleDenominator * 0.28e-3 / metersPerUnit;

		double tileSpanX = (tileMatrix.TileWidth * pixelSpan);
		double tileSpanY = (tileMatrix.TileHeight * pixelSpan);
		LonLat topLeftCorner = translateToLonLat(tileMatrix.TopLeftCorner);
		double tileMatrixMinX = topLeftCorner.x;
		double tileMatrixMaxY = topLeftCorner.y;

		double col = (x - tileMatrixMinX) / tileSpanX;
		double row = (tileMatrixMaxY - y) / tileSpanY;
		return new WmtsTile(col, row, wmtsZ);
	}

	private LonLat translateToLonLat(double[] coordinates) {
		return translateToLonLat(coordinates[1], coordinates[0]);
	}

	protected LonLat translateToLonLat(double x, double y) {
		return tranformEpsg.toLonLat(x, y, supportedCRS);
	}

	protected static int getWmtsZ(SlippyTile tile) {
		return tile.z - 6;
	}
}
