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

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
                return JSon.fromString(request.get().
                        post(Entity.entity(inputData, MediaType.APPLICATION_JSON), String.class));
            case "PUT":
                return JSon.fromString(request.get().
                        put(Entity.entity(inputData, MediaType.APPLICATION_JSON), String.class));
            case "GET":
                return JSon.fromString(request.get().get(String.class));
            case "DELETE":
                return JSon.fromString(request.get().delete(String.class));
            default:
                throw new UnsupportedOperationException(operation);
        }
    }

    static protected String getPath(JSon data, String pattern, List<List<String>> attributes) {
        final List<String> values = Collections.List();
        attributes.forEach(e -> values.add(getValue(data, e).toString()));

        return String.format(pattern, values.toArray(new String[values.size()]));
    }

    static protected JSon getValue(JSon data, List<String> path) {
        return data.getValue(path);
    }

    static protected Map<String, JSon> getValues(JSon data, List<String> path) {
        final Map<String, JSon> map = Collections.Map();
        path.stream().forEach(e -> map.put(e, getValue(data, Collections.List(e))));
        return map;
    }

    static protected JSon mergeData(List<? extends BasicType> data) {
        final AtomicReference<JSon> result = new AtomicReference<>(JSon.apply(new HashMap<String, JSon>()));
        data.stream().forEach(e -> result.set(e.toJson().overrides(result.get())));
        return result.get();
    }
}
