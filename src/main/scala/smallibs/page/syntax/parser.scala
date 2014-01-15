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
    define | use | value | repetition | alternate | optional

  private def text: Parser[Template] =
    regex(new Regex("[^@]+")) ^^ {
      Text
    }

  private def innerText: Parser[Template] =
    regex(new Regex("[^@|]+")) ^^ {
      Text
    }

  private def define: Parser[Template] =
    ("@DEFINE" ~> "::" ~> ident) ~ (spaces ~> "[|" ~> innerTemplate <~ "|]") ^^ {
      case n ~ v => Define(n, v)
    }

  private def use: Parser[Template] =
    "@USE" ~> "::" ~> ident ^^ {
      case n => Use(n)
    }

  private def value: Parser[Template] =
    ("@VAL" ~> ("::" ~> ident).?) ~ (spaces ~> "[|" ~> innerTemplate <~ "|]").? ^^ {
      case s ~ v => Value(s, v)
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