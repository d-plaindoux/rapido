package smallibs.rapido.syntax

import org.specs2.mutable._
import smallibs.rapido.ast._

object RapidoSpec extends Specification {
  "Parser dedicated to Types" should {

    "provides a number type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.number, "int")
      parsed.get mustEqual TypeNumber
    }

    "provides a string type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.string, "string")
      parsed.get mustEqual TypeString
    }

    "provides a boolean type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.boolean, "bool")
      parsed.get mustEqual TypeBoolean
    }

    "provides an identified type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.identified, "Action")
      parsed.get mustEqual TypeIdentifier("Action")
    }

    "provides an empty data object type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{}")
      parsed.get mustEqual TypeObject(Map())
    }

    "provides a data object type with one attribute" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{ valid : bool }")
      parsed.get mustEqual TypeObject(Map("valid" ->(None, TypeBoolean)))
    }

    "provides a data object type with one quoted attribute" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{ 'valid' : bool }")
      parsed.get mustEqual TypeObject(Map("valid" ->(None, TypeBoolean)))
    }

    "provides a data object type with one string quoted attribute" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{ \"valid\" : bool }")
      parsed.get mustEqual TypeObject(Map("valid" ->(None, TypeBoolean)))
    }

    "provides a data object type with one attribute and a get" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{ @get valid : bool }")
      parsed.get mustEqual TypeObject(Map("valid" ->(Some(GetAccess(None)), TypeBoolean)))
    }

    "provides a data object type with one attribute and a named get" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{ @get(Valid) valid : bool }")
      parsed.get mustEqual TypeObject(Map("valid" ->(Some(GetAccess(Some("Valid"))), TypeBoolean)))
    }

    "provides a data object type with one attribute and a set" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{ @set valid : bool }")
      parsed.get mustEqual TypeObject(Map("valid" ->(Some(SetAccess(None)), TypeBoolean)))
    }

    "provides a data object type with one attribute and a named set" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{ @set(Valid) valid : bool }")
      parsed.get mustEqual TypeObject(Map("valid" ->(Some(SetAccess(Some("Valid"))), TypeBoolean)))
    }

    "provides a data object type with one attribute and a set,get" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{ @{set,get} valid : bool }")
      parsed.get mustEqual TypeObject(Map("valid" ->(Some(SetGetAccess(None)), TypeBoolean)))
    }

    "provides a data object type with one attribute and a named set,get" in {
      val parsed = RapidoParser.parseAll(RapidoParser.extensible, "{ @{get,set}(Valid) valid : bool }")
      parsed.get mustEqual TypeObject(Map("valid" ->(Some(SetGetAccess(Some("Valid"))), TypeBoolean)))
    }

    "provides a data object type with two attributes" in {
      val parsed = RapidoParser.parseAll(RapidoParser.typeDefinition, "{ valid : bool ; content : {} }")
      val expected: TypeObject = TypeObject(Map("valid" ->(None, TypeBoolean), "content" ->(None, TypeObject(Map()))))
      parsed.get mustEqual expected
    }

    "provides an empty data object type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.typeDefinition, "{}")
      parsed.get mustEqual TypeObject(Map())
    }

    "provides a number optional type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.typeDefinition, "int?")
      parsed.get mustEqual TypeOptional(TypeNumber)
    }

    "provides a number optional of multiple type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.typeDefinition, "int*?")
      parsed.get mustEqual TypeOptional(TypeMultiple(TypeNumber))
    }

    "provides a number multiple type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.typeDefinition, "int*")
      parsed.get mustEqual TypeMultiple(TypeNumber)
    }

    "provides a number multiple of multiple type" in {
      val parsed = RapidoParser.parseAll(RapidoParser.typeDefinition, "int**")
      parsed.get mustEqual TypeMultiple(TypeMultiple(TypeNumber))
    }
    "provides a type composition" in {
      val parsed = RapidoParser.parseAll(RapidoParser.typeDefinition, "Action with {} ")
      parsed.get mustEqual TypeComposed(TypeIdentifier("Action"), TypeObject(Map()))
    }

    "provides a type composition with three components" in {
      val parsed = RapidoParser.parseAll(RapidoParser.typeDefinition, "Listener with Action with {}")
      parsed.get mustEqual TypeComposed(TypeIdentifier("Listener"), TypeComposed(TypeIdentifier("Action"), TypeObject(Map())))
    }

  }

  "Parser dedicated to attributes" should {
    "provides a quoted attribute name" in {
      val parsed = RapidoParser.parseAll(RapidoParser.attribute, "an_int:Int")
      parsed.get mustEqual("an_int", (None, TypeIdentifier("Int")))
    }

    "provides a simple attribute" in {
      val parsed = RapidoParser.parseAll(RapidoParser.attribute, "'X-Auth-Token':Int")
      parsed.get mustEqual("X-Auth-Token", (None, TypeIdentifier("Int")))
    }

    "provides a string-quoted attribute name" in {
      val parsed = RapidoParser.parseAll(RapidoParser.attribute, "\"X-Auth-Token\":Int")
      parsed.get mustEqual("X-Auth-Token", (None, TypeIdentifier("Int")))
    }
  }

  "Parser dedicated to Services" should {

    "provides a services with one definition with no parameter but no error" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceDefinition, "list : => Action* = GET")
      parsed.get mustEqual Service("list", Action(GET, None, None, None, None, None), ServiceType(None, TypeMultiple(TypeIdentifier("Action")), None))
    }

    "provides a services with one definition with a parameter but no error" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceDefinition, "list : Param => Action* = GET")
      parsed.get mustEqual Service("list", Action(GET, None, None, None, None, None), ServiceType(Some(TypeIdentifier("Param")), TypeMultiple(TypeIdentifier("Action")), None))
    }

    "provides a services with one definition with no parameter but an error" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceDefinition, "list : => Action* or Error = GET")
      parsed.get mustEqual Service("list", Action(GET, None, None, None, None, None), ServiceType(None, TypeMultiple(TypeIdentifier("Action")), Some(TypeIdentifier("Error"))))
    }

    "provides a services with one definition with no parameter but an error" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceDefinition, "list : Param => Action* or Error = GET")
      parsed.get mustEqual Service("list", Action(GET, None, None, None, None, None), ServiceType(Some(TypeIdentifier("Param")), TypeMultiple(TypeIdentifier("Action")), Some(TypeIdentifier("Error"))))
    }

    "provides a services with one definition with extended url for the action" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceDefinition, "list : => Action* = GET[list]")
      parsed.get mustEqual Service("list", Action(GET, Some(Path(List(StaticLevel("list")))), None, None, None, None), ServiceType(None, TypeMultiple(TypeIdentifier("Action")), None))
    }

    "provides a services with one definition with extended url for the action with parameters" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceDefinition, "list : => Action* = GET PARAMS[Object]")
      parsed.get mustEqual Service("list", Action(GET, None, Some(TypeIdentifier("Object")), None, None, None), ServiceType(None, TypeMultiple(TypeIdentifier("Action")), None))
    }

    "provides a services with one definition with extended url for the action with body" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceDefinition, "list : => Action* = GET BODY[Object]")
      parsed.get mustEqual Service("list", Action(GET, None, None, Some(TypeIdentifier("Object")), None, None), ServiceType(None, TypeMultiple(TypeIdentifier("Action")), None))
    }

    "provides a services with one definition with extended url for the action with header" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceDefinition, "list : => Action* = GET HEADER[Object]")
      parsed.get mustEqual Service("list", Action(GET, None, None, None, Some(TypeIdentifier("Object")), None), ServiceType(None, TypeMultiple(TypeIdentifier("Action")), None))
    }

    "provides a services with one definition with extended url for the action with return" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceDefinition, "list : => Action* = GET RETURN[Object]")
      parsed.get mustEqual Service("list", Action(GET, None, None, None, None, Some(TypeIdentifier("Object"))), ServiceType(None, TypeMultiple(TypeIdentifier("Action")), None))
    }
  }

  "Parser dedicated to Path" should {

    "provides an empty path" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[]")
      parsed.get must throwA[RuntimeException]
    }

    "provides a simple static path" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[toto]")
      parsed.get mustEqual Path(List(StaticLevel("toto")))
    }

    "provides a simple static path even when starting with /" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[/toto]")
      parsed.get mustEqual Path(List(StaticLevel("/toto")))
    }

    "provides a simple static path even when ending with /" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[toto/]")
      parsed.get mustEqual Path(List(StaticLevel("toto/")))
    }

    "provides a simple static path even when starting and ending with /" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[/toto/]")
      parsed.get mustEqual Path(List(StaticLevel("/toto/")))
    }

    "provides a simple static path starting with an open tag" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[<toto]")
      parsed.get must throwA[RuntimeException]
    }

    "provides a complex static path" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[toto/titi]")
      parsed.get mustEqual Path(List(StaticLevel("toto/titi")))
    }

    "provides a complex static path with multiple / (involutive)" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[toto///titi]")
      parsed.get mustEqual Path(List(StaticLevel("toto///titi")))
    }

    "provides a simple variable path" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[<p>]")
      parsed.get mustEqual Path(List(DynamicLevel(List("p"))))
    }

    "provides a simple variable chained path" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[<p.address.zip>]")
      parsed.get mustEqual Path(List(DynamicLevel(List("p", "address", "zip"))))
    }

    "provides a complex path" in {
      val parsed = RapidoParser.parseAll(RapidoParser.path, "[addresses/<p.address.zip>/who/<h.name>]")
      parsed.get mustEqual Path(List(StaticLevel("addresses/"),
        DynamicLevel(List("p", "address", "zip")),
        StaticLevel("/who/"),
        DynamicLevel(List("h", "name")
        )))
    }

  }

  "Parser dedicated to entities" should {

    "provides a type definition" in {
      val parsed = RapidoParser.parseAll(RapidoParser.typeSpecification, "type Action = { performed : bool }")
      parsed.get mustEqual TypeEntity("Action", TypeObject(Map("performed" ->(None, TypeBoolean))))
    }

    "provides a service definition" in {
      val parsed = RapidoParser.parseAll(RapidoParser.serviceSpecification, "service Test [places] { list : => Action = GET add : Param => Action = POST }")
      parsed.get mustEqual ServiceEntity("Test", Route("Test", Nil, Path(List(StaticLevel("places")))),
        List(Service("list", Action(GET, None, None, None, None, None), ServiceType(None, TypeIdentifier("Action"), None)),
          Service("add", Action(POST, None, None, None, None, None), ServiceType(Some(TypeIdentifier("Param")), TypeIdentifier("Action"), None))))
    }

    "provides a client definition" in {
      val parsed = RapidoParser.parseAll(RapidoParser.clientSpecification, "client foo provides bar,baz")
      parsed.get mustEqual ClientEntity("foo", List("bar", "baz"))
    }

    "provides a type definition specification" in {
      val parsed = RapidoParser.parseAll(RapidoParser.specification, "type Action = { performed : bool }")
      parsed.get mustEqual TypeEntity("Action", TypeObject(Map("performed" ->(None, TypeBoolean))))
    }

    "provides a service definition specification" in {
      val parsed = RapidoParser.parseAll(RapidoParser.specification, "service Test [places] { list : => Action = GET add : Param => Action = POST }")
      parsed.get mustEqual ServiceEntity("Test", Route("Test", Nil, Path(List(StaticLevel("places")))),
        List(Service("list", Action(GET, None, None, None, None, None), ServiceType(None, TypeIdentifier("Action"), None)),
          Service("add", Action(POST, None, None, None, None, None), ServiceType(Some(TypeIdentifier("Param")), TypeIdentifier("Action"), None))))
    }

    "provides a client definition specification" in {
      val parsed = RapidoParser.parseAll(RapidoParser.specification, "client foo provides bar,baz")
      parsed.get mustEqual ClientEntity("foo", List("bar", "baz"))
    }


  }
}