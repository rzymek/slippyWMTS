package slippyWMTS.webproxy.gae;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import slippyWMTS.Epsg;
import slippyWMTS.Transform;
import slippyWMTS.area.TileBox;
import slippyWMTS.capabilities.xml.Capabilities;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSet;
import slippyWMTS.tile.SlippyTile;
import slippyWMTS.tile.Tile;
import slippyWMTS.tile.WmtsTile;
import slippyWMTS.webproxy.gae.exceptions.HttpErrorResult;

class TileRequest {
	public String service;
	public SlippyTile tile = new SlippyTile(0, 0, 0);
}

class TileProcessor {
	private static class Service {
		public final URL url;
		public final Transform transform;

		public Service(URL url, String layer) throws IOException {
			this.url = url;

			Map<String, String> params = new HashMap<>();
			params.put("SERVICE", "WMTS");
			params.put("REQUEST", "GetCapabilities");
			URL getCapabilities = UrlBuilder.createURL(url, params);
			InputStream in = getCapabilities.openStream();
			Capabilities capabilities = Capabilities.parse(in);
			System.out.println(capabilities);
			//TODO: improve:
			TileMatrixSet tileMatrixSet = capabilities.Contents.getTileMatrixSetByCRS(Pattern.compile(".*:" + Epsg.WGS84.code + "$"));
			transform = new Transform(tileMatrixSet);
		}
	}

	private HashMap<String, Service> services = new HashMap<>();

	public void serve(TileRequest tileReq, HttpServletResponse resp) throws IOException {
		Service service = services.get(tileReq.service);
		TileBox<WmtsTile> tileBox = service.transform.transform(tileReq.tile);

		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		out.println(tileBox.toString());
		out.close();
	}

	public void register(String name, String serviceURL, String layer) {
		try {
			services.put(name, new Service(new URL(serviceURL), layer));
		} catch (Exception e) {
			throw new RuntimeException("Failed to register " + name + " with " + serviceURL + " and layer " + layer, e);
		}
	}
}

public class TileServlet extends HttpServlet {
	private static final Pattern PATH_REGEX = Pattern.compile("/([^/]+)/([0-9]+)/([0-9]+)/([0-9]+).png");
	private static final long serialVersionUID = 1L;

	private TileProcessor tileProcessor = new TileProcessor();

	public void init() {
		tileProcessor.register("topo", "http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/TOPO", "EPSG:4180");
	}

	// http://localhost:8888/tile/topo/10/570/336.png
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			TileRequest tileReq = getTileRequest(req);
			tileProcessor.serve(tileReq, resp);
		} catch (HttpErrorResult ex) {
			resp.sendError(ex.errorCode);
		}
	}

	private TileRequest getTileRequest(HttpServletRequest req) {
		String pathInfo = req.getPathInfo();
		Matcher matcher = PATH_REGEX.matcher(pathInfo);
		if (!matcher.matches()) {
			throw new HttpErrorResult(404);
		}
		TileRequest tileReq = new TileRequest();
		tileReq.service = matcher.group(1);
		tileReq.tile.z = Integer.parseInt(matcher.group(2));
		tileReq.tile.x = Integer.parseInt(matcher.group(3));
		tileReq.tile.y = Integer.parseInt(matcher.group(4));
		return tileReq;
	}
}
