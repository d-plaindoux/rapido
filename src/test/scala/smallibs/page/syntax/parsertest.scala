package smallibs.page.syntax


//import org.specs2.mutable._
import org.specs2.mutable._
import smallibs.page.ast._

object PageSpec extends Specification {
  "Parser" should {

    "provides an empty" in {
      val parsed = PageParser.parseAll(PageParser.template, "")
      parsed.get mustEqual NoTemplate
    }

    "provides a text" in {
      val parsed = PageParser.parseAll(PageParser.template, "Hello, World!")
      parsed.get mustEqual Text("Hello, World!")
    }

    "provides an ident" in {
      val parsed = PageParser.parseAll(PageParser.template, "@VAL::name")
      parsed.get mustEqual Value(Some("name"),None)
    }

    "provides a value ~ value" in {
      val parsed = PageParser.parseAll(PageParser.template, "@VAL::name@VAL::value")
      parsed.get mustEqual Sequence(List(Value(Some("name"),None), Value(Some("value"),None)))
    }

    "provides an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP::name[]")
      parsed.get mustEqual Repetition(Some("name"), NoTemplate)
    }

    "provides a repeatable with a text" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP::name[Hello, World!]")
      parsed.get mustEqual Repetition(Some("name"), Text("Hello, World!"))
    }

    "provides a repeatable with an ident" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP::name[@VAL::name]")
      parsed.get mustEqual Repetition(Some("name"), Value(Some("name"),None))
    }

    "provides a repeatable with an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP::name[@REP::value[]]")
      parsed.get mustEqual Repetition(Some("name"), Repetition(Some("value"), NoTemplate))
    }

    "provides an anonymous empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP[]")
      parsed.get mustEqual Repetition(None, NoTemplate)
    }

    "provides a anonymous repeatable with a text" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP[Hello, World!]")
      parsed.get mustEqual Repetition(None, Text("Hello, World!"))
    }

    "provides a anonymous repeatable with an ident" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP[@VAL::name]")
      parsed.get mustEqual Repetition(None, Value(Some("name"),None))
    }

    "provides a anonymous repeatable with an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP[@REP::value[]]")
      parsed.get mustEqual Repetition(None, Repetition(Some("value"), NoTemplate))
    }
  }
}