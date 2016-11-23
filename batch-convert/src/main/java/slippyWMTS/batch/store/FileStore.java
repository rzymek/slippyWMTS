package slippyWMTS.batch.store;

import slippyWMTS.tile.SlippyTile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
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
    public void save(SlippyTile slippyTile, Image slippy) throws IOException {
        File dir = new File(baseDir, slippyTile.z + "/" + slippyTile.x + "/");
        dir.mkdirs();
        File output = new File(dir, slippyTile.y + "." + EXT);
        ImageIO.write((RenderedImage) slippy, EXT, output);
    }

    @Override
    public void close() throws Exception {

    }
}
