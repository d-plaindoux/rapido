package smallibs.page

import org.specs2.mutable._
import scala.util.Success
import smallibs.page.engine.Engine
import smallibs.page.syntax.PageParser

object EngineTest extends Specification {
  "Template and generator should" should {

    "provides a result with an empty" in {
      val template = PageParser.parseAll(PageParser.template, "")
      val engine = Engine(Provider.empty)
      engine.generate(template.get) mustEqual Success("")
    }

    "provides a result with an input text" in {
      val template = PageParser.parseAll(PageParser.template, "Hello, World")
      val engine = Engine(Provider.empty)
      engine.generate(template.get) mustEqual Success("Hello, World")
    }

    "provides a result with an input Ident" in {
      val template = PageParser.parseAll(PageParser.template, "@VAL::hello")
      val engine = Engine(Provider.record("hello" -> Provider.constant("World")))
      engine.generate(template.get) mustEqual Success("World")
    }

    "provides a result with an input sequence" in {
      val template = PageParser.parseAll(PageParser.template, "@VAL::hello, @VAL::world!")
      val engine = Engine(Provider.record(
        "hello" -> Provider.constant("Hello"),
        "world" -> Provider.constant("World")
      ))
      engine.generate(template.get) mustEqual Success("Hello, World!")
    }

    "provides a result with an anonymous repeatable" in {
      val template = PageParser.parseAll(PageParser.template, "@REP[| - @VAL|]")
      val engine = Engine(Provider.set(
        Provider.constant("Hello"), Provider.constant("World")
      ))
      engine.generate(template.get) mustEqual Success(" - Hello - World")
    }

    "provides a result with a named repeatable" in {
      val template = PageParser.parseAll(PageParser.template, "@REP::keys[| - @VAL|]")
      val engine = Engine(Provider.record(
        "keys" -> Provider.set(Provider.constant("Hello"), Provider.constant("World"))
      ))
      engine.generate(template.get) mustEqual Success(" - Hello - World")
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
      engine.generate(template.get) mustEqual Success(" - Hello - World")
    }

  }
}