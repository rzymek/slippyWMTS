package slippyWMTS.batch.store;

import slippyWMTS.tile.SlippyTile;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface Store extends AutoCloseable {
    void save(SlippyTile tile, BufferedImage slippy) throws Exception;

    boolean exists(SlippyTile tile) throws Exception;

    void saveEmpty(SlippyTile tile) throws Exception;
}
