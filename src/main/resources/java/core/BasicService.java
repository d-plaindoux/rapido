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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.client.*;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        final WebTarget builder =
                Collections.<Map.Entry<String, JSon>>List().append(params.entrySet()).
                        foldLeft(client.target(uri).path(path).path(servicePath)).apply(
                        (b, e) -> b.queryParam(e.getKey(), e.getValue())
                );

        final Invocation.Builder request = Collections.<Map.Entry<String, JSon>>List().append(header.entrySet()).
                foldLeft(builder.request()).apply(
                (b, e) -> b.header(e.getKey(), e.getValue())
        ).header("Content-Type", "application/json");

        switch (operation) {
            case "POST":
                return JSon.fromString(request.post(Entity.entity(inputData, APPLICATION_JSON), String.class));
            case "PUT":
                return JSon.fromString(request.put(Entity.entity(inputData, APPLICATION_JSON), String.class));
            case "GET":
                return JSon.fromString(request.get(String.class));
            case "DELETE":
                return JSon.fromString(request.delete(String.class));
            default:
                throw new UnsupportedOperationException(operation);
        }
    }

    static protected String getPath(JSon data, String pattern, List<List<String>> attributes) {
        final List<String> values = Collections.<List<String>>List().append(attributes).
                foldLeft(Collections.<String>List()).apply(
                (v, e) -> {
                    v.add(getValue(data, e).toString());
                    return v;
                }
        );

        return String.format(pattern, values.toArray(new String[values.size()]));
    }

    static protected JSon getValue(JSon data, List<String> path) {
        return data.getValue(path);
    }

    static protected Map<String, JSon> getValues(JSon data, List<String> path) {
        return Collections.<String>List().append(path).
                foldLeft(Collections.<String, JSon>Map()).apply(
                (m, e) -> {
                    m.put(e, getValue(data, Collections.List(e)));
                    return m;
                }
        );
    }

    static protected JSon mergeData(List<BasicType> data) {
        return Collections.<BasicType>List().append(data).
                foldLeft(JSon.apply(new HashMap<String, JSon>())).apply(
                (r, e) -> e.toJson().overrides(r)
        );
    }
}