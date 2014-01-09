package smallibs.page.engine

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
      val engine = Engine(Provider.record("hello" -> Provider.constant("World")))
      engine.generate(Value(Some("hello"), None)) mustEqual Success("World")
    }

    "provides a result with an input sequence" in {
      val engine = Engine(Provider.record(
        "hello" -> Provider.constant("Hello"),
        "world" -> Provider.constant("World")
      ))
      engine.generate(
        Sequence(List(Value(Some("hello"),None), Text(", "), Value(Some("world"), None), Text("!")))
      ) mustEqual Success("Hello, World!")
    }

    "provides a result with an anonymous repeatable" in {
      val engine = Engine(Provider.set(
        Provider.constant("Hello"), Provider.constant("World")
      ))
      engine.generate(
        Repetition(None, Some(Sequence(List(Text(" - "), Value(None, None)))))
      ) mustEqual Success(" - Hello - World")
    }

    "provides a result with a named repeatable" in {
      val engine = Engine(Provider.record(
        "keys" -> Provider.set(Provider.constant("Hello"), Provider.constant("World"))
      ))
      engine.generate(
        Repetition(Some("keys"), Some(Sequence(List(Text(" - "), Value(None, None)))))
      ) mustEqual Success(" - Hello - World")
    }

    "provides a result with a named complex repeatable" in {
      val engine = Engine(Provider.record(
        "keys" ->
          Provider.set(
            Provider.record("name" -> Provider.constant("Hello")),
            Provider.record("name" -> Provider.constant("World"))
          )))
      engine.generate(
        Repetition(Some("keys"), Some(Sequence(List(Text(" - "), Value(Some("name"), None)))))
      ) mustEqual Success(" - Hello - World")
    }
  }
}