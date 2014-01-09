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
    (text | value | repetition | special).* ^^ {
      simplify
    }

  //
  // Private behaviors
  //

  private def innerTemplate: Parser[Template] =
    rep(innerText | value | repetition) ^^ {
      simplify
    }

  private def text: Parser[Template] =
    regex(new Regex("[^@]+")) ^^ {
      Text
    }

  private def innerText: Parser[Template] =
    regex(new Regex("[^@|]+")) ^^ {
      Text
    }

  private def value: Parser[Template] =
    "@VAL" ~> ("::" ~> ident).? ~ ("[|" ~> innerTemplate <~ "|]").? ^^ {
      case s ~ v => Value(s, v)
    }

  private def repetition: Parser[Template] =
    ("@REP" ~> ("::" ~> ident).?) ~ ("[|" ~> innerTemplate <~ "|]").? ^^ {
      case s ~ t => Repetition(s, t)
    }

  private def special: Parser[Template] =
    ("@" | "|") ^^ {
      Text
    }

  private def simplify: Function[List[Template], Template] = {
    case Nil => NoTemplate // Simplification
    case List(t) => t // Simplification
    case l => Sequence(l)
  }
}