package smallibs.rapido.syntax

import org.specs2.mutable._
import smallibs.rapido.ast._

object RapidoSpec extends Specification {
  "Parser" should {
    "provides a number type" in {
      val parsed = Parser.parseAll(Parser.integer, "Number")
      parsed.get mustEqual TypeNumber
    }
    "provides a string type" in {
      val parsed = Parser.parseAll(Parser.string, "String")
      parsed.get mustEqual TypeString
    }
    "provides a boolean type" in {
      val parsed = Parser.parseAll(Parser.boolean, "Boolean")
      parsed.get mustEqual TypeBoolean
    }
    "provides an attribute type" in {
      val parsed  = Parser.parseAll(Parser.attribute, "valid : Boolean")
      parsed.get mustEqual ("valid", TypeBoolean)
    }
    "provides an empty data object type" in {
      val parsed  = Parser.parseAll(Parser.dataObject, "{}")
      parsed.get mustEqual TypeObject(Map())
    }
    "provides a data object type with one attribute" in {
      val parsed  = Parser.parseAll(Parser.dataObject, "{ valid : Boolean }")
      parsed.get mustEqual TypeObject(Map("valid" -> TypeBoolean))
    }
    "provides a data object type with two attributes" in {
      val parsed  = Parser.parseAll(Parser.dataObject, "{ valid : Boolean ; content : {} }")
      val expected: TypeObject = TypeObject(Map("valid" -> TypeBoolean, "content" -> TypeObject(Map())))
      parsed.get mustEqual expected
    }
  }
}