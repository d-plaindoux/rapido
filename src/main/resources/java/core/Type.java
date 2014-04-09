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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Type {

    public static <E> Function<JSon, List<E>> list(Function<JSon, E> f) {
        return e -> {
            final List<JSon> list = ((JSon.ArrayData) e).getData();
            return list.stream().map(j -> f.apply(j)).
                    collect(Collectors.toList());
        };
    }

    public static Type map() {
        return null;
    }

    public static Function<JSon, String> string() {
        return e -> (String) e.toRaw();
    }

    public static Function<JSon, Boolean> bool() {
        return e -> (boolean) e.toRaw();
    }

    public static Function<JSon, Integer> integer() {
        return e -> (int) e.toRaw();
    }

    public static <E> Function<JSon, E> data(Function<JSon, E> f) {
        return f::apply;
    }
}
