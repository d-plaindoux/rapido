package smallibs.page.engine

import scala.Some
import scala.annotation.tailrec
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import smallibs.page.DataProvider
import smallibs.page.ast._

class Engine(path: List[String], data: DataProvider) {

  def generate(template: Template): Try[String] =
    template match {
      case NoTemplate => Success("")
      case Text(t) => Success(t)
      case Value(None, None) =>
        Success(data.toString)
      case Value(None, Some(newTemplate)) =>
        generate(newTemplate)
      case Value(Some(name), value) =>
        data get name match {
          case None =>
            Failure(new NoSuchElementException(path.reverse + ": " + name + " (" + data.getClass.getSimpleName + ")"))
          case Some(newData) => new Engine(name :: path, newData).generate(Value(None, value))
        }
      case Sequence(seq) =>
        @tailrec
        def generate_list(result: String, l: List[Template]): Try[String] =
          l match {
            case Nil => Success(result)
            case e :: nl =>
              generate(e) match {
                case f@Failure(_) => f
                case Success(s) =>
                  generate_list(result + s, nl)
              }
          }
        generate_list("", seq)
      case Repetition(None, None) =>
        data.values.foldLeft[Try[String]](Success("")) {
          (result: Try[String], data: DataProvider) =>
            for (v <- result;
                 c <- new Engine(path, data).generate(Value(None, None)))
            yield v + c
        }
      case Repetition(None, Some(content)) =>
        data.values.foldLeft[Try[String]](Success("")) {
          (result: Try[String], data: DataProvider) =>
            for (v <- result;
                 c <- new Engine(path, data).generate(content))
            yield v + c
        }
      case Repetition(Some(name), content) =>
        data get name match {
          case None => Failure(new NoSuchElementException(data + ": " + name))
          case Some(newData) => new Engine(name :: path, newData).generate(Repetition(None, content))
        }
    }

}

object Engine {
  def apply(bean: DataProvider): Engine = new Engine(Nil, bean)
}