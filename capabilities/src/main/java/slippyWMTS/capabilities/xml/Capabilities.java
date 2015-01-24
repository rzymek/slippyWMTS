package slippyWMTS.capabilities.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Capabilities", namespace = Capabilities.WMTS)
public class Capabilities {
  static final String WMTS = "http://www.opengis.net/wmts/1.0";
  static final String OWS = "http://www.opengis.net/ows/1.1";

  @XmlElement(namespace = WMTS)
  public Contents Contents;

  public static class Contents {
    @XmlElement(namespace = WMTS)
    public Layer Layer;
    @XmlElement(namespace = WMTS)
    public TileMatrixSet[] TileMatrixSet;
  }

  public static class Layer {
    @XmlElement(namespace = WMTS)
    public TileMatrixSetLink[] TileMatrixSetLink;

  }

  public static class TileMatrixSetLink {
    @XmlElement(namespace = WMTS)
    public String TileMatrixSet;
    @XmlElement(namespace = WMTS)
    public TileMatrixSetLimits[] TileMatrixSetLimits;

  }

  public static class TileMatrixSetLimits {
    @XmlElement(namespace = WMTS)
    public TileMatrixLimits TileMatrixLimits;
  }

  public static class TileMatrixLimits {
    @XmlElement(namespace = WMTS)
    public String TileMatrix;
    @XmlElement(namespace = WMTS)
    public int MinTileRow;
    @XmlElement(namespace = WMTS)
    public int MaxTileRow;
    @XmlElement(namespace = WMTS)
    public int MinTileCol;
    @XmlElement(namespace = Capabilities.WMTS)
    public int MaxTileCol;
  }

  public static class TileMatrixSet {
    @XmlElement(namespace = OWS)
    public String Identifier;
    @XmlElement(namespace = OWS)
    public String SupportedCRS;
    @XmlElement(namespace = WMTS)
    public TileMatrix TileMatrix;
  }

  public static class TileMatrix {
    @XmlElement(namespace = OWS)
    public String Identifier;
    @XmlElement(namespace = WMTS)
    public double ScaleDenominator;
    @XmlElement(namespace = WMTS)
    @XmlList
    public double[] TopLeftCorner;
    @XmlElement(namespace = WMTS)
    public double TileWidth;
    @XmlElement(namespace = WMTS)
    public double TileHeight;
    @XmlElement(namespace = WMTS)
    public double MatrixWidth;
    @XmlElement(namespace = WMTS)
    public double MatrixHeight;
  }
}
