package smallibs.page.engine

import scala.Some
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import smallibs.page.DataProvider
import smallibs.page.ast._

class Engine(path: List[String], data: DataProvider) {

  def generate(template: Template): Try[Option[String]] =
    template match {
      case NoTemplate => Success(Some(""))
      case Text(t) => Success(Some(t))
      case Value(None, None) => Success(Some(data.toString))
      case Value(None, Some(newTemplate)) => generate(newTemplate)
      case Value(Some(name), value) => data get name match {
        case None => Failure(new NoSuchElementException(path.reverse + ": " + name))
        case Some(newData) => new Engine(name :: path, newData).generate(Value(None, value))
      }
      case Sequence(seq) => generate_list("", seq)
      case Repetition(None, sep, content) => generate_repetition(sep, content.getOrElse(Value(None, None)))
      case Repetition(Some(name), sep, content) => data get name match {
        case None => Failure(new NoSuchElementException(data + ": " + name))
        case Some(newData) => new Engine(name :: path, newData).generate(Repetition(None, sep, content))
      }
      case Optional(None, None) => generate(Value(None, None))
      case Optional(None, Some(template)) => Success(generate(template).getOrElse(None))
      case Optional(Some(name), template) => data get name match {
        case None => Failure(new NoSuchElementException(path.reverse + ": " + name))
        case Some(newData) => new Engine(name :: path, newData).generate(Optional(None, template))
      }
      case Alternate(None, l) => generate_alternate(l)
      case Alternate(Some(name), l) => data get name match {
        case None => Failure(new NoSuchElementException(path.reverse + ": " + name))
        case Some(newData) => new Engine(name :: path, newData).generate_alternate(l)
      }
    }

  def generate_list(result: String, l: List[Template]): Try[Option[String]] =
    l match {
      case Nil => Success(Some(result))
      case e :: nl =>
        generate(e) match {
          case f@Failure(_) => f
          case Success(s) => generate_list(result + s.getOrElse(""), nl)
        }
    }

  def generate_repetition(sep: Option[String], template: Template): Try[Option[String]] = {
    def generate_from_list(values: List[DataProvider]): List[String] =
      values match {
        case Nil => Nil
        case data :: values =>
          new Engine(path, data).generate(template) match {
            case Success(None) => generate_from_list(values)
            case Success(Some(e)) => e :: generate_from_list(values)
            case Failure(f) => throw f
          }
      }

    try {
      Success(Some(generate_from_list(data.values).mkString(sep.getOrElse(""))))
    } catch {
      case e: Throwable => Failure(e)
    }
  }

  def generate_alternate(l: List[Template]): Try[Option[String]] =
    l match {
      case Nil => throw new IllegalAccessException
      case e :: l => generate(e) match {
        case Failure(_) => generate_alternate(l)
        case success => success
      }
    }
}

object Engine {
  def apply(bean: DataProvider): Engine = new Engine(Nil, bean)
}