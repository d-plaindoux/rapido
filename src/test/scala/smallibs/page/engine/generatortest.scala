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

package smallibs.page.engine

import org.specs2.mutable._
import scala.util.Success
import smallibs.page._
import smallibs.page.ast._

object EngineTest extends Specification {
  "Generator should" should {

    "provides a result with an empty" in {
      val engine = Engine(Provider.empty)
      engine.generate(NoTemplate) mustEqual Success(Some(""))
    }

    "provides a result with an input text" in {
      val engine = Engine(Provider.empty)
      engine.generate(Text("Hello, World")) mustEqual Success(Some("Hello, World"))
    }

    "provides a result with an input Ident" in {
      val engine = Engine(Provider.record("hello" -> Provider.constant("World")))
      engine.generate(Value(Some("hello"), None)) mustEqual Success(Some("World"))
    }

    "provides a result with an input sequence" in {
      val engine = Engine(Provider.record(
        "hello" -> Provider.constant("Hello"),
        "world" -> Provider.constant("World")
      ))
      engine.generate(
        Sequence(List(Value(Some("hello"), None), Text(", "), Value(Some("world"), None), Text("!")))
      ) mustEqual Success(Some("Hello, World!"))
    }

    "provides a result with an anonymous repeatable" in {
      val engine = Engine(Provider.set(
        Provider.constant("Hello"), Provider.constant("World")
      ))
      engine.generate(
        Repetition(None, None, Some(Sequence(List(Text(" - "), Value(None, None)))))
      ) mustEqual Success(Some(" - Hello - World"))
    }

    "provides a result with a named repeatable" in {
      val engine = Engine(Provider.record(
        "keys" -> Provider.set(Provider.constant("Hello"), Provider.constant("World"))
      ))
      engine.generate(
        Repetition(Some("keys"), None, Some(Sequence(List(Text(" - "), Value(None, None)))))
      ) mustEqual Success(Some(" - Hello - World"))
    }

    "provides a result with a named complex repeatable" in {
      val engine = Engine(Provider.record(
        "keys" ->
          Provider.set(
            Provider.record("name" -> Provider.constant("Hello")),
            Provider.record("name" -> Provider.constant("World"))
          )))
      engine.generate(
        Repetition(Some("keys"), None, Some(Sequence(List(Text(" - "), Value(Some("name"), None)))))
      ) mustEqual Success(Some(" - Hello - World"))
    }

    "provides a result with a named alternative" in {
      val engine = Engine(
        Provider.set(
          Provider.record("name" -> Provider.constant("Hello")),
          Provider.record("key" -> Provider.constant("World"))
        ))
      engine.generate(
        Repetition(None, None, Some(Alternate(None, List(Value(Some("name"), None), Text("...")))))
      ) mustEqual Success(Some("Hello..."))
    }

  }
}