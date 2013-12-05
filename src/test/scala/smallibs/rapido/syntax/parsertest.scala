package smallibs.rapido.syntax

import org.specs2.mutable._
import smallibs.rapido.ast._

object ArithmeticSpec extends Specification {
  "Parser" should {
    "provide a TypeNumber" in {
      val parsed: Parser.ParseResult[Type] = Parser.parseAll(Parser.integerType, "Number")
      parsed.get mustEqual TypeNumber
    }
    "provide a TypeString" in {
      val parsed: Parser.ParseResult[Type] = Parser.parseAll(Parser.stringType, "String")
      parsed.get mustEqual TypeString
    }
    "provide a TypeBoolean" in {
      val parsed: Parser.ParseResult[Type] = Parser.parseAll(Parser.booleanType, "Boolean")
      parsed.get mustEqual TypeBoolean
    }
  }
}