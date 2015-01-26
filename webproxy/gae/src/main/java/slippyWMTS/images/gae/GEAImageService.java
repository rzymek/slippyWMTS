package slippyWMTS.images.gae;

import java.util.ArrayList;
import java.util.List;

import slippyWMTS.area.TileBox;
import slippyWMTS.images.Composition;
import slippyWMTS.images.ImageOps;
import slippyWMTS.images.RawImage;
import slippyWMTS.position.DoubleXY;

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Composite.Anchor;
import com.google.appengine.api.images.CompositeTransform;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

public class GEAImageService implements ImageOps<RawImage> {
	private ImagesService imagesService = ImagesServiceFactory.getImagesService();

	@Override
	public RawImage composeAndCrop(List<Composition<RawImage>> compositions, TileBox<DoubleXY> cropBox, int width, int height) {
		double resWidth = cropBox.bottomRight.x;
		double resHeight = cropBox.bottomRight.y;
		long backgroundColor = 0;
		Image composite = imagesService.composite(makeComposites(compositions), (int) resWidth, (int) resHeight, backgroundColor,
				ImagesService.OutputEncoding.PNG);
		Transform crop = ImagesServiceFactory.makeCrop((cropBox.topLeft.x / resWidth), (cropBox.topLeft.y / resHeight), 1.0, 1.0);
		Transform resize = ImagesServiceFactory.makeResize(256, 256, /*allowStrech*/true);
		CompositeTransform transformations = ImagesServiceFactory.makeCompositeTransform().concatenate(crop).concatenate(resize);
		composite = imagesService.applyTransform(transformations, composite);
		return new RawImage(composite.getImageData());
	}

	private List<Composite> makeComposites(List<Composition<RawImage>> compositions) {
		float opacity = 1;
		List<Composite> result = new ArrayList<>();
		for (Composition<RawImage> in : compositions) {
			Image image = ImagesServiceFactory.makeImage(in.imageData.data);
			result.add(ImagesServiceFactory.makeComposite(image, in.x, in.y, opacity, Anchor.TOP_LEFT));
		}
		return result;
	}
}
