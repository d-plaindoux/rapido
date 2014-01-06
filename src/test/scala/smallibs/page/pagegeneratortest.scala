package smallibs.page

//import org.specs2.mutable._
import org.specs2.mutable._
import scala.util.Success
import smallibs.page.engine.Engine
import smallibs.page.syntax.PageParser

object EngineTest extends Specification {
  "Template and generator should" should {

    "provides a result with an empty" in {
      val template = PageParser.parseAll(PageParser.aTemplate, "")
      val engine = Engine(Provider.empty)
      engine.generate(template.get) mustEqual Success("")
    }

    "provides a result with an input text" in {
      val template = PageParser.parseAll(PageParser.aTemplate, "Hello, World")
      val engine = Engine(Provider.empty)
      engine.generate(template.get) mustEqual Success("Hello, World")
    }

    "provides a result with an input Ident" in {
      val template = PageParser.parseAll(PageParser.aTemplate, "@value:hello")
      val engine = Engine(Provider.map("hello" -> Provider.constant("World")))
      engine.generate(template.get) mustEqual Success("World")
    }

    "provides a result with an input sequence" in {
      val template = PageParser.parseAll(PageParser.aTemplate, "@value:hello, @value:world!")
      val engine = Engine(Provider.map(
        "hello" -> Provider.constant("Hello"),
        "world" -> Provider.constant("World")
      ))
      engine.generate(template.get) mustEqual Success("Hello, World!")
    }

    "provides a result with an anonymous repeatable" in {
      val template = PageParser.parseAll(PageParser.aTemplate, "@rep[ - @value]")
      val engine = Engine(Provider.map(
        "0" -> Provider.constant("Hello"),
        "1" -> Provider.constant("World")
      ))
      engine.generate(template.get) mustEqual Success(" - Hello - World")
    }

    "provides a result with a named repeatable" in {
      val template = PageParser.parseAll(PageParser.aTemplate, "@rep:keys[ - @value]")
      val engine = Engine(Provider.map(
        "keys" ->
          Provider.map(
            "0" -> Provider.constant("Hello"),
            "1" -> Provider.constant("World")
          )
      ))
      engine.generate(template.get) mustEqual Success(" - Hello - World")
    }

    "provides a result with a named complex repeatable" in {
      val template = PageParser.parseAll(PageParser.aTemplate, "@rep:keys[ - @value:name]")
      val engine = Engine(Provider.map(
        "keys" ->
          Provider.map(
            "0" -> Provider.map("name" -> Provider.constant("Hello")),
            "1" -> Provider.map("name" -> Provider.constant("World"))
          )
      ))
      engine.generate(template.get) mustEqual Success(" - Hello - World")
    }

  }
}