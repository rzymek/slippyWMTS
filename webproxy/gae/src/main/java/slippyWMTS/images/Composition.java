package slippyWMTS.images;

public class Composition {

	public final byte[] imageData;
	public final int x;
	public final int y;

	public Composition(int x, int y, byte[] imageData) {
		this.x = x;
		this.y = y;
		this.imageData = imageData;
	}

}
