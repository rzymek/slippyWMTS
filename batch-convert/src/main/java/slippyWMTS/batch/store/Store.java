package slippyWMTS.batch.store;

import slippyWMTS.tile.SlippyTile;

import java.awt.image.BufferedImage;
import java.sql.SQLException;

public interface Store extends AutoCloseable {
    void save(SlippyTile tile, BufferedImage slippy) throws Exception;

    void save(SlippyTile tile, byte[] bytes) throws Exception;

    boolean exists(SlippyTile tile) throws Exception;

    void saveEmpty(SlippyTile tile) throws Exception;

    void saveError(SlippyTile tile) throws Exception;

    void cleanup() throws SQLException, Exception;
}
