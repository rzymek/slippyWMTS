package slippyWMTS.area;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import slippyWMTS.tile.Tile;

public class TileBox<K> implements Iterable<K> {

	public K topLeft;
	public K topRight;
	public K bottomRight;
	public K bottomLeft;

	@Override
	public String toString() {
		return asList().toString();
	}

	public Iterator<K> iterator() {
		return asList().iterator();
	}

	@SuppressWarnings("unchecked")
	private List<K> asList() {
		return Arrays.asList(topLeft, topRight, bottomRight, bottomLeft);
	}
}
