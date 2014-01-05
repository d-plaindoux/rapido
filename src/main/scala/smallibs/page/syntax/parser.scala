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
    rep(aText | anIdent | aString | aRepetition) ^^ {
      simplify
    }

  //
  // Private behaviors
  //

  private def anInnerTemplate: Parser[Template] =
    rep(anInnerText | anIdent | aString | aRepetition) ^^ {
      simplify
    }

  private def aText: Parser[Template] =
    regex(new Regex("[^@]+")) ^^ {
      s => Text(s)
    }

  private def anInnerText: Parser[Template] =
    regex(new Regex("[^@\\]]+")) ^^ {
      s => Text(s)
    }

  private def anIdent: Parser[Template] =
    "@ident:" ~> ident ^^ {
      s => AnIdent(s)
    }

  private def aString: Parser[Template] =
    "@string:" ~> ident ^^ {
      s => AString(s)
    }

  private def aRepetition: Parser[Template] =
    ("@rep:" ~> ident) ~ ("[" ~> anInnerTemplate <~ "]") ^^ {
      case s ~ t => ARepetition(s, t)
    }

  private def simplify: Function[List[Template], Template] = {
    case Nil => Empty // Simplification
    case List(t) => t // Simplification
    case l => Sequence(l)
  }
}