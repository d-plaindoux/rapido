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

import scala.util.Success
import scala.util.Try

case class VirtualValue(path: List[String], model: String, values: List[List[String]])

abstract class BasicType(in: JSon) {


  val virtualValues: List[VirtualValue]

  val data = in

  protected def getValue(path: List[String]): Try[JSon] = data getValue path

  protected def setValue(path: List[String], value: JSon): JSon = data setValue(path, value)

  def toJson: Try[JSon] = {
    virtualValues.foldLeft[List[Try[JSon]]](Nil) {
      case (result, VirtualValue(path, model, values)) => result :+ setVirtualValue(path, model, values)
    }.foldRight[Try[JSon]](Success(data)) {
      (current, result) => result flatMap (value => current map (_ overrides value))
    }
  }

  private def getVirtualValue(pattern: String, attributes: List[List[String]]): Try[String] = {
    (attributes map data.getValue).foldRight[Try[List[JSon]]](Success(Nil)) {
      (te, tl) => for (l <- tl; e <- te) yield e :: l
    } map {
      pattern.format(_: _*)
    }
  }

  protected def setVirtualValue(path: List[String], pattern: String, attributes: List[List[String]]): Try[JSon] = {
    getVirtualValue(pattern, attributes) map {
      value => setValue(path, StringData(value))
    }
  }
}
