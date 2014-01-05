package smallibs.rapido.syntax

import scala.util.matching.Regex
import scala.util.parsing.combinator.JavaTokenParsers
import smallibs.rapido.ast._

object RapidoParser extends JavaTokenParsers {

  //
  // Public behaviors
  //

  def specification: Parser[Entity] =
    (typeSpecification
      | serviceSpecification
      | serviceDefinition
      | routeSpecification
      | clientSpecification) ^^ {
      case t: Entity => t
    }

  def typeSpecification: Parser[Entity] =
    ("type" ~> ident <~ "=") ~ typeDefinition ^^ {
      case n ~ t => TypeEntity(n, t)
    }

  def serviceSpecification: Parser[Entity] =
    ("service" ~> ident <~ "{") ~ (rep(serviceDefinition) <~ "}") ^^ {
      case n ~ l => ServiceEntity(n, l)
    }

  def routeSpecification: Parser[Entity] =
    ("route" ~> ident) ~ ("(" ~> repsep(routeParameter, ",") <~ ")").? ~ path ^^ {
      case name ~ None ~ path => RouteEntity(name, Nil, path)
      case name ~ Some(params) ~ path => RouteEntity(name, params, path)
    }

  def clientSpecification: Parser[Entity] =
    ("client" ~> ident) ~ ("provides" ~> repsep(ident, ",")) ^^ {
      case n ~ l => ClientEntity(n, l)
    }

  //
  // Internal behaviors
  //

  def routeParameter: Parser[(String, Type)] =
    (ident <~ ":") ~ typeDefinition ^^ {
      case n ~ t => (n, t)
    }

  def typeDefinition: Parser[Type] =
    (atomic | extensible) ~ ("[" ~ "]").* ^^ {
      case t ~ a => a.foldLeft(t) {
        (r, _) => TypeArray(r)
      }
    }

  def serviceDefinition: Parser[Service] =
    (ident <~ ":") ~ restAction ~ (typeDefinition.? <~ "=>") ~ typeDefinition ~ ("or" ~> typeDefinition).? ^^ {
      case name ~ action ~ in ~ out ~ err => Service(name, action, ServiceType(in, out, err))
    }

  def restAction: Parser[Operation] =
    ("GET" | "POST" | "PUT" | "DELETE") ^^ {
      case "GET" => GET
      case "POST" => POST
      case "PUT" => PUT
      case "DELETE" => DELETE
    }

  class Terminal(s: String) {
    def produces(t: Type): Parser[Type] = s ^^ {
      _ => t
    }
  }

  object Terminal {
    def apply(s: String): Terminal = new Terminal(s)
  }

  def number: Parser[Type] =
    Terminal("Number") produces TypeNumber

  def string: Parser[Type] =
    Terminal("String") produces TypeString

  def boolean: Parser[Type] =
    Terminal("Boolean") produces TypeBoolean

  def identified: Parser[Type] =
    ident ^^ {
      s => TypeIdentifier(s)
    }

  def attribute: Parser[(String, Type)] =
    (ident <~ ":") ~ typeDefinition ^^ {
      case i ~ t => (i, t)
    }

  def record: Parser[Type] =
    "{" ~> repsep(attribute, ";") <~ "}" ^^ {
      l => TypeObject(l)
    }

  def atomic: Parser[Type] =
    (number | string | boolean) ^^ {
      t => t
    }

  def extensible: Parser[Type] =
    (record | identified) ~ ("with" ~> extensible).* ^^ {
      case t ~ l => l.foldLeft(t) {
        (r, t) => TypeComposed(r, t)
      }
    }

  def path: Parser[Path] =
    "[" ~> repsep(variableEntry | staticEntry, "/") <~ "]" ^^ {
      l => Path(for (e <- l if e != StaticLevel("")) yield e)
    }

  def staticEntry: Parser[PathEntry] =
    regex(new Regex("[^/\\]]*")) ^^ {
      s => StaticLevel(s)
    }

  def variableEntry: Parser[PathEntry] =
    "<" ~> repsep(ident, ".") <~ ">" ^^ {
      s => DynamicLevel(s)
    }

}
