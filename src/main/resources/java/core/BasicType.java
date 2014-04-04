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

import static @OPT[|@USE::package.|]core.collections.List;
import static @OPT[|@USE::package.|]core.collections.Map;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class BasicType {

    public static class VirtualValue {
        public final List<String> path;
        public final String pattern;
        public final List<List<String>> attributes;

        public VirtualValue(List<String> path, String pattern, List<List<String>> attributes) {
            this.path = path;
            this.pattern = pattern;
            this.attributes = attributes;
        }
    }

    private final JSon data;
    private final List<VirtualValue> virtualValues;

    protected BasicType(JSon data, List<VirtualValue> virtualValues) {
        this.data = data;
        this.virtualValues = virtualValues;
    }

    protected JSon getValue(List<String> path) {
        return data.getValue(path);
    }

    protected JSon setValue(List<String> path, JSon value) {
        return data.setValue(path, value);
    }

    private String getVirtualValue(String pattern, List<List<String>> attributes) {
        final List<String> parameters = List();
        attributes.forEach(l -> parameters.add(data.getValue(l).toString()));

        return String.format(pattern, parameters.toArray(new String[parameters.size()]));
    }

    protected JSon setVirtualValue(VirtualValue value) {
        return setValue(value.path, JSon.apply(getVirtualValue(value.pattern, value.attributes)));
    }

    public JSon toJson() {
        final AtomicReference<JSon> result = new AtomicReference<>(data);
        virtualValues.forEach(v -> result.set(setVirtualValue(v).overrides(result.get())));

        return result.get();
    }
}
