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

import org.specs2.mutable._
import scala.util.Success
import smallibs.rapido.core

object JSonSpec extends Specification {
  "JSon internalized" should {
    "provide NullData" in {
      JSon(null) mustEqual Success(NullData)
    }

    "provide StringData" in {
      JSon("hello") mustEqual Success(StringData("hello"))
    }

    "provide NumberData" in {
      JSon(123) mustEqual Success(NumberData(123))
    }

    "provide BooleanData" in {
      JSon(true) mustEqual Success(BooleanData(true))
    }

    "provide ListData" in {
      JSon(List(true)) mustEqual Success(ArrayData(List(BooleanData(true))))
    }

    "provide ObjectData" in {
      JSon(Map("a" -> true)) mustEqual Success(ObjectData(Map("a" -> BooleanData(true))))
    }
  }

  "JSon externalized" should {
    "provide null" in {
      NullData.toRaw mustEqual null
    }

    "provide a string" in {
      StringData("hello").toRaw mustEqual "hello"
    }

    "provide a number" in {
      NumberData(123).toRaw mustEqual 123
    }

    "provide a boolean" in {
      BooleanData(true).toRaw mustEqual true
    }

    "provide a list" in {
      ArrayData(List(BooleanData(true))).toRaw mustEqual List(true)
    }

    "provide a map" in {
      ObjectData(Map("a" -> BooleanData(true))).toRaw mustEqual Map("a" -> true)
    }
  }

  "JSon get value" should {
    "provide the object when path is empty" in {
      (NullData getValue Nil).get mustEqual NullData
    }

    "provide en exception when path is not empty and data is not an object" in {
      (NullData getValue List("a")).isFailure mustEqual true
    }
  }

  "JSOn set value" should {
    "replaces the current value when the path is empty" in {
      (NullData setValue(Nil, StringData("a"))) mustEqual StringData("a")
    }

    "replaces the current value when the path is not empty" in {
      (ObjectData(Map("b" -> NullData)) setValue(List("b"), StringData("a"))) mustEqual ObjectData(Map("b" -> StringData("a")))
    }

    "creates complex objects when the path is not empty" in {
      (ObjectData(Map()) setValue(List("b","c"), StringData("a"))) mustEqual ObjectData(Map("b" -> ObjectData(Map("c" -> StringData("a")))))
    }
  }

  "JSOn overrides" should {
    "replaces the current value by the new one" in {
      (NullData overrides StringData("a")) mustEqual core.NullData
    }

    "replaces the current object data by the new one" in {
      val data1: ObjectData = ObjectData(Map("a" -> NullData))
      val data2: ObjectData = ObjectData(Map("a" -> StringData("a")))
      val result: ObjectData = ObjectData(Map("a" -> NullData))
      (data1 overrides data2) mustEqual result
    }

    "extends the current object data by the new one" in {
      val data1: ObjectData = ObjectData(Map("a" -> NullData))
      val data2: ObjectData = ObjectData(Map("b" -> StringData("a")))
      val result: ObjectData = ObjectData(Map("a" -> NullData, "b" -> StringData("a")))
      (data1 overrides data2) mustEqual result
    }

    "extends the inner current object data by the inner new one" in {
      val data1: ObjectData = ObjectData(Map("i" -> ObjectData(Map("a" -> NullData))))
      val data2: ObjectData = ObjectData(Map("i" -> ObjectData(Map("b" -> StringData("a")))))
      val result: ObjectData = ObjectData(Map("a" -> NullData, "b" -> StringData("a")))
      (data1 overrides data2) mustEqual ObjectData(Map("i" -> result))
    }
  }
}