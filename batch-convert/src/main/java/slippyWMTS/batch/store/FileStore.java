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
        File dir = new File(baseDir, slippyTile.z + "/" + slippyTile.x + "/");
        dir.mkdirs();
        File output = new File(dir, slippyTile.y + "." + EXT);
        ImageIO.write(slippy, EXT, output);
    }

    @Override
    public void close() throws Exception {

    }
}
