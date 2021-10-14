package slippyWMTS.batch.utils;

import org.junit.Test;
import rx.Observable;
import rx.exceptions.Exceptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ImageUtilsTest {

    @Test
    public void blank() throws Exception {
        assertInDir("/tiles/blank", true);
    }

    @Test
    public void full() throws Exception {
        assertInDir("/tiles/full", false);
    }

    @Test
    public void transparent() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/transparent.png")) {
            BufferedImage img = ImageIO.read(in);
            assertTrue(ImageUtils.isFullyTransparent(img));
        }
    }

    @Test
    public void notTransparent() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/not-fully-transparent.png")) {
            BufferedImage img = ImageIO.read(in);
            assertFalse(ImageUtils.isFullyTransparent(img));
        }
    }

    private void assertInDir(String dir, boolean expected) {
        Observable.just(dir)
            .map(ImageUtilsTest.class::getResource)
            .map(res -> checked(() -> res.toURI()))
            .map(uri -> checked(() -> Paths.get(uri)))
            .map(path -> checked(() -> Files.list(path).iterator()))
            .flatMap(files -> Observable.from(() -> files))
            .map(Path::toFile)
            .flatMap(this::isBlank)
            .toList()
            .toBlocking()
            .forEach(list -> {
                assertThat(
                    list.stream().map(String::valueOf).collect(Collectors.joining("\n")),
                    list,
                    everyItem(hasProperty("value", is(expected)))
                );
            });
    }

    private Observable<Pair<String, Boolean>> isBlank(File file) {
        return Observable.just(file)
            .map(f -> checked(() -> ImageIO.read(f)))
            .map(img -> ImageUtils.isBlank(img))
            .map(blank -> new Pair(file.getName(), blank));
    }

    public static <T> T checked(Func0Ex<T> f) {
        try {
            return f.call();
        } catch (Throwable e) {
            throw Exceptions.propagate(e);
        }
    }
}

