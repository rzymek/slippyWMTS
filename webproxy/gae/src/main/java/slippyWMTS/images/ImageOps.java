package slippyWMTS.images;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import slippyWMTS.area.TileBox;
import slippyWMTS.position.DoubleXY;

public interface ImageOps {

	void composeAndCrop(List<Composition> compositions, TileBox<DoubleXY> cropBox, int width, int height, HttpServletResponse resp);

}
