package slippyWMTS.batch.store;

import slippyWMTS.tile.SlippyTile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileStore implements Store {
    private static final String EXT = "jpg";
    private File baseDir;

    public FileStore(String dir) {
        this.baseDir = new File(dir);
        this.baseDir.mkdirs();
    }

    @Override
    public void save(SlippyTile slippyTile, BufferedImage slippy) throws IOException {
        File file = toFile(slippyTile);
        file.getParentFile().mkdirs();
        ImageIO.write(slippy, EXT, file);
    }

    private File toFile(SlippyTile slippyTile) {
        return new File(baseDir, slippyTile.z + "/" + slippyTile.x + "/" + slippyTile.y + "." + EXT);
    }

    @Override
    public boolean exists(SlippyTile tile) {
        boolean exists = toFile(tile).exists();
        return exists;
    }

    @Override
    public void saveEmpty(SlippyTile tile) throws Exception {
        toFile(tile).createNewFile();
    }

    @Override
    public void close() throws Exception {

    }
}
