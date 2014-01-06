package smallibs.page.engine

//import org.specs2.mutable._
import org.specs2.mutable._
import scala.util.Success
import smallibs.page._
import smallibs.page.ast._

object EngineTest extends Specification {
  "Generator should" should {

    "provides a result with an empty" in {
      val engine = Engine(Provider.empty)
      engine.generate(NoTemplate) mustEqual Success("")
    }

    "provides a result with an input text" in {
      val engine = Engine(Provider.empty)
      engine.generate(Text("Hello, World")) mustEqual Success("Hello, World")
    }

    "provides a result with an input Ident" in {
      val engine = Engine(Provider.map(Map("hello" -> Provider.constant("World"))))
      engine.generate(Value(Some("hello"))) mustEqual Success("World")
    }

    "provides a result with an input sequence" in {
      val engine = Engine(Provider.map(Map(
        "hello" -> Provider.constant("Hello"),
        "world" -> Provider.constant("World")
      )))
      engine.generate(
        Sequence(List(Value(Some("hello")), Text(", "), Value(Some("world")), Text("!")))
      ) mustEqual Success("Hello, World!")
    }

    "provides a result with an anonymous repeatable" in {
      val engine = Engine(Provider.map(Map(
        "0" -> Provider.constant("Hello"),
        "1" -> Provider.constant("World"))
      ))
      engine.generate(
        Repetition(None, Sequence(List(Text(" - "), Value(None))))
      ) mustEqual Success(" - Hello - World")
    }

    "provides a result with a named repeatable" in {
      val engine = Engine(Provider.map(Map(
        "keys" -> Provider.list(Provider.constant("Hello"), Provider.constant("World"))
      )))
      engine.generate(
        Repetition(Some("keys"), Sequence(List(Text(" - "), Value(None))))
      ) mustEqual Success(" - Hello - World")
    }

    "provides a result with a named complex repeatable" in {
      val engine = Engine(Provider.map(Map(
        "keys" ->
          Provider.map(Map(
            "0" -> Provider.map(Map("name" -> Provider.constant("Hello"))),
            "1" -> Provider.map(Map("name" -> Provider.constant("World")))))
      )))
      engine.generate(
        Repetition(Some("keys"), Sequence(List(Text(" - "), Value(Some("name")))))
      ) mustEqual Success(" - Hello - World")
    }
  }
}