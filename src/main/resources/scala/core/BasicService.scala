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

import scala.util.{Failure, Success, Try}
import java.net.URI
import javax.ws.rs.core.UriBuilder
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.api.client.{WebResource, Client}

trait BasicService {
  val url: String
  val path: String

  //
  // Public behaviors
  //
  def httpRequest(servicePath: String, operation: String, params: Option[Map[String, JSon]], body: Option[Map[String, JSon]], header: Option[Map[String, JSon]]): Try[JSon] = {
    val client: Client = Client.create(new DefaultClientConfig)
    val uri: URI = UriBuilder.fromUri(url).build()
    val inputData: String = ObjectData(body.getOrElse(Map())).toJSonString
    try {
      val builder: WebResource = client.
        resource(uri).
        path(path).
        path(servicePath)

      val builderWithParams = params.getOrElse(Map()).foldRight(builder) {
        (kv, builder) => builder.queryParam(kv._1, kv._2.toString)
      }

      val builderWithHeader = header.getOrElse(Map()).foldRight(builderWithParams.header("Content-Type", "application/json")) {
        (kv, builder) => builder.header(kv._1, kv._2.toString)
      }

      operation match {
        case "POST" =>
          JSon.fromString(builderWithHeader.post(classOf[String], inputData))
        case "PUT" =>
          JSon.fromString(builderWithHeader.put(classOf[String], inputData))
        case "GET" =>
          JSon.fromString(builderWithHeader.get(classOf[String]))
        case "DELETE" =>
          JSon.fromString(builderWithHeader.delete(classOf[String]))
        case _ => throw new UnsupportedOperationException(operation)
      }
    } catch {
      case e: Throwable => Failure(e)
    }
  }

  def getPath(data: JSon, pattern: String, attributes: List[List[String]]): Try[String] = {
    (attributes map (data getValue _)).foldRight[Try[List[JSon]]](Success(Nil)) {
      (te, tl) => for (l <- tl; e <- te) yield e :: l
    } map {
      pattern.format(_: _*)
    }
  }

  def getValue(data: JSon, path: List[String]): Try[JSon] = {
    data getValue path
  }

  def getValues(data: JSon, path: List[String]): Try[Map[String, JSon]] =
    path.foldRight[Try[Map[String, JSon]]](Success(Map[String, JSon]())) {
      (current, tresult) =>
        for (result <- tresult; value <- data getValue List(current)) yield result + (current -> value)
    }

  def mergeData(data: List[Type]): Try[JSon] =
    data.foldRight[Try[JSon]](Success(ObjectData(Map()))) {
      (te, tm) => for (e <- te.toJson; m <- tm) yield e overrides m
    }
}