package smallibs.page.syntax


//import org.specs2.mutable._
import org.specs2.mutable._
import smallibs.page.ast._

object PageSpec extends Specification {
  "Parser" should {

    "provides an empty" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "")
      parsed.get mustEqual NoTemplate
    }

    "provides a text" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "Hello, World!")
      parsed.get mustEqual Text("Hello, World!")
    }

    "provides an ident" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@VAL::name")
      parsed.get mustEqual Value(Some("name"))
    }

    "provides a value ~ value" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@VAL::name@VAL::value")
      parsed.get mustEqual Sequence(List(Value(Some("name")), Value(Some("value"))))
    }

    "provides an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@REP::name[]")
      parsed.get mustEqual Repetition(Some("name"), NoTemplate)
    }

    "provides a repeatable with a text" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@REP::name[Hello, World!]")
      parsed.get mustEqual Repetition(Some("name"), Text("Hello, World!"))
    }

    "provides a repeatable with an ident" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@REP::name[@VAL::name]")
      parsed.get mustEqual Repetition(Some("name"), Value(Some("name")))
    }

    "provides a repeatable with an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@REP::name[@REP::value[]]")
      parsed.get mustEqual Repetition(Some("name"), Repetition(Some("value"), NoTemplate))
    }

    "provides an anonymous empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@REP[]")
      parsed.get mustEqual Repetition(None, NoTemplate)
    }

    "provides a anonymous repeatable with a text" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@REP[Hello, World!]")
      parsed.get mustEqual Repetition(None, Text("Hello, World!"))
    }

    "provides a anonymous repeatable with an ident" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@REP[@VAL::name]")
      parsed.get mustEqual Repetition(None, Value(Some("name")))
    }

    "provides a anonymous repeatable with an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@REP[@REP::value[]]")
      parsed.get mustEqual Repetition(None, Repetition(Some("value"), NoTemplate))
    }
  }
}