package smallibs.rapido.syntax

import scala.util.parsing.combinator._
import smallibs.rapido.ast._

object Parser extends JavaTokenParsers {

  //
  // Public behaviors
  //

  def typeSpecification: Parser[Entity] =
    ("type" ~> ident <~ "=") ~ typeDefinition ^^ {
      case n ~ t => TypeEntity(n, t)
    }

  def serviceSpecification: Parser[Entity] =
    ("service" ~> ident <~ "{") ~ (rep(serviceDefinition) <~ "}") ^^ {
      case n ~ l => ServiceEntity(n, Services(l))
    }

  //
  // Internal public behaviors
  //

  def typeDefinition: Parser[Type] =
    (atomic | extensible) ~ ("[" ~ "]").* ^^ {
      case t ~ a => a.foldLeft(t) {
        (r, _) => TypeArray(r)
      }
    }

  def serviceDefinition: Parser[Service] =
    (ident <~ ":") ~ restAction ~ (typeDefinition.? <~ "=>") ~ typeDefinition ~ ("or" ~> typeDefinition).? ^^ {
      case name ~ action ~ in ~ out ~ err => Service(name,action,ServiceType(in,out,err))
    }

  //
  // Private behaviors
  //

  private def restAction: Parser[Operation] =
    ("GET" | "POST" | "PUT" | "DELETE") ^^ {
      case "GET" => GET
      case "POST" => POST
      case "PUT" => PUT
      case "DELETE" => DELETE
    }

  private class Terminal(s: String) {
    def produces(t: Type): Parser[Type] = s ^^ {
      _ => t
    }
  }

  private object Terminal {
    def apply(s: String): Terminal = new Terminal(s)
  }

  private def integer: Parser[Type] =
    Terminal("Number") produces TypeNumber

  private def string: Parser[Type] =
    Terminal("String") produces TypeString

  private def boolean: Parser[Type] =
    Terminal("Boolean") produces TypeBoolean

  private def identified: Parser[Type] =
    ident ^^ {
      s => TypeIdentifier(s)
    }

  private def attribute: Parser[(String, Type)] =
    (ident <~ ":") ~ typeDefinition ^^ {
      case i ~ t => (i, t)
    }

  private def record: Parser[Type] =
    "{" ~> repsep(attribute, ";") <~ "}" ^^ {
      l => TypeObject(l)
    }

  private def atomic: Parser[Type] =
    (integer | string | boolean) ^^ {
      t => t
    }

  private def extensible: Parser[Type] =
    (record | identified) ~ ("with" ~> extensible).* ^^ {
      case t ~ l => l.foldLeft(t) {
        (r, t) => TypeComposed(r, t)
      }
    }
}
