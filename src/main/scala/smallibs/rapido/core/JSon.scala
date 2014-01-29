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

package smallibs.rapido.core

import scala.util.{Failure, Success, Try}

sealed trait JSon {
  p: JSon =>

  def getValue(path: List[String]): Try[JSon] =
    Failure(new Exception("Type mismatch: waiting for an object"))

  def setValue(path: List[String], result: JSon): Try[JSon] =
    path match {
      case Nil => Success(result)
      case (key :: path) => ObjectData(Map(key -> null)) setValue(path, result)
    }

  def toRaw: Any;

  def ++(data: JSon): Try[JSon] =
    Failure(new Exception("Type mismatch: waiting for an object"))

  def addToObjectData(data: ObjectData): Try[JSon] =
    Failure(new Exception("Type mismatch: waiting for an object"))

  def addToCompositeObjectData(data: CompositeObjectData): Try[JSon] =
    Failure(new Exception("Type mismatch: waiting for an object"))
}

case class StringData(s: String) extends JSon {
  def toRaw: Any = s
}

case class BooleanData(s: Boolean) extends JSon {
  def toRaw: Any = s
}

case class NumberData(s: Int) extends JSon {
  def toRaw: Any = s
}

case object NullData extends JSon {
  def toRaw: Any = null
}

case class ArrayData(l: List[JSon]) extends JSon {
  def toRaw: Any = for (e <- l) yield e.toRaw
}

case class ObjectData(map: Map[String, JSon]) extends JSon {
  def toRaw: Any = (for ((k, v) <- map) yield (k, v.toRaw)).toMap

  override def getValue(path: List[String]): Try[JSon] =
    path match {
      case Nil => Success(this)
      case (key :: path) =>
        map get key match {
          case None => Failure(new Exception(s"Field $key not found"))
          case Some(data) => data getValue path
        }
    }

  override def setValue(path: List[String], result: JSon): Try[JSon] =
    path match {
      case (key :: path) if map contains key =>
        ((map get key).get setValue(path, result)).map {
          value => ObjectData(Map(key -> value) ++ map)
        }
      case _ => super.setValue(path, result)
    }

  override def ++(data: JSon): Try[JSon] =
    data.addToObjectData(this)

  override def addToObjectData(data: ObjectData): Try[JSon] =
    Success(CompositeObjectData(List(data, this)))

  override def addToCompositeObjectData(data: CompositeObjectData): Try[JSon] =
    Success(CompositeObjectData(data.composite ++ List(this)))
}

case class CompositeObjectData(composite: List[ObjectData]) extends JSon {
  def toRaw: Any = ???

  override def getValue(path: List[String]): Try[JSon] =
    path match {
      case Nil => Success(this)
      case _ =>
        composite.foldLeft[Try[JSon]](Failure(new Exception)) {
          case (f@Failure(_), current) => current getValue path
          case (s, _) => s
        }
    }

  override def setValue(path: List[String], result: JSon): Try[JSon] =
    super.setValue(path, result)

  override def ++(data: JSon): Try[JSon] =
    data.addToCompositeObjectData(this)

  override def addToObjectData(data: ObjectData): Try[JSon] =
    Success(CompositeObjectData(data :: composite))

  override def addToCompositeObjectData(data: CompositeObjectData): Try[JSon] =
    Success(CompositeObjectData(data.composite ++ composite))
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
      case _ => Failure(new Exception("Not a JSon well formed formula"))
    }
}
