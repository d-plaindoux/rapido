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
}