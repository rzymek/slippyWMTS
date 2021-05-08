package slippyWMTS.batch.utils;

public class Pair<K, V> {

    public final K key;
    public final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Pair{" +
            "key=" + key +
            ", value=" + value +
            '}';
    }
}
