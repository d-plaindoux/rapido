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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.client.*;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class BasicService {

    protected final String url;
    protected final String path;

    protected BasicService(String url, String path) {
        this.url = url;
        this.path = path;
    }

    //
    // Public behaviors
    //
    protected JSon httpRequest(String servicePath, String operation, Map<String, JSon> params, Map<String, JSon> body, Map<String, JSon> header) {
        final Client client = ClientBuilder.newClient();
        final URI uri = UriBuilder.fromUri(url).build();
        final String inputData = new JSon.ObjectData(body).toJSonString();

        final AtomicReference<WebTarget> builder =
                new AtomicReference<>(client.
                        target(uri).
                        path(path).
                        path(servicePath));

        params.entrySet().stream().
                forEach(e -> builder.set(builder.get().queryParam(e.getKey(), e.getValue().toString())));

        final AtomicReference<Invocation.Builder> request =
                new AtomicReference<>(builder.get().request());

        if (header.isEmpty()) {
            request.set(request.get().header("Content-Type", "application/json"));
        } else {
            header.entrySet().stream().
                    forEach(e -> request.set(request.get().header(e.getKey(), e.getValue().toString())));
        }

        switch (operation) {
            case "POST":
                return JSon.fromString(request.get().post(Entity.text(inputData), String.class));
            case "PUT":
                return JSon.fromString(request.get().put(Entity.text(inputData), String.class));
            case "GET":
                return JSon.fromString(request.get().get(String.class));
            case "DELETE":
                return JSon.fromString(request.get().delete(String.class));
            default:
                throw new UnsupportedOperationException(operation);
        }
    }

    protected String getPath(JSon data, String pattern, List<List<String>> attributes) {
        final List<String> values = attributes.stream().
            map(e -> getValue(data, e).toString()).
            collect(Collectors.toList());

        return String.format(pattern, values.toArray(new String[values.size()]));
    }

    protected JSon getValue(JSon data, List<String> path) {
        return data.getValue(path);
    }

    protected Map<String, JSon> getValues(JSon data, List<String> path) {
        final Map<String, JSon> map = Collections.emptyMap();
        path.stream().forEach(e -> map.put(e, getValue(data, Arrays.asList(e))));
        return map;
    }

    protected JSon mergeData(List<BasicType> data) {
        final AtomicReference<JSon> result = new AtomicReference<>(JSon.apply(new HashMap<String, JSon>()));
        data.stream().forEach(e -> result.set(e.toJson().overrides(result.get())));
        return result.get();
    }

}
