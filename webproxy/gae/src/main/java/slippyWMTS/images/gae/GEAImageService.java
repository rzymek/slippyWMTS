package slippyWMTS.images.gae;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Composite.Anchor;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;

import slippyWMTS.area.TileBox;
import slippyWMTS.images.Composition;
import slippyWMTS.images.ImageOps;
import slippyWMTS.position.DoubleXY;

public class GEAImageService implements ImageOps {
	private ImagesService imagesService = ImagesServiceFactory.getImagesService();

	@Override
	public void composeAndCrop(List<Composition> compositions, TileBox<DoubleXY> cropBox, HttpServletResponse resp) {		
//		imagesService.composite(
				makeComposites(compositions),
//				resWidth as int,
//				resHeight as int,
//				backgroundColor,
//				ImagesService.OutputEncoding.PNG
//			);

	}

	private List<Composite> makeComposites(List<Composition> compositions) {
		float opacity = 1;
		List<Composite> result = new ArrayList<>();
		for (Composition in : compositions) {
			Image image = ImagesServiceFactory.makeImage(in.imageData);
			result.add(ImagesServiceFactory.makeComposite(image, in.x, in.y, opacity, Anchor.TOP_LEFT));								
		}
		return result;
	}
}
