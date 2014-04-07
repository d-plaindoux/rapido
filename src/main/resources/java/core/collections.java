package @OPT[|@USE::package.|]core;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface collections {

    static <E> List<E> emptyList(Class<E> c) {
        return new List<>();
    }

    static <E> List<E> List(E... e) {
        final List<E> es = new List<>();
        es.addAll(Arrays.asList(e));
        return es;
    }

    class List<E> extends ArrayList<E> {

        public List<E> append(java.util.List<E> xl) {
            final List<E> list = List();
            list.addAll(this);
            list.addAll(xl);
            return list;
        }

        public List<E> append(E... es) {
            return append(List(es));
        }

        public <R> Function<BiFunction<E, R, R>, R> foldRigth(R r) {
            return (BiFunction<E, R, R> f) -> {
                R result = r;
                for (int i = this.size(); i > 0; i--) {
                    result = f.apply(this.get(i - 1), result);
                }
                return result;
            };
        }
    }

    static <K, E> Map<K, E> Map() {
        return new Map<>();
    }

    class Map<K, E> extends HashMap<K, E> {

        public Map<K, E> append(java.util.Map<K, E> xm) {
            final Map<K, E> map = Map();
            map.putAll(this);
            map.putAll(xm);
            return map;
        }

        public Map<K, E> append(K k, E e) {
            final Map<K, E> map = Map();
            map.putAll(this);
            map.put(k, e);
            return map;
        }
    }
}