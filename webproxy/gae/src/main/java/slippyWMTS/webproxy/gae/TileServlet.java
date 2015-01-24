package slippyWMTS.webproxy.gae;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import slippyWMTS.tile.Tile;
import slippyWMTS.webproxy.gae.exceptions.HttpErrorResult;

class TileRequest {
  public String service;
  public Tile tile = new Tile();
}
class TileProcessor {

  public byte[] get(TileRequest tileReq) {
    // TODO Auto-generated method stub
    return null;
  }

  public void register(String string, String string2) {
    // TODO Auto-generated method stub
    
  }
  
}
public class TileServlet extends HttpServlet {
  private static final Pattern PATH_REGEX = Pattern.compile("/([^/]+)/([0-9]+)/([0-9]+)/([0-9]+).png");
  private static final long serialVersionUID = 1L;

  private TileProcessor tileProcessor;
  
  public void init() {
    tileProcessor.register("topo","http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/TOPO");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      TileRequest tileReq = getTileRequest(req);
      byte[] image = tileProcessor.get(tileReq);
      resp.setContentType("image/png");
      resp.setContentLength(image.length);
      ServletOutputStream out = resp.getOutputStream();
      try {
        out.write(image);
      }finally{
        out.close();
      }
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
