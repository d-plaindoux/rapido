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
import scala.util.parsing.json.JSON

sealed trait JSon {
  p: JSon =>

  def getValue(path: List[String]): Try[JSon] =
    path match {
      case Nil => Success(this)
      case _ => Failure(new Exception("Type mismatch: waiting for an object and not " + p))
    }

  def setValue(path: List[String], result: JSon): JSon =
    path.foldRight[JSon](result) {
      (current, result) => ObjectData(Map(current -> result))
    } overrides this

  def toRaw: Any

  def toJSonString: String

  def overrides(data: JSon): JSon = p

  def overridenByObjectData(data: ObjectData): JSon = data
}

case class StringData(s: String) extends JSon {
  def toRaw: Any = s

  override def toJSonString: String = "\"" + s + "\""

  override def toString: String = s
}

case class BooleanData(s: Boolean) extends JSon {
  def toRaw: Any = s

  override def toJSonString: String =
    if (s) "true" else "false"
}

case class NumberData(s: Int) extends JSon {
  def toRaw: Any = s

  override def toJSonString: String =
    s.toString
}

case object NullData extends JSon {
  def toRaw: Any = null

  override def toJSonString: String =
    "null"
}

case class ArrayData(data: List[JSon]) extends JSon {
  def toRaw: Any = for (e <- data) yield e.toRaw

  override def toJSonString: String =
    (for (e <- data) yield e.toJSonString).mkString("[", ",", "]")
}

case class ObjectData(data: Map[String, JSon]) extends JSon {
  def toRaw: Any = (for ((k, v) <- data) yield (k, v.toRaw)).toMap

  override def getValue(path: List[String]): Try[JSon] =
    path match {
      case (key :: path) =>
        data get key match {
          case None => Failure(new Exception(s"Field $key not found"))
          case Some(data) => data getValue path
        }
      case _ => super.getValue(path)
    }

  override def overrides(data: JSon): JSon =
    data.overridenByObjectData(this)

  override def overridenByObjectData(objectData: ObjectData): JSon = {
    val k1 = Set(objectData.data.keysIterator.toList: _*)
    val k2 = Set(data.keysIterator.toList: _*)
    val intersection = k1 & k2
    val r1 = for (key <- intersection) yield key -> (objectData.data(key) overrides data(key))
    val r2 = objectData.data.filterKeys(!intersection.contains(_)) ++ data.filterKeys(!intersection.contains(_))
    ObjectData((r1 ++ r2).toMap)
  }

  override def toJSonString: String = {
    def toString(k: String, e: String) = "\"" + k + "\":" + e
    (for ((k, e) <- data) yield toString(k, e.toJSonString)).mkString("{", ",", "}")
  }

}

object JSon {
  def apply(a: Any): Try[JSon] =
    a match {
      case s: String => Success(StringData(s))
      case s: Boolean => Success(BooleanData(s))
      case s: Int => Success(NumberData(s))
      case null => Success(NullData)
      case a: List[_] =>
        (for (e <- a) yield JSon(e)).foldRight[Try[List[JSon]]](Success(Nil)) {
          (te, tl) => for (e <- te; l <- tl) yield e :: l
        } map {
          e => ArrayData(e)
        }
      case m: Map[_, _] =>
        (for ((k, v) <- m) yield (k.toString, JSon(v))).foldRight[Try[Map[String, JSon]]](Success(Map())) {
          (tc, tm) => for (c <- tc._2; m <- tm) yield m ++ Map(tc._1 -> c)
        } map {
          e => ObjectData(e)
        }
      case _ => Failure(new Exception(s"Not a JSon well formed formula $a"))
    }

  def fromString(value: String): Try[JSon] =
    JSON parseFull value match {
      case None => Failure(new Exception(s"Not a JSON data $value"))
      case Some(value) => JSon(value)
    }
}