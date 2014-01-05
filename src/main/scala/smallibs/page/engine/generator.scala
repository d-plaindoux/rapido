package smallibs.page.engine

import scala.Some
import scala.annotation.tailrec
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import smallibs.page.DataProvider
import smallibs.page.ast._

class Engine(bean: DataProvider) {

  def generate(template: Template): Try[String] =
    template match {
      case Empty => Success("")
      case Text(t) => Success(t)
      case AnIdent(name) =>
        bean get name match {
          case None => Failure(new NoSuchElementException(name))
          case Some(value) => Success(value.toString)
        }
      case AString(name) =>
        bean get name match {
          case None => Failure(new NoSuchElementException(name))
          case Some(value) => Success('"' + value.toString + '"')
        }
      case Sequence(seq) => {
        @tailrec
        def generate_list(result: String, l: List[Template]): Try[String] =
          l match {
            case Nil => Success(result)
            case e :: l =>
              generate(e) match {
                case f@Failure(_) => f
                case Success(s) =>
                  generate_list(result + s, l)
              }
          }
        generate_list("", seq)
      }

    }

  def engine(path: List[String]): Try[Engine] = {
    @tailrec
    def engine_with_bean(path: List[String], bean: DataProvider): Try[Engine] =
      path match {
        case Nil =>
          Success(Engine(bean))
        case name :: path =>
          bean get name match {
            case None =>
              Failure(new NoSuchElementException(name))
            case Some(bean) =>
              engine_with_bean(path, bean)
          }
      }
    engine_with_bean(path, bean)
  }

}

object Engine {
  def apply(bean: DataProvider): Engine = new Engine(bean)
}