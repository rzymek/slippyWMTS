package slippyWMTS.images;

public class Composition<I> {

	public final I imageData;
	public final int x;
	public final int y;

	public Composition(int x, int y, I imageData) {
		this.x = x;
		this.y = y;
		this.imageData = imageData;
	}

}
