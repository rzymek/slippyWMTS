var drawTileFrame = false;
function getTileBox(params) {
    var box = params.box;
    var source = window[params.source];
    var tileZ = params.z;
    var bBoxMinX = box.x1;
    var bBoxMaxX = box.x2;
    var bBoxMinY = box.y1;
    var bBoxMaxY = box.y2;

    function floor (x) {
        return Math.floor(x);
    }

    var scaleDenominators = source.tiles.EPSG_2180.map(function (it) {
        return it.scaleDenominator;
    });
    var matrixSizes = source.tiles.EPSG_2180.map(function (it) {
        return it.matrixSize;
    });
    var topLeftCorner = {
        y : 850000.0, // X
        x : 100000.0
    // Y
    };
    var tileWidth = 512;
    var tileHeight = 512;

    var scaleDenominator = scaleDenominators[tileZ];
    var matrixWidth = matrixSizes[tileZ].width;
    var matrixHeight = matrixSizes[tileZ].height;

    var metersPerUnit = 1;
    var pixelSpan = scaleDenominator * 0.28e-3 / metersPerUnit;

    var tileSpanX = tileWidth * pixelSpan;
    var tileSpanY = tileHeight * pixelSpan;
    var tileMatrixMinX = topLeftCorner.x;
    var tileMatrixMaxY = topLeftCorner.y;
    var tileMatrixMaxX = tileMatrixMinX + tileSpanX * matrixWidth;
    var tileMatrixMinY = tileMatrixMaxY - tileSpanY * matrixHeight;

    var tileMinCol = (bBoxMinX - tileMatrixMinX) / tileSpanX;
    var tileMaxCol = (bBoxMaxX - tileMatrixMinX) / tileSpanX;
    var tileMinRow = (tileMatrixMaxY - bBoxMaxY) / tileSpanY;
    var tileMaxRow = (tileMatrixMaxY - bBoxMinY) / tileSpanY;
    // to avoid requesting out-of-range tiles
    if (tileMinCol < 0)
        tileMinCol = 0;
    if (tileMaxCol >= matrixWidth)
        tileMaxCol = matrixWidth - 1;
    if (tileMinRow < 0)
        tileMinRow = 0;
    if (tileMaxRow >= matrixHeight)
        tileMaxRow = matrixHeight - 1;

    /*
     * document.writeln(JSON.stringify([ tileMinRow, tileMinCol, tileMaxRow,
     * tileMaxCol ]));
     */
    tileExact = {
        x1 : tileMinRow,
        y1 : tileMinCol,
        x2 : tileMaxRow,
        y2 : tileMaxCol
    };
    var epsilon = 1e-6;
    var tileBounds = {
        x1 : floor(tileExact.x1 + epsilon),
        y1 : floor(tileExact.y1 + epsilon),
        x2 : floor(tileExact.x2 - epsilon),
        y2 : floor(tileExact.y2 - epsilon)
    };
    return tileBounds;
}
