package slippyWMTS.batch.utils;

import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Test;
import rx.Observable;
import rx.exceptions.Exceptions;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ImageUtilsTest {

    @Test
    public void blank() throws Exception {
        Observable.just("/tiles/blank")
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
                            everyItem(hasProperty("value", is(true)))
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