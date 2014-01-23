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

package smallibs.page.syntax

import scala.util.matching.Regex
import scala.util.parsing.combinator.JavaTokenParsers
import smallibs.page.ast._

object PageParser extends JavaTokenParsers {

  //
  // Public behaviors
  //

  override def skipWhitespace: Boolean = false

  def template: Parser[Template] =
    (text | commonTemplate | special).* ^^ {
      simplify
    }

  //
  // Private behaviors
  //

  private def innerTemplate: Parser[Template] =
    (innerText | commonTemplate | innerSpecial).* ^^ {
      simplify
    }

  private def commonTemplate: Parser[Template] =
    comment | macroDefinition | define | use | value | repetition | alternate | optional

  private def comment: Parser[Template] =
    spaces ~ "@[|" ~ regex(new Regex("[^|]*")) ~ "|]" ~ spaces ^^ {
      _ => NoTemplate
    }

  private def text: Parser[Template] =
    regex(new Regex("[^@]+")) ^^ {
      Text
    }

  private def innerText: Parser[Template] =
    regex(new Regex("[^@|]+")) ^^ {
      Text
    }

  private def macroDefinition: Parser[Template] =
    ("@MACRO" ~> "::" ~> ident) ~ (spaces ~> "[|" ~> innerTemplate <~ "|]") <~ spaces ^^ {
      case n ~ v => Macro(n, v)
    }

  private def define: Parser[Template] =
    ("@SET" ~> "::" ~> ident) ~ (spaces ~> "[|" ~> innerTemplate <~ "|]") <~ spaces ^^ {
      case n ~ v => Define(n, v)
    }

  private def use: Parser[Template] =
    "@USE" ~> "::" ~> ident ^^ {
      case n => Use(n)
    }

  private def value: Parser[Template] =
    ("@VAL" ~> ("::" ~> ident).*) ~ (spaces ~> "[|" ~> innerTemplate <~ "|]").? ^^ {
      case Nil ~ t => Value(None, t)
      case List(e) ~ v => Value(Some(e), v)
      case l ~ v => l.foldRight(Value(None, v)) {
        (l, r) => Value(Some(l), Some(r))
      }
    }

  private def repetition: Parser[Template] =
    ("@REP" ~> ("(" ~> regex(new Regex("[^)]+")) <~ ")").? ~ ("::" ~> ident).?) ~ (spaces ~> "[|" ~> innerTemplate <~ "|]").? ^^ {
      case s ~ v ~ t => Repetition(v, s, t)
    }

  private def optional: Parser[Template] =
    ("@OPT" ~> ("::" ~> ident).?) ~ (spaces ~> "[|" ~> innerTemplate <~ "|]").? ^^ {
      case v ~ t => Optional(v, t)
    }

  private def alternate: Parser[Template] =
    ("@OR" ~> ("::" ~> ident).?) ~ (spaces ~> "[|" ~> innerTemplate <~ "|]").+ ^^ {
      case s ~ t => Alternate(s, t)
    }

  private def special: Parser[Template] =
    "@" ^^ {
      Text
    }

  private def innerSpecial: Parser[Template] =
    "@" ^^ {
      Text
    }

  private def spaces: Parser[Unit] =
    regex(new Regex("\\s*")) ^^ {
      _ => ()
    }

  private def simplify: Function[List[Template], Template] = {
    case Nil => NoTemplate // Simplification
    case List(t) => t // Simplification
    case l => Sequence(l)
  }
}