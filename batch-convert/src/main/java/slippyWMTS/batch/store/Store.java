package slippyWMTS.batch.store;

import slippyWMTS.tile.SlippyTile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;

public interface Store extends AutoCloseable {
    void save(SlippyTile slippyTile, BufferedImage slippy) throws Exception;
}
