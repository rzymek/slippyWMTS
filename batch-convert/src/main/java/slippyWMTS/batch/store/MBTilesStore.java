package slippyWMTS.batch.store;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import slippyWMTS.tile.SlippyTile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class MBTilesStore implements Store {
    public static final String FORMAT_NAME = "jpg";
    private final Connection connection;
    private final PreparedStatement insert;

    public MBTilesStore(String dbfile) throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.
        String[] sql = getDDL().split(";");
        for (String expr : sql) {
            statement.execute(expr);
        }
        statement.close();
        insert = connection.prepareStatement("insert or replace into tiles(zoom_level, tile_column, tile_row, tile_data) values (?,?,?,?)");
    }

    private String getDDL() throws IOException {
        try (InputStream in = MBTilesStore.class.getResourceAsStream("/mbtiles-ddl.sql")) {
            return IOUtils.toString(in);
        }
    }

    @Override
    public void save(SlippyTile slippyTile, Image slippy) throws Exception {
        insert.setInt(1, slippyTile.z);
        insert.setInt(2, slippyTile.x);
        int y = (int) ((Math.pow(2, slippyTile.z) - 1) - slippyTile.y);
        insert.setInt(3, y);
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            ImageIO.write((RenderedImage) slippy, FORMAT_NAME, buf);
            insert.setBytes(4, buf.toByteArray());
        }
        insert.execute();
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
