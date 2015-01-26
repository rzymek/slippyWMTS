package slippyWMTS.capabilities.xml;

import java.beans.Transient;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Capabilities", namespace = Capabilities.WMTS)
public class Capabilities {
  static final String WMTS = "http://www.opengis.net/wmts/1.0";
  static final String OWS = "http://www.opengis.net/ows/1.1";

  public static Capabilities parse(InputStream in) {
    try {
      JAXBContext context = JAXBContext.newInstance(Capabilities.class);
      Unmarshaller jaxb = context.createUnmarshaller();
      return (Capabilities) jaxb.unmarshal(in);
    } catch (JAXBException ex) {
      throw new IllegalArgumentException("Failed to parse Capabilities XML", ex);
    }
  }

  @XmlElement(namespace = WMTS)
  public Contents Contents;

  public static class Contents {
    @XmlElement(namespace = WMTS)
    public Layer Layer;
    @XmlElement(namespace = WMTS)
    public TileMatrixSet[] TileMatrixSet;

    @Transient
    public TileMatrixSet getTileMatrixSetByCRS(Pattern crs) {
      for (Capabilities.TileMatrixSet set : TileMatrixSet) {
        if (crs.matcher(set.SupportedCRS).matches()) {
          return set;
        }
      }
      throw new IllegalArgumentException("CRS not supported:" + crs);
    }
  }

  public static class Layer {
	    @XmlElement(namespace = OWS)
	    public String Identifier;
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
    public TileMatrix[] TileMatrix;
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
    public int TileWidth;
    @XmlElement(namespace = WMTS)
    public int TileHeight;
    @XmlElement(namespace = WMTS)
    public int MatrixWidth;
    @XmlElement(namespace = WMTS)
		public int MatrixHeight;
	}
}
