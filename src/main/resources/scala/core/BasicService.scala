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

package @OPT[|@USE::package.|] core

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait BasicService {
  val url: String
  val path: String

  //
  // Public behaviors
  //

  def http_request(path: String, operation: String, body: Option[Any], header: Option[Any], implicit_header: Option[Any]): Try[Any] =
    ???

  def get_path(data: JSon, pattern: String, attributes: List[List[String]]): Try[String] = {
    (attributes map (data getValue (_))).foldRight[Try[List[JSon]]](Success(Nil)) {
      case (Success(e), Success(l)) => Success(e :: l)
      case (Failure(e), _) => Failure(e)
      case (_, f@Failure(e)) => f
    } map {
      pattern.format(_)
    }
  }

}