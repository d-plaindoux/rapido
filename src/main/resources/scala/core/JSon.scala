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

trait JSon {
  p: JSon =>

  def getValue(path: List[String]): Try[JSon] =
    (path, this) match {
      case (Nil, _) => Success(this)
      case ((key :: path), ObjectData(map)) =>
        map get key match {
          case None => Failure(new Exception(s"Field $key not found"))
          case Some(data) => data getValue path
        }
      case (_, _) => Failure(new Exception("Type mismatch: waiting for an object"))
    }

  def setValue(path: List[String], result: JSon): Try[JSon] =
    (path, this) match {
      case (Nil, _) => Success(result)
      case ((key :: path), ObjectData(map)) =>
        map get key match {
          case None => ObjectData(Map(key -> null)) setValue(path, result)
          case Some(data) => data setValue(path, result)
        }
      case ((key :: path), _) => ObjectData(Map(key -> null)) setValue(path, result)
    }

  def toRaw: Any =
    p match {
      case StringData(s) => s
      case BooleanData(s) => s
      case NumberData(s) => s
      case NullData => null
      case ArrayData(l) => for (e <- l) yield e.toRaw
      case ObjectData(m) => (for ((k, v) <- m) yield (k, v.toRaw)).toMap
    }

  def ++(data: JSon): Try[JSon] =
  // TODO -- Improve extension mechanism - function property?
    (p, data) match {
      case (ObjectData(m1), ObjectData(m2)) => Success(ObjectData(m1 ++ m2))
      case _ => Failure(new Exception)
    }
}

case class StringData(s: String) extends JSon

case class BooleanData(s: Boolean) extends JSon

case class NumberData(s: Int) extends JSon

case object NullData extends JSon

case class ArrayData(l: List[JSon]) extends JSon

case class ObjectData(l: Map[String, JSon]) extends JSon

object JSon {
  def apply(a: Any): Try[JSon] =
    a match {
      case s: String => Success(StringData(s))
      case s: Boolean => Success(BooleanData(s))
      case s: Int => Success(NumberData(s))
      case null => Success(NullData)
      case a: List[_] =>
        (for (e <- a) yield JSon(e)).foldRight[Try[List[JSon]]](Success(Nil)) {
          case (Success(a), Success(l)) => Success(a :: l)
          case (Failure(e), _) => Failure(new Exception("Not a JSon well formed formula", e))
          case (_, f@Failure(_)) => f
        } map {
          e => ArrayData(e)
        }
      case m: Map[_, _] =>
        (for ((k, v) <- m) yield (k.toString, JSon(v))).foldRight[Try[Map[String, JSon]]](Success(Map())) {
          case ((v, Success(k)), Success(m)) => Success(m ++ Map(v -> k))
          case ((_, Failure(e)), _) => Failure(new Exception("Not a JSon well formed formula for $v", e))
          case (_, f@Failure(_)) => f
        } map {
          e => ObjectData(e)
        }
    }
}
