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

package @OPT[|@USE::package.|]core

import javax.ws.rs.client.*;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
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
        final String inputData = new ObjectData(body).toJSonString();

        AtomicReference<WebTarget> builder =
                new AtomicReference<>(client.
                        target(uri).
                        path(path).
                        path(servicePath));

        params.entrySet().stream().
                forEach(e -> builder.set(builder.get().queryParam(e.getKey(), e.getValue().toString())));

        AtomicReference<Invocation.Builder> request = new AtomicReference<>(builder.get().request());

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
/*
    protected String getPath(JSon data, String pattern, List<List<String>> attributes) {
        (attributes map (data getValue _)).foldRight[Try[List[JSon]]](Success(Nil)) {
            (te, tl) => for (l <- tl; e <- te) yield e :: l
        } map {
            pattern.format(_: _*)
        }
    }

    protected JSon getValue(JSon data, List<String> path) {
        return data.getValue(path);
    }

    protected Map<String,JSon> getValues(data: JSon, path: List[String]) {
        path.foldRight[Try[Map[String, JSon]]](Success(Map[String, JSon]())) {
            (current, tresult) =>
            for (result <- tresult; value <- data getValue List(current)) yield result + (current -> value)
        }
    }

    protected JSon mergeData(data: List[Type]) {
        data.foldRight[Try[JSon]](Success(ObjectData(Map()))) {
            (te, tm) => for (e <- te.toJson; m <- tm) yield e overrides m
        }
    }
*/
}
