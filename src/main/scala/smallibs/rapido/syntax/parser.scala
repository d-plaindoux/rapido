package smallibs.rapido.syntax

import scala.util.parsing.combinator._
import smallibs.rapido.ast._

object Parser extends JavaTokenParsers {

  class Terminal(s: String) {
    def produces(t: Type): Parser[Type] = s ^^ {
      _ => t
    }
  }

  object Terminal {
    def apply(s: String): Terminal = new Terminal(s)
  }

  def integer: Parser[Type] =
    Terminal("Number") produces TypeNumber

  def string: Parser[Type] =
    Terminal("String") produces TypeString

  def boolean: Parser[Type] =
    Terminal("Boolean") produces TypeBoolean

  def attribute: Parser[(String, Type)] =
    ((ident <~ ":") ~ (integer | string | boolean | dataObject)) ^^ {
      case i ~ t => (i, t)
    }

  def dataObject: Parser[Type] =
    "{" ~> repsep(attribute, ";") <~ "}" ^^ {
      l => TypeObject(l.toMap)
    }

}