package @OPT[|@USE::package.|]core;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

public interface collections {

    static <K, E> C<K, E> C(K k, E e) {
        return new C<>(k, e);
    }

    class C<K, E> {
        public final K key;
        public final E value;

        public C(K key, E value) {
            this.key = key;
            this.value = value;
        }
    }

    static <E> List<E> emptyList(Class<E> c) {
        return new List<E>();
    }

    static <E> List<E> List(E... e) {
        final List<E> es = new List<>();
        es.addAll(Arrays.asList(e));
        return es;
    }

    class List<E> extends ArrayList<E> {

        public List<E> append(List<E> xl) {
            final List<E> list = List();
            list.addAll(this);
            list.addAll(xl);
            return list;
        }

        public List<E> append(E... es) {
            return append(List(es));
        }

    }

    static <K, E> Map<K, E> Map() {
        return new Map<>();
    }

    class Map<K, E> extends HashMap<K, E> {

        public Map<K, E> append(Map<K, E> xm) {
            final Map<K, E> map = Map();
            map.putAll(this);
            map.putAll(xm);
            return map;
        }

        public Map<K, E> append(C<K,E>... v) {
            final Map<K, E> map = Map();
            map.putAll(this);
            List(v).stream().forEach(c -> map.put(c.key, c.value));
            return map;
        }
    }

}