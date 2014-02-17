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

package smallibs.rapido.lang.syntax

import scala.util.matching.Regex
import scala.util.parsing.combinator.{PackratParsers, JavaTokenParsers}
import smallibs.rapido.lang.ast._

object RapidoParser extends JavaTokenParsers with PackratParsers {

  //
  // Public behaviors
  //

  def specifications: PackratParser[List[Entity]] =
    specification.*

  def specification: PackratParser[Entity] =
    positioned(typeSpecification | serviceSpecification | clientSpecification)

  def typeSpecification: PackratParser[Entity] =
    ("type" ~> ident <~ "=") ~! extensible ^^ {
      case n ~ t => TypeEntity(n, t)
    }

  def serviceSpecification: PackratParser[Entity] =
    ("service" ~> ident) ~ ("(" ~> repsep(identified, ",") <~ ")").? ~! path ~ ("{" ~> serviceDefinition.* <~ "}") ^^ {
      case n ~ None ~ r ~ l => ServiceEntity(n, Route(n, Nil, r), l)
      case n ~ Some(p) ~ r ~ l => ServiceEntity(n, Route(n, p, r), l)
    }

  def clientSpecification: PackratParser[Entity] =
    ("client" ~> ident) ~! ("provides" ~> repsep(ident, ",")) ^^ {
      case n ~ l => ClientEntity(n, l)
    }

  //
  // Internal behaviors
  //

  def routeParameter: PackratParser[(String, Type)] =
    (ident <~ ":") ~! typeDefinition ^^ {
      case n ~ t => (n, t)
    }

  def typeDefinition: PackratParser[Type] =
    (atomic | extensible) ~ ("*" | "?").? ^^ {
      case t ~ None => t
      case t ~ Some("*") => TypeMultiple(t)
      case t ~ Some("?") => TypeOptional(t)
    }

  private def directive(name: String): PackratParser[TypeRecord] =
    name ~> "[" ~> identified <~ "]"

  def serviceDefinition: PackratParser[Service] =
    (ident <~ ":") ~! (repsep(identified, ",") <~ "=>") ~ identified ~ ("or" ~> identified).? ~ ("=" ~> restAction) ~
      path.? ~ directive("HEADER").? ~ directive("PARAMS").? ~ directive("BODY").? ^^ {
      case name ~ in ~ out ~ err ~ action ~ path ~ header ~ param ~ body =>
        Service(name, Action(action, path, param, body, header), ServiceType(in, out, err))
    }

  def restAction: PackratParser[Operation] =
    ("GET" | "POST" | "PUT" | "DELETE" | ident) ^^ {
      case "GET" => GET
      case "HEAD" => HEAD
      case "POST" => POST
      case "PUT" => PUT
      case "DELETE" => DELETE
      case name => UserDefine(name)
    }

  class Terminal(s: String) {
    def produces(t: Type): PackratParser[Type] = s ^^ {
      _ => t
    }
  }

  object Terminal {
    def apply(s: String): Terminal = new Terminal(s)
  }

  def number: PackratParser[Type] =
    Terminal("int") produces TypeNumber

  def string: PackratParser[Type] =
    Terminal("string") produces TypeString

  def boolean: PackratParser[Type] =
    Terminal("bool") produces TypeBoolean

  def identified: PackratParser[TypeRecord] =
    ident ^^ {
      TypeIdentifier
    }

  def attributeName: PackratParser[String] =
    ident | ("\"" ~> regex(new Regex("[^\"]+")) <~ "\"") | ("'" ~> regex(new Regex("[^\']+")) <~ "'")

  def getterSetter: PackratParser[Option[String] => Access] =
    "@" ~ "get" ^^ {
      _ => GetAccess
    } | "@" ~ "set" ^^ {
      _ => SetAccess
    } | ("@" ~ "{" ~ (("set" ~ "," ~ "get") | ("get" ~ "," ~ "set")) ~ "}") ^^ {
      _ => SetGetAccess
    }

  def attribute: PackratParser[(String, TypeAttribute)] =
    ((getterSetter ~ ("(" ~> ident <~ ")").?).? ~ attributeName <~ ":") ~! typeDefinition ^^ {
      case None ~ i ~ t => (i, ConcreteTypeAttribute(None, t))
      case Some(g ~ n) ~ i ~ t => (i, ConcreteTypeAttribute(Some(g(n)), t))
    }

  def virtual: PackratParser[(String, VirtualTypeAttribute)] =
    ("virtual" ~> attributeName) ~ ("=" ~> path) ^^ {
      case i ~ p => (i, VirtualTypeAttribute(p))
    }

  def record: PackratParser[TypeRecord] =
    "{" ~> repsep(virtual | attribute, ";" | ",") <~ "}" ^^ {
      l => TypeObject(l.toMap)
    }

  def atomic: PackratParser[Type] =
    number | string | boolean

  def extensible: PackratParser[TypeRecord] =
    (record | identified) ~ ("with" ~> extensible).* ^^ {
      case t ~ l => l.foldLeft(t) {
        TypeComposed
      }
    }

  def path: PackratParser[Path] =
    "[" ~> (variableEntry | staticEntry).+ <~ "]" ^^ {
      case l => Path(for (e <- l if e != StaticLevel("")) yield e)
    }

  def staticEntry: PackratParser[PathEntry] =
    regex(new Regex("[^<\\]]+")) ^^ {
      StaticLevel
    }

  def variableEntry: PackratParser[PathEntry] =
    "<" ~> repsep(regex(new Regex("[^.>]+")), ".") <~ ">" ^^ {
      DynamicLevel
    }

  protected override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r
}
