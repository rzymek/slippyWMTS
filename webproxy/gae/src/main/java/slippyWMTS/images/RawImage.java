package slippyWMTS.images;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class RawImage {
	public byte[] data;

	public RawImage(byte[] imageData) {
		data = imageData;
	}

	public void serve(HttpServletResponse resp) throws IOException {
		resp.setContentType("image/png");
		ServletOutputStream out = resp.getOutputStream();
		try {
			out.write(data);
		} finally {
			out.close();
		}
	}

}
