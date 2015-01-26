package slippyWMTS.images.gae;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Composite.Anchor;
import com.google.appengine.api.images.CompositeTransform;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

import slippyWMTS.area.TileBox;
import slippyWMTS.images.Composition;
import slippyWMTS.images.ImageOps;
import slippyWMTS.position.DoubleXY;

public class GEAImageService implements ImageOps {
	private ImagesService imagesService = ImagesServiceFactory.getImagesService();

  @Override
  public void composeAndCrop(List<Composition> compositions, TileBox<DoubleXY> cropBox, int width, int height, HttpServletResponse resp) {
    double resWidth = cropBox.bottomRight.x;
    double resHeight = cropBox.bottomRight.y;
    long backgroundColor = 0;
    Image composite = imagesService.composite(
			makeComposites(compositions),
			(int) resWidth ,
			(int) resHeight,
			backgroundColor,
			ImagesService.OutputEncoding.PNG
		);
		Transform crop = ImagesServiceFactory.makeCrop(
			(cropBox.topLeft.x / resWidth),
			(cropBox.topLeft.y / resHeight),
			1.0,
			1.0
		);		
		Transform resize = ImagesServiceFactory.makeResize(256, 256, /*allowStrech*/ true);
		CompositeTransform transformations = ImagesServiceFactory.makeCompositeTransform()
		    .concatenate(crop)
		    .concatenate(resize);
		composite = imagesService.applyTransform(transformations, composite);
		
		write(resp, composite);
	}

  private void write(HttpServletResponse resp, Image composite) {
    try {
			resp.setContentType("image/png");
			resp.getOutputStream().write(composite.getImageData());
		} catch (IOException e) {
			try {
				resp.setContentType("text/plain");
				e.printStackTrace(resp.getWriter());
			} catch (IOException e1) {
				e.printStackTrace();
				e1.printStackTrace();
			}
		}
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
