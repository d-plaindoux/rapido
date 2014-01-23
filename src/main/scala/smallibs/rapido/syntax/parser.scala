/*
 * Copyright (C)2014 D. Plaindoux.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package smallibs.rapido.syntax

import scala.util.matching.Regex
import scala.util.parsing.combinator.JavaTokenParsers
import smallibs.rapido.ast._

object RapidoParser extends JavaTokenParsers {

  //
  // Public behaviors
  //

  def specifications: Parser[List[Entity]] =
    specification.*

  def specification: Parser[Entity] =
    typeSpecification | serviceSpecification | clientSpecification

  def typeSpecification: Parser[Entity] =
    ("type" ~> ident <~ "=") ~! typeDefinition ^^ {
      case n ~ t => TypeEntity(n, t)
    }

  def serviceSpecification: Parser[Entity] =
    ("service" ~> ident) ~ ("(" ~> repsep(typeDefinition, ",") <~ ")").? ~! path ~ ("{" ~> serviceDefinition.* <~ "}") ^^ {
      case n ~ None ~ r ~ l => ServiceEntity(n, Route(n, Nil, r), l)
      case n ~ Some(p) ~ r ~ l => ServiceEntity(n, Route(n, p, r), l)
    }

  def clientSpecification: Parser[Entity] =
    ("client" ~> ident) ~! ("provides" ~> repsep(ident, ",")) ^^ {
      case n ~ l => ClientEntity(n, l)
    }

  //
  // Internal behaviors
  //

  def routeParameter: Parser[(String, Type)] =
    (ident <~ ":") ~! typeDefinition ^^ {
      case n ~ t => (n, t)
    }

  def typeDefinition: Parser[Type] =
    (atomic | extensible) ~ ("*" | "?").* ^^ {
      case t ~ a => a.foldLeft(t) {
        case (r, "*") => TypeMultiple(r)
        case (r, "?") => TypeOptional(r)
      }
    }

  private def directive(name: String): Parser[Type] =
    name ~> "[" ~> typeDefinition <~ "]"

  def serviceDefinition: Parser[Service] =
    (ident <~ ":") ~! (repsep(typeDefinition,",") <~ "=>") ~ typeDefinition ~ ("or" ~> typeDefinition).? ~ ("=" ~> restAction) ~
      path.? ~ directive("HEADER").? ~ directive("PARAMS").? ~ directive("BODY").? ~ directive("RETURN").? ^^ {
      case name ~ in ~ out ~ err ~ action ~ path ~ header ~ param ~ body ~ result =>
        Service(name, Action(action, path, param, body, header, result), ServiceType(in, out, err))
    }

  def restAction: Parser[Operation] =
    ("GET" | "POST" | "PUT" | "DELETE" | ident) ^^ {
      case "GET" => GET
      case "HEAD" => HEAD
      case "POST" => POST
      case "PUT" => PUT
      case "DELETE" => DELETE
      case name => UserDefine(name)
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
    Terminal("int") produces TypeNumber

  def string: Parser[Type] =
    Terminal("string") produces TypeString

  def boolean: Parser[Type] =
    Terminal("bool") produces TypeBoolean

  def identified: Parser[Type] =
    ident ^^ {
      TypeIdentifier
    }

  def attributeName: Parser[String] =
    ident | ("\"" ~> regex(new Regex("[^\"]+")) <~ "\"") | ("'" ~> regex(new Regex("[^\']+")) <~ "'")

  def getterSetter: Parser[Option[String] => Access] =
    "@get" ^^ {
      _ => GetAccess
    } | "@set" ^^ {
      _ => SetAccess
    } | ("@" ~ "{" ~ (("set" ~ "," ~ "get") | ("get" ~ "," ~ "set")) ~ "}") ^^ {
      _ => SetGetAccess
    }

  def attribute: Parser[(String, TypeAttribute)] =
    ((getterSetter ~ ("(" ~> ident <~ ")").?).? ~ attributeName <~ ":") ~! typeDefinition ^^ {
      case None ~ i ~ t => (i, ConcreteTypeAttribute(None, t))
      case Some(g ~ n) ~ i ~ t => (i, ConcreteTypeAttribute(Some(g(n)), t))
    }

  def virtual: Parser[(String, VirtualTypeAttribute)] =
    ("virtual" ~> attributeName) ~ ("=" ~> path) ^^ {
      case i ~ p => (i, VirtualTypeAttribute(p))
    }

  def record: Parser[Type] =
    "{" ~> repsep(virtual | attribute, ";" | ",") <~ "}" ^^ {
      l => TypeObject(l.toMap)
    }

  def atomic: Parser[Type] =
    (number | string | boolean) ^^ {
      t => t
    }

  def extensible: Parser[Type] =
    (record | identified) ~ ("with" ~> extensible).* ^^ {
      case t ~ l => l.foldLeft(t) {
        TypeComposed
      }
    }

  def path: Parser[Path] =
    "[" ~> (variableEntry | staticEntry).+ <~ "]" ^^ {
      case l => Path(for (e <- l if e != StaticLevel("")) yield e)
    }

  def staticEntry: Parser[PathEntry] =
    regex(new Regex("[^<\\]]+")) ^^ {
      StaticLevel
    }

  def variableEntry: Parser[PathEntry] =
    "<" ~> repsep(regex(new Regex("[^.>]+")), ".") <~ ">" ^^ {
      DynamicLevel
    }

  protected override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r
}
