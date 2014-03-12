package smallibs.rapido.lang.checker

import scala.util.parsing.input.Position
import smallibs.rapido.lang.ast.Type

// ---------------------------------------------------------------------------------------------------------------------
// Error categories
// ---------------------------------------------------------------------------------------------------------------------

sealed trait CheckerError

case class TypeConflicts(position: Position, name: String, positions: List[Position]) extends CheckerError

case class TypeUndefined(position: Position, undefined: List[String]) extends CheckerError

case class SubTypeError(position: Position, receiver: Type, value: Type) extends CheckerError

// ---------------------------------------------------------------------------------------------------------------------
// Class able to receive positioned errors
// ---------------------------------------------------------------------------------------------------------------------

class ErrorNotifier(errors: List[CheckerError]) {

  def finish: List[CheckerError] = errors

  def hasError: Boolean = !errors.isEmpty

  def findWith(checker: ErrorNotifier => ErrorNotifier): ErrorNotifier = checker(this)

  def atPosition(position: Position): ErrorAtPositionNotifier =
    new ErrorAtPositionNotifier(position, errors)
}

class ErrorAtPositionNotifier(position: Position, errors: List[CheckerError]) {

  def conflict(name: String, positions: List[Position]): ErrorAtPositionNotifier =
    new ErrorAtPositionNotifier(position, errors :+ TypeConflicts(position, name, positions))

  def undefined(undefined: List[String]): ErrorAtPositionNotifier =
    undefined match {
      case Nil => this
      case notEmpty => new ErrorAtPositionNotifier(position, errors :+ TypeUndefined(position, notEmpty))
    }


  def subtype(subtype: Option[(Type, Type)]): ErrorAtPositionNotifier =
    subtype match {
      case None => this
      case Some((r, v)) => new ErrorAtPositionNotifier(position, errors :+ SubTypeError(position, r, v))
    }

  def terminate: ErrorNotifier =
    new ErrorNotifier(errors)

}

object ErrorNotifier {
  def apply(): ErrorNotifier =
    new ErrorNotifier(Nil)
}