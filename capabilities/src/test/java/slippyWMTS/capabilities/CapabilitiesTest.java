package slippyWMTS.capabilities;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import slippyWMTS.capabilities.xml.Capabilities;
import slippyWMTS.capabilities.xml.Capabilities.Contents;
import slippyWMTS.capabilities.xml.Capabilities.Layer;
import slippyWMTS.capabilities.xml.Capabilities.TileMatrixSetLink;

public class CapabilitiesTest {

  private void printXML(Capabilities capabilites) throws JAXBException, PropertyException {
    JAXBContext context = JAXBContext.newInstance(Capabilities.class);
    Marshaller jaxb = context.createMarshaller();
    jaxb.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    jaxb.marshal(capabilites, System.out);
  }

  @Test
  public void print() throws Exception {
    Capabilities capabilities = new Capabilities();
    capabilities.Contents = new Contents();
    capabilities.Contents.Layer = new Layer();
    capabilities.Contents.Layer.TileMatrixSetLink = new TileMatrixSetLink[] { new TileMatrixSetLink() };
    capabilities.Contents.Layer.TileMatrixSetLink[0] = new TileMatrixSetLink();
    capabilities.Contents.Layer.TileMatrixSetLink[0].TileMatrixSet = "XXX";
    printXML(capabilities);
  }

  @Test
  public void parse() throws Exception {
    InputStream in = getClass().getResourceAsStream("/GetCapabilities.xml");
    JAXBContext context = JAXBContext.newInstance(Capabilities.class);
    Unmarshaller jaxb = context.createUnmarshaller();
    Capabilities capabilites = (Capabilities) jaxb.unmarshal(in);
    printXML(capabilites);
    assertEquals("EPSG:2180", capabilites.Contents.Layer.TileMatrixSetLink[0].TileMatrixSet);
    assertEquals(100000.0, capabilites.Contents.TileMatrixSet[0].TileMatrix.TopLeftCorner[1], 0.0);
  }
}
