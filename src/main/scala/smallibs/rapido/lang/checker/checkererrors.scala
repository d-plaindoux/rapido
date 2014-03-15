package smallibs.rapido.lang.checker

import scala.util.parsing.input.Position
import smallibs.rapido.lang.ast.{Path, Type}

// ---------------------------------------------------------------------------------------------------------------------
// Error categories
// ---------------------------------------------------------------------------------------------------------------------

sealed trait CheckerError

case class TypeConflicts(position: Position, name: String, positions: List[Position]) extends CheckerError

case class TypeUndefined(position: Position, undefined: List[String]) extends CheckerError

case class PathesError(position: Position, undefined: List[Path]) extends CheckerError

case class SubTypeError(position: Position, receiver: Type, value: Type) extends CheckerError

// ---------------------------------------------------------------------------------------------------------------------
// Class able to receive positioned errors
// ---------------------------------------------------------------------------------------------------------------------

class ErrorNotifier(errors: List[CheckerError]) {

  def hasError: Boolean = !errors.isEmpty

  def getErrors: List[CheckerError] = this.errors

  def onError[B](default: List[CheckerError] => Unit): Unit =
    if (hasError) default(errors)

  def findWith(checker: ErrorNotifier => ErrorNotifier): ErrorNotifier =
    checker(this)

  def locate(position: Position): ErrorAtPositionNotifier =
    new ErrorAtPositionNotifier(position, errors)

  def ++(notifier: ErrorNotifier): ErrorNotifier =
    new ErrorNotifier(this.errors ++ notifier.getErrors)
}

class ErrorAtPositionNotifier(position: Position, errors: List[CheckerError]) {

  def conflict(name: String, positions: List[Position]): ErrorAtPositionNotifier =
    new ErrorAtPositionNotifier(position, errors :+ TypeConflicts(position, name, positions))

  def undefined(undefined: List[String]): ErrorAtPositionNotifier =
    undefined match {
      case Nil => this
      case notEmpty => new ErrorAtPositionNotifier(position, errors :+ TypeUndefined(position, notEmpty))
    }

  def path(path: Option[Path]): ErrorAtPositionNotifier =
    path match {
      case None => this
      case Some(p) => new ErrorAtPositionNotifier(position, errors :+ PathesError(position, List(p)))
    }

  def pathes(path: List[Path]): ErrorAtPositionNotifier =
    path match {
      case Nil => this
      case p => new ErrorAtPositionNotifier(position, errors :+ PathesError(position, p))
    }

  def subtype(subtype: Option[(Type, Type)]): ErrorAtPositionNotifier =
    subtype match {
      case None => this
      case Some((r, v)) => new ErrorAtPositionNotifier(position, errors :+ SubTypeError(position, r, v))
    }

  def unlocate: ErrorNotifier =
    new ErrorNotifier(errors)

}

object ErrorNotifier {
  def apply(): ErrorNotifier =
    new ErrorNotifier(Nil)
}