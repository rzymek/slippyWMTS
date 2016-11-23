package slippyWMTS.batch.store;

import slippyWMTS.tile.SlippyTile;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

public interface Store extends AutoCloseable {
    void save(SlippyTile slippyTile, Image slippy) throws IOException, SQLException, Exception;
}
