package slippyWMTS.batch.utils;

import java.util.concurrent.TimeUnit;

public class RxUtils {
    public static int sleep(int v, TimeUnit seconds) {
        try {
            Thread.sleep(seconds.toMillis(v));
        } catch (InterruptedException e) {
        }
        return v;
    }
}
