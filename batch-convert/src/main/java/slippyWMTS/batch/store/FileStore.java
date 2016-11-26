package slippyWMTS.batch.store;

import org.apache.commons.io.FileUtils;
import slippyWMTS.tile.SlippyTile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class FileStore implements Store {
    private static final String EXT = "jpg";
    private File baseDir;

    public FileStore(String dir) {
        this.baseDir = new File(dir);
        this.baseDir.mkdirs();
    }

    @Override
    public void save(SlippyTile slippyTile, BufferedImage slippy) throws IOException {
        File file = toFileExt(slippyTile);
        ImageIO.write(slippy, EXT, file);
    }

    private File toFileExt(SlippyTile slippyTile) {
        File file = toFile(slippyTile);
        file.getParentFile().mkdirs();
        return file;
    }

    private File toFile(SlippyTile slippyTile) {
        return new File(baseDir, slippyTile.z + "/" + slippyTile.x + "/" + slippyTile.y + "." + EXT);
    }

    @Override
    public boolean exists(SlippyTile tile) {
        return toFile(tile).exists();
    }

    @Override
    public void saveEmpty(SlippyTile tile) throws Exception {
        toFileExt(tile).createNewFile();
    }

    @Override
    public void saveError(SlippyTile tile) throws Exception {
        FileUtils.writeStringToFile(toFileExt(tile),"error");
    }

    @Override
    public void close() throws Exception {

    }
}
