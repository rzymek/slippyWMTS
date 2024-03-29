package slippyWMTS.batch.store;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.sqlite.SQLiteConfig;
import slippyWMTS.tile.SlippyTile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class MBTilesStore implements Store {
    public static final String FORMAT_NAME = "jpg";
    private final Connection connection;
    private final PreparedStatement insert;
    private final PreparedStatement select;
    private final String dbfile;

    public MBTilesStore(String dbfile, String name, Type type) throws Exception {
        this.dbfile = dbfile;
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        try (Statement statement = connection.createStatement()) {
            String[] sql = getDDL(name, type).split(";");
            for (String expr : sql) {
                if (expr.trim().isEmpty()) {
                    continue;
                }
                statement.execute(expr);
            }
        }
        insert = connection.prepareStatement("insert or replace into tiles(zoom_level, tile_column, tile_row, tile_data) values (?,?,?,?)");
        select = connection.prepareStatement("select exists(select 1 from tiles where zoom_level=? and tile_column=? and tile_row=?)");
    }

    private String getDDL(String name, Type type) throws IOException {
        try (InputStream in = MBTilesStore.class.getResourceAsStream("/mbtiles-ddl.sql")) {
            return IOUtils.toString(in)
                .replace("%NAME%", name)
                .replace("%TYPE%", type.name());
        }
    }

    @Override
    public void save(SlippyTile slippyTile, BufferedImage slippy) throws Exception {
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            ImageIO.write(slippy, FORMAT_NAME, buf);
            save(slippyTile, buf.toByteArray());
        }
    }

    public void save(SlippyTile slippyTile, byte[] bytes) throws Exception {
        setPositionParams(insert, slippyTile);
        insert.setBytes(4, bytes);
        insert.execute();
    }


    private PreparedStatement setPositionParams(PreparedStatement statement, SlippyTile tile) throws SQLException {
        statement.setInt(1, tile.z);
        statement.setInt(2, tile.x);
        statement.setInt(3, getTmsY(tile));
        return statement;
    }

    private int getTmsY(SlippyTile slippyTile) {
        return (int) ((Math.pow(2, slippyTile.z) - 1) - slippyTile.y);
    }

    @Override
    public boolean exists(SlippyTile tile) throws SQLException {
        ResultSet rs = setPositionParams(select, tile)
            .executeQuery();
        rs.next();
        return rs.getBoolean(1);

    }

    @Override
    public void saveEmpty(SlippyTile tile) throws Exception {
        setPositionParams(insert, tile);
        insert.setNull(4, Types.BLOB);//mark for removal
        insert.execute();

    }

    @Override
    public void saveError(SlippyTile tile) throws SQLException {
        setPositionParams(insert, tile);
        insert.setBytes(4, new byte[0]);//mark for retry
        insert.execute();
    }

    @Override
    public void cleanup() throws SQLException {

        //13502420
        try (Statement statement = connection.createStatement()) {
            statement.execute("delete from tiles where tile_data is null");
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();

        //13510040	topo.mbtiles
        SQLiteConfig config = new SQLiteConfig();
        config.setTempStoreDirectory(".");
        config.setTempStore(SQLiteConfig.TempStore.FILE);
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbfile, config.toProperties())) {
            Statement statement = connection.createStatement();
            System.out.println("ANALYZE");
            statement.execute("analyze");
            System.out.println("VACUUM");
//            statement.execute("vacuum");
        }
    }

    public enum Type {
        overlay,
        baselayer
    }
}
