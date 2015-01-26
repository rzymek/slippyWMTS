package slippyWMTS.images;

import java.util.List;

import slippyWMTS.area.TileBox;
import slippyWMTS.position.DoubleXY;

public interface ImageOps<I> {

	I composeAndCrop(List<Composition<I>> compositions, TileBox<DoubleXY> cropBox, int width, int height);

}
