package slippyWMTS.webproxy.gae;

import slippyWMTS.tile.SlippyTile;

class TileRequest {
	public String service;
	public SlippyTile tile = new SlippyTile(0, 0, 0);
}