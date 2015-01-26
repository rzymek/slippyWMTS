package slippyWMTS.webproxy.gae;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import slippyWMTS.caching.DataSource;
import slippyWMTS.caching.DataStoreSource;
import slippyWMTS.caching.MemcacheSource;
import slippyWMTS.caching.URLFetchSource;
import slippyWMTS.caching.WeakHashMapCache;
import slippyWMTS.images.RawImage;
import slippyWMTS.images.gae.GEAImageService;
import slippyWMTS.webproxy.gae.exceptions.HttpErrorResult;

public class TileServlet extends HttpServlet {
	private static final Pattern PATH_REGEX = Pattern.compile("/([^/]+)/([0-9]+)/([0-9]+)/([0-9]+).png");
	private static final long serialVersionUID = 1L;

	private TileProcessor<RawImage> tileProcessor = new TileProcessor<RawImage>(createCache(), new GEAImageService());

	public void init() {
		String[] geop = { "TOPO", "ORTO" };
		for (String service : geop) {
			tileProcessor.register(service.toLowerCase(), "http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/" + service, "EPSG:4180");
		}
		// tileProcessor.register("cien","http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/ISOK_CIEN","EPSG:4180");
	}

	private DataSource<RawImage> createCache() {
		return new WeakHashMapCache(new MemcacheSource(new DataStoreSource(new URLFetchSource())));
	}

	// http://localhost:8888/tile/topo/10/570/336.png
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			TileRequest tileReq = getTileRequest(req);
			RawImage result = tileProcessor.get(tileReq);
			result.serve(resp);
		} catch (HttpErrorResult ex) {
			resp.sendError(ex.errorCode);
		} catch (Exception ex) {
			throw new ServletException(ex);
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
