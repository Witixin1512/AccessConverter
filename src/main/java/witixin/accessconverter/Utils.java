package witixin.accessconverter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Utils {

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <K, V> Map<K, V> makeMap(Function<Map<K, V>, Map<K, V>> genericFunction) {
        Map<K, V> map = new HashMap<>();
        return genericFunction.apply(map);
    }
}
