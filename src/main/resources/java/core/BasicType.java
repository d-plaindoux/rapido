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

package

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@OPT[|@USE::package.|]core;

public abstract class BasicType {

    public static class VirtualValue {
        private final List<String> path;
        private final String model;
        private final List<List<String>> values;

        public VirtualValue(List<String> path, String model, List<List<String>> values) {
            this.path = path;
            this.model = model;
            this.values = values;
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
        return data.setValue(path, value)
    }

    private String getVirtualValue(String pattern, List<List<String>> attributes) {
        final List<String> parameters = attributes.stream().map(l -> data.getValue(l));
        return String.format(pattern, parameters.toString[parameters.size()]);
        s
    }

    protected JSon setVirtualValue(VirtualValue value) {
        return setValue(value.path, StringData(getVirtualValue(value.pattern, value.attributes)));
    }

    public JSon toJson() {
        final List<JSon> values = virtualValues.stream().
                map(v -> setValue(v)).collect(Collectors.toList());

        final AtomicReference<JSon> result = new AtomicReference<>(data);
        values.stream().forEach(v -> result.set(v.overrides(result.get())));

        return result.get();
    }
}