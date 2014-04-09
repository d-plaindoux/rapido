/*
 * Copyright (C)2014 D. Plaindoux.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package @OPT[|@USE::package.|]core;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.*;
import java.util.stream.Collectors;

public interface JSon {

    <E> E toRaw();

    String toJSonString();

    // -----------------------------------------------------------------------------------------------------------------

    default JSon getValue(List<String> path) {
        if (path.isEmpty()) {
            return this;
        } else {
            throw new IllegalArgumentException("Type mismatch: waiting for an object and not " + path);
        }
    }

    default JSon setValue(List<String> path, JSon result) {
        Collections.List<String> list = Collections.<String>List().append(path);

        return list.foldRigth(result).
                apply((s, r) -> new ObjectData(Collections.<String, JSon>Map().append(s, r))).
                overrides(this);
    }

    default JSon overrides(JSon data) {
        return this;
    }

    default JSon overridenByObjectData(ObjectData data) {
        return data;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public class StringData implements JSon {

        private final String s;

        private StringData(String s) {
            this.s = s;
        }

        public String toRaw() {
            return s;
        }

        public String toJSonString() {
            return '"' + s + '"';
        }

        public String toString() {
            return s;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public class BooleanData implements JSon {

        private Boolean s;

        private BooleanData(Boolean s) {
            this.s = s;
        }

        public Boolean toRaw() {
            return s;
        }

        public String toJSonString() {
            if (s)
                return "true";
            else
                return "false";
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public class NumberData implements JSon {

        private final Long s;

        private NumberData(Long s) {
            this.s = s;
        }

        private NumberData(Integer s) {
            this.s = Integer.toUnsignedLong(s);
        }

        private NumberData(Double s) {
            this.s = Double.doubleToRawLongBits(s);
        }

        public Long toRaw() {
            return s;
        }

        public String toJSonString() {
            return String.valueOf(this.s);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public class NullData implements JSon {
        public Object toRaw() {
            return null;
        }

        public String toJSonString() {
            return "null";
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public class ArrayData implements JSon {

        private final List<JSon> data;

        private ArrayData(List<JSon> data) {
            this.data = data;
        }

        public List<Object> toRaw() {
            return this.data.stream().map(JSon::toRaw).collect(Collectors.toList());
        }

        public String toJSonString() {
            String value =
                    this.data.stream().
                            map(JSon::toJSonString).
                            collect(Collectors.joining(", "));

            return '[' + value + ']';
        }

        public List<JSon> getData() {
            return data;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public class ObjectData implements JSon {

        private Map<String, JSon> data;

        public ObjectData(Map<String, JSon> data) {
            this.data = data;
        }

        public Map<String, Object> toRaw() {
            final Map<String, Object> raw = new HashMap<>();
            this.data.entrySet().stream().
                    forEach(e -> raw.put(e.getKey(), e.getValue().toRaw()));
            return raw;
        }

        public JSon getValue(List<String> path) {
            if (path.isEmpty()) {
                return JSon.super.getValue(path);
            } else {
                final String key = path.get(0);
                if (this.data.containsKey(key)) {
                    return this.data.get(key).getValue(path.subList(1, path.size()));
                } else {
                    throw new IllegalArgumentException(String.format("Field %s not found", key));
                }
            }
        }

        public JSon overrides(JSon data) {
            return data.overridenByObjectData(this);
        }

        public JSon overridenByObjectData(ObjectData objectData) {
            final Set<String> intersection =
                    objectData.data.keySet().stream().
                            filter(e -> data.keySet().contains(e)).
                            collect(Collectors.toSet());

            final Map<String, JSon> r = new HashMap<>();

            intersection.stream().
                    forEach(e -> r.put(e, objectData.data.get(e).overrides(data.get(e))));

            objectData.data.entrySet().stream().
                    filter(e -> !intersection.contains(e.getKey())).
                    forEach(e -> r.put(e.getKey(), objectData.data.get(e.getKey())));
            data.entrySet().stream().
                    filter(e -> !intersection.contains(e.getKey())).
                    forEach(e -> r.put(e.getKey(), data.get(e.getKey())));

            return new ObjectData(r);
        }

        public String toJSonString() {
            final String raw = this.data.entrySet().stream().
                    map(e -> '"' + e.getKey() + '"' + ":" + e.getValue().toJSonString()).
                    collect(Collectors.joining(","));

            return '{' + raw + '}';
        }
    }

    static JSon apply(Object object) {
        if (object == null) {
            return new NullData();
        } else if (object instanceof String) {
            return new StringData(String.class.cast(object));
        } else if (object instanceof Boolean) {
            return new BooleanData(Boolean.class.cast(object));
        } else if (object instanceof Integer) {
            return new NumberData(Integer.class.cast(object));
        } else if (object instanceof Double) {
            return new NumberData(Double.class.cast(object));
        } else if (object instanceof Long) {
            return new NumberData(Long.class.cast(object));
        } else if (object instanceof List) {
            @SuppressWarnings("unchecked")
            final List<Object> objects = List.class.cast(object);
            final List<JSon> jSons = objects.stream().map(JSon::apply).collect(Collectors.toList());
            return new ArrayData(jSons);
        } else if (object instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> record = Map.class.cast(object);

            final Map<String, JSon> result = new HashMap<>();
            record.entrySet().stream().
                    forEach(e -> result.put(e.getKey(), apply(e.getValue())));

            return new ObjectData(result);
        } else {
            throw new IllegalArgumentException(String.format("Not a JSon well formed formula %s", object.toString()));
        }
    }

    static Object transform(JsonElement element) {
        if (element.isJsonObject()) {
            final HashMap<String, Object> map = new HashMap<>();
            element.getAsJsonObject().entrySet().
                    forEach(e -> map.put(e.getKey(), transform(e.getValue())));

            return map;
        } else if (element.isJsonArray()) {
            final ArrayList<Object> array = new ArrayList<>();
            element.getAsJsonArray().iterator().
                    forEachRemaining(e -> array.add(transform(e)));

            return array;
        } else if (element.isJsonNull()) {
            return apply(null);
        } else if (element instanceof JsonPrimitive) {
            final JsonPrimitive primitive = (JsonPrimitive) element;
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isNumber()) {
                return primitive.getAsLong();
            } else if (primitive.isString()) {
                return primitive.getAsString();
            }
        }

        throw new IllegalAccessError();
    }

    static JSon fromString(String string) {
        return apply(transform(new JsonParser().parse(string)));
    }
}