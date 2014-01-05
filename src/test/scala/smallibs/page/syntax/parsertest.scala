package smallibs.page.syntax


//import org.specs2.mutable._

import org.specs2.mutable._
import smallibs.page.ast._

object PageSpec extends Specification {
  "Parser" should {

    "provides an empty" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "")
      parsed.get mustEqual Empty
    }

    "provides a text" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "Hello, World!")
      parsed.get mustEqual Text("Hello, World!")
    }

    "provides an ident" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@ident:name")
      parsed.get mustEqual AnIdent("name")
    }

    "provides a string" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@string:name")
      parsed.get mustEqual AString("name")
    }

    "provides a string ~ ident" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@string:name@ident:value")
      parsed.get mustEqual Sequence(List(AString("name"), AnIdent("value")))
    }

    "provides an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@rep:name[]")
      parsed.get mustEqual ARepetition("name", Empty)
    }

    "provides a repeatable with a text" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@rep:name[Hello, World!]")
      parsed.get mustEqual ARepetition("name", Text("Hello, World!"))
    }

    "provides a repeatable with an ident" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@rep:name[@ident:name]")
      parsed.get mustEqual ARepetition("name", AnIdent("name"))
    }

    "provides a repeatable with an empty repeatable" in {
      val parsed = PageParser.parseAll(PageParser.aTemplate, "@rep:name[@rep:value[]]")
      parsed.get mustEqual ARepetition("name", ARepetition("value", Empty))
    }
  }
}