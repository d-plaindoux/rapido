package smallibs.rapido.syntax

import org.specs2.mutable._
import smallibs.rapido.ast._

object ArithmeticSpec extends Specification {
  "Parser" should {
    "provide a TypeNumber" in {
      val parsed = Parser.parseAll(Parser.integer, "Number")
      parsed.get mustEqual TypeNumber
    }
    "provide a TypeString" in {
      val parsed = Parser.parseAll(Parser.string, "String")
      parsed.get mustEqual TypeString
    }
    "provide a TypeBoolean" in {
      val parsed = Parser.parseAll(Parser.boolean, "Boolean")
      parsed.get mustEqual TypeBoolean
    }
    "provide a attribute" in {
      val parsed  = Parser.parseAll(Parser.attribute, "valid : Boolean")
      parsed.get mustEqual ("valid", TypeBoolean)
    }
    "provide an empty data object" in {
      val parsed  = Parser.parseAll(Parser.dataObject, "{}")
      parsed.get mustEqual TypeObject(Map())
    }
    "provide a data object with one attribute" in {
      val parsed  = Parser.parseAll(Parser.dataObject, "{ valid : Boolean }")
      parsed.get mustEqual TypeObject(Map("valid" -> TypeBoolean))
    }
    "provide a data object with two attributes" in {
      val parsed  = Parser.parseAll(Parser.dataObject, "{ valid : Boolean ; content : {} }")
      val expected: TypeObject = TypeObject(Map("valid" -> TypeBoolean, "content" -> TypeObject(Map())))
      parsed.get mustEqual expected
    }
  }
}