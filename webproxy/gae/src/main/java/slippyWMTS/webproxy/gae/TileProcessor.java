package slippyWMTS.webproxy.gae;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import slippyWMTS.area.TileBox;
import slippyWMTS.tile.WmtsTile;

class TileProcessor {
	private HashMap<String, Service> services = new HashMap<>();

	public void serve(TileRequest tileReq, HttpServletResponse resp) throws IOException {
		Service service = services.get(tileReq.service);
		TileBox<WmtsTile> tileBox = service.transform.transform(tileReq.tile);

		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		out.println(service.getURL(tileBox.topLeft));
	}

	public void register(String name, String serviceURL, String layer) {
		try {
			services.put(name, new Service(new URL(serviceURL), layer));
		} catch (Exception e) {
			throw new RuntimeException("Failed to register " + name + " with " + serviceURL + " and layer " + layer, e);
		}
	}
}
