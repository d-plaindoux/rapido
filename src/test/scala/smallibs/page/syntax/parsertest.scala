package smallibs.page.syntax

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
      parsed.get mustEqual Value(Some("name"), None)
    }

    "provides a value ~ value" in {
      val parsed = PageParser.parseAll(PageParser.template, "@VAL::name@VAL::value")
      parsed.get mustEqual Sequence(List(Value(Some("name"), None), Value(Some("value"), None)))
    }

    "provides an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP::name[||]")
      parsed.get mustEqual Repetition(Some("name"), None, Some(NoTemplate))
    }

    "provides a repeatable with a text" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP::name[|Hello, World!|]")
      parsed.get mustEqual Repetition(Some("name"), None, Some(Text("Hello, World!")))
    }

    "provides a repeatable with an ident" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP::name[|@VAL::name|]")
      parsed.get mustEqual Repetition(Some("name"), None, Some(Value(Some("name"), None)))
    }

    "provides a repeatable with an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP::name[|@REP::value[||]|]")
      parsed.get mustEqual Repetition(Some("name"), None, Some(Repetition(Some("value"), None, Some(NoTemplate))))
    }

    "provides an anonymous empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP[||]")
      parsed.get mustEqual Repetition(None, None, Some(NoTemplate))
    }

    "provides a anonymous repeatable with a text" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP[|Hello, World!|]")
      parsed.get mustEqual Repetition(None, None, Some(Text("Hello, World!")))
    }

    "provides a anonymous repeatable with an ident" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP[|@VAL::name|]")
      parsed.get mustEqual Repetition(None, None, Some(Value(Some("name"), None)))
    }

    "provides a anonymous repeatable with an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP[|@REP::value[||]|]")
      parsed.get mustEqual Repetition(None, None, Some(Repetition(Some("value"), None, Some(NoTemplate))))
    }

    "provides a anonymous repeatable with an implicit repeatable" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP[|@REP::value|]")
      parsed.get mustEqual Repetition(None, None, Some(Repetition(Some("value"), None, None)))
    }

    "provides a anonymous repeatable with an implicit repeatable and a separator" in {
      val parsed = PageParser.parseAll(PageParser.template, "@REP(.)[|@REP(,)::value|]")
      parsed.get mustEqual Repetition(None, Some("."), Some(Repetition(Some("value"), Some(","), None)))
    }

    "provides an anonymous empty alternate" in {
      val parsed = PageParser.parseAll(PageParser.template, "@OR[||]")
      parsed.get mustEqual Alternate(None, List(NoTemplate))
    }

    "provides an anonymous empty alternate with one choice" in {
      val parsed = PageParser.parseAll(PageParser.template, "@OR[|1|][|2|]")
      parsed.get mustEqual Alternate(None, List(Text("1"), Text("2")))
    }

    "provides an anonymous empty alternate" in {
      val parsed = PageParser.parseAll(PageParser.template, "@OR::name[||]")
      parsed.get mustEqual Alternate(Some("name"), List(NoTemplate))
    }

    "provides an anonymous empty alternate with one choice" in {
      val parsed = PageParser.parseAll(PageParser.template, "@OR::name[|1|][|2|]")
      parsed.get mustEqual Alternate(Some("name"), List(Text("1"), Text("2")))
    }

  }
}