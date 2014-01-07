package smallibs.page.syntax

import scala.util.matching.Regex
import scala.util.parsing.combinator.JavaTokenParsers
import smallibs.page.ast._

object PageParser extends JavaTokenParsers {

  //
  // Public behaviors
  //

  override def skipWhitespace: Boolean = false

  def aTemplate: Parser[Template] =
    (aText | aValue | aRepetition).* ^^ {
      simplify
    }

  //
  // Private behaviors
  //

  private def anInnerTemplate: Parser[Template] =
    rep(anInnerText | aValue | aRepetition) ^^ {
      simplify
    }

  private def aText: Parser[Template] =
    regex(new Regex("[^@]+")) ^^ {
      Text
    }

  private def anInnerText: Parser[Template] =
    regex(new Regex("[^@\\]]+")) ^^ {
      Text
    }

  private def aValue: Parser[Template] =
    "@VAL" ~> ("::" ~> ident).? ^^ {
      Value
    }

  private def aRepetition: Parser[Template] =
    ("@REP" ~> ("::" ~> ident).?) ~ ("[" ~> anInnerTemplate <~ "]") ^^ {
      case s ~ t => Repetition(s, t)
    }

  private def simplify: Function[List[Template], Template] = {
    case Nil => NoTemplate // Simplification
    case List(t) => t // Simplification
    case l => Sequence(l)
  }
}