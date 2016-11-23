package slippyWMTS.batch.store;

import org.apache.commons.io.IOUtils;
import slippyWMTS.tile.SlippyTile;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MBTilesStore implements Store {
    private final Connection connection;
    private final Statement statement;

    public MBTilesStore(String dbfile) throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite:"+dbfile);
        statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.
        String ddl = getDDL();
        statement.execute(ddl);
    }

    private String getDDL() throws IOException {
        try(InputStream in = MBTilesStore.class.getResourceAsStream("/mbtiles-ddl.sql")) {
            return IOUtils.toString(in);
        }
    }

    @Override
    public void save(SlippyTile slippyTile, Image slippy) throws IOException {

    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
