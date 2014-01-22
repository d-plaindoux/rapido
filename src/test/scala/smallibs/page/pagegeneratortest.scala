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

package smallibs.page

import org.specs2.mutable._
import scala.util.Success
import smallibs.page.engine.Engine
import smallibs.page.syntax.PageParser

object EngineGeneratorTest extends Specification {
  "Template and generator should" should {

    "provides a result with an empty" in {
      val template = PageParser.parseAll(PageParser.template, "")
      val engine = Engine(Provider.empty)
      engine.generate(template.get) mustEqual Success(Some(""))
    }

    "provides a result with an input text" in {
      val template = PageParser.parseAll(PageParser.template, "Hello, World")
      val engine = Engine(Provider.empty)
      engine.generate(template.get) mustEqual Success(Some("Hello, World"))
    }

    "provides a result with an input Ident" in {
      val template = PageParser.parseAll(PageParser.template, "@VAL::hello")
      val engine = Engine(Provider.record("hello" -> Provider.constant("World")))
      engine.generate(template.get) mustEqual Success(Some("World"))
    }

    "provides a result with an input sequence" in {
      val template = PageParser.parseAll(PageParser.template, "@VAL::hello, @VAL::world!")
      val engine = Engine(Provider.record(
        "hello" -> Provider.constant("Hello"),
        "world" -> Provider.constant("World")
      ))
      engine.generate(template.get) mustEqual Success(Some("Hello, World!"))
    }

    "provides a result with an anonymous repeatable" in {
      val template = PageParser.parseAll(PageParser.template, "@REP[| - @VAL|]")
      val engine = Engine(Provider.set(
        Provider.constant("Hello"), Provider.constant("World")
      ))
      engine.generate(template.get) mustEqual Success(Some(" - Hello - World"))
    }

    "provides a result with a named repeatable" in {
      val template = PageParser.parseAll(PageParser.template, "@REP::keys[| - @VAL|]")
      val engine = Engine(Provider.record(
        "keys" -> Provider.set(Provider.constant("Hello"), Provider.constant("World"))
      ))
      engine.generate(template.get) mustEqual Success(Some(" - Hello - World"))
    }

    "provides a result with a named complex repeatable" in {
      val template = PageParser.parseAll(PageParser.template, "@REP::keys[| - @VAL::name|]")
      val engine = Engine(Provider.record(
        "keys" ->
          Provider.set(
            Provider.record("name" -> Provider.constant("Hello")),
            Provider.record("name" -> Provider.constant("World"))
          )
      ))
      engine.generate(template.get) mustEqual Success(Some(" - Hello - World"))
    }

    "provides a result using Define and Use" in {
      val template = PageParser.parseAll(PageParser.template, "@MACRO::keys[|@REP::keys[| - @VAL::name|]|]\n@USE::keys")
      val engine = Engine(Provider.record(
        "keys" ->
          Provider.set(
            Provider.record("name" -> Provider.constant("Hello")),
            Provider.record("name" -> Provider.constant("World"))
          )
      ))
      engine.generate(template.get) mustEqual Success(Some(" - Hello - World"))
    }

  }
}