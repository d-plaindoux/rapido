package smallibs.rapido.syntax

import scala.util.parsing.combinator._
import smallibs.rapido.ast._

object Parser extends JavaTokenParsers {

  class Terminal(s: String) {
    def produces(t: Type): Parser[Type] = s ^^ (_ => t)
  }

  object Terminal {
    def apply(s: String): Terminal = new Terminal(s)
  }

  def integerType: Parser[Type] = Terminal("Number") produces TypeNumber
  def stringType: Parser[Type] = Terminal("String") produces TypeString
  def booleanType: Parser[Type] = Terminal("Boolean") produces TypeBoolean

}