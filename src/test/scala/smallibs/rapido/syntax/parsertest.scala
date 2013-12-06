package smallibs.rapido.syntax

import org.specs2.mutable._
//import org.specs2.mutable._

import smallibs.rapido.ast._

object RapidoSpec extends Specification {
  "Parser dedicated to Types" should {
    "provides a number type" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "Number")
      parsed.get mustEqual TypeNumber
    }
    "provides a string type" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "String")
      parsed.get mustEqual TypeString
    }
    "provides a boolean type" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "Boolean")
      parsed.get mustEqual TypeBoolean
    }
    "provides an identified type" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "Action")
      parsed.get mustEqual TypeIdentifier("Action")
    }
    "provides an empty data object type" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "{}")
      parsed.get mustEqual TypeObject(Nil)
    }
    "provides a data object type with one attribute" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "{ valid : Boolean }")
      parsed.get mustEqual TypeObject(List("valid" -> TypeBoolean))
    }
    "provides a data object type with two attributes" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "{ valid : Boolean ; content : {} }")
      val expected: TypeObject = TypeObject(List("valid" -> TypeBoolean, "content" -> TypeObject(Nil)))
      parsed.get mustEqual expected
    }
    "provides an empty data object type" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "{}")
      parsed.get mustEqual TypeObject(Nil)
    }
    "provides a number array type" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "Number[]")
      parsed.get mustEqual TypeArray(TypeNumber)
    }
    "provides a number array of array type" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "Number[][]")
      parsed.get mustEqual TypeArray(TypeArray(TypeNumber))
    }
    "provides a type composition" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "Action with {} ")
      parsed.get mustEqual TypeComposed(TypeIdentifier("Action"), TypeObject(Nil))
    }
    "provides a type composition with three components" in {
      val parsed = Parser.parseAll(Parser.typeDefinition, "Listener with Action with {}")
      parsed.get mustEqual TypeComposed(TypeIdentifier("Listener"), TypeComposed(TypeIdentifier("Action"), TypeObject(Nil)))
    }
  }

  "Parser dedicated to Services" should {
    "provides a services definition" in {
      val parsed = Parser.parseAll(Parser.serviceDefinition, "list : GET => Action[]")
      parsed.get mustEqual Service("list", GET, ServiceType(None, TypeArray(TypeIdentifier("Action")), None))
    }
  }

  "Parser dedicated to entities" should {
    "provides a type definition" in {
      val parsed = Parser.parseAll(Parser.typeSpecification, "type Action = { performed : Boolean }")
      parsed.get mustEqual TypeEntity("Action", TypeObject(List("performed" -> TypeBoolean)))
    }
    "provides a service definition" in {
      val parsed = Parser.parseAll(Parser.serviceSpecification, "service Test { list : GET => Action }")
      parsed.get mustEqual ServiceEntity("Test", Services(List(Service("list", GET, ServiceType(None, TypeIdentifier("Action"), None)))))
    }
  }
}