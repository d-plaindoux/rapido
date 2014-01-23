/*
 * Copyright (C)2014 D. Plaindoux.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package smallibs.page.engine

import scala.Some
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import smallibs.page.DataProvider
import smallibs.page.ast._

class Engine(path: List[String], data: DataProvider, definitions: Map[String, Template]) {

  type Definitions = Map[String, Template]

  def generate(template: Template): Try[Option[String]] =
    generateWithDefinitions(template) match {
      case f@Failure(e) => Failure(e)
      case Success((s, _)) => Success(s)
    }

  def generateWithDefinitions(template: Template): Try[(Option[String], Definitions)] = {
    template match {
      case NoTemplate =>
        Success(Some(""), definitions)
      case Text(t) =>
        Success(Some(t), definitions)
      case Value(None, None) =>
        Success(Some(data.toString), definitions)
      case Value(None, Some(newTemplate)) =>
        generateWithDefinitions(newTemplate)
      case Value(Some(name), value) => data get name match {
        case None =>
          Failure(new NoSuchElementException(path.reverse + ": " + name))
        case Some(newData) =>
          new Engine(name :: path, newData, definitions).generateWithDefinitions(Value(None, value))
      }
      case Sequence(seq) =>
        generateWithDefinitions_list("", seq) map {
          case (r, _) => (r, definitions)
        }
      case Repetition(None, sep, content) =>
        generateWithDefinitions_repetition(sep, content.getOrElse(Value(None, None)))
      case Repetition(Some(name), sep, content) => data get name match {
        case None =>
          Failure(new NoSuchElementException(data + ": " + name))
        case Some(newData) =>
          new Engine(name :: path, newData, definitions).generateWithDefinitions(Repetition(None, sep, content))
      }
      case Optional(None, None) =>
        generateWithDefinitions(Value(None, None))
      case Optional(None, Some(template)) =>
        generateWithDefinitions(template) match {
          case f@Failure(_) =>
            Success(None, definitions)
          case success =>
            success
        }
      case Optional(Some(name), template) => data get name match {
        case None =>
          Failure(new NoSuchElementException(path.reverse + ": " + name))
        case Some(newData) =>
          new Engine(name :: path, newData, definitions).generateWithDefinitions(Optional(None, template))
      }
      case Alternate(None, l) => generateWithDefinitions_alternate(l)
      case Alternate(Some(name), l) => data get name match {
        case None =>
          Failure(new NoSuchElementException(path.reverse + ": " + name))
        case Some(newData) =>
          new Engine(name :: path, newData, definitions).generateWithDefinitions_alternate(l)
      }
      case Macro(name, t) =>
        Success(None, definitions ++ Map(name -> t))
      case Define(name, t) =>
        generateWithDefinitions(t) map {
          case (None, _) =>
            (None, definitions)
          case (Some(e), _) => (None, definitions ++ Map(name -> Text(e)))
        }
      case Use(name) =>
        definitions get name match {
          case None =>
            Failure(new NoSuchElementException(path.reverse + ": " + name))
          case Some(t) =>
            generateWithDefinitions(t)
        }
    }
  }

  def generateWithDefinitions_list(result: String, l: List[Template]): Try[(Option[String], Definitions)] =
    l match {
      case Nil => Success(Some(result), definitions)
      case e :: nl =>
        generateWithDefinitions(e) match {
          case f@Failure(_) =>
            f
          case Success((s, d)) =>
            new Engine(path, data, d).generateWithDefinitions_list(result + s.getOrElse(""), nl)
        }
    }

  def generateWithDefinitions_repetition(sep: Option[String], template: Template): Try[(Option[String], Definitions)] = {
    def generateWithDefinitions_from_list(values: List[DataProvider], definitions: Definitions): List[String] =
      values match {
        case Nil => Nil
        case data :: values =>
          new Engine(path, data, definitions).generateWithDefinitions(template) match {
            case Success((None, d)) =>
              generateWithDefinitions_from_list(values, d)
            case Success((Some(e), d)) =>
              e :: generateWithDefinitions_from_list(values, d)
            case Failure(f) =>
              throw f
          }
      }

    try {
      Success(Some(generateWithDefinitions_from_list(data.values, definitions).mkString(sep.getOrElse(""))), definitions)
    } catch {
      case e: Throwable => Failure(e)
    }
  }

  def generateWithDefinitions_alternate(l: List[Template]): Try[(Option[String], Definitions)] =
    l match {
      case Nil =>
        Failure(new IllegalAccessException(path.reverse.toString))
      case e :: l => generateWithDefinitions(e) match {
        case Failure(f) =>
          generateWithDefinitions_alternate(l)
        case success => success
      }
    }
}

object Engine {
  def apply(bean: DataProvider): Engine = apply(bean, Map())

  def apply(bean: DataProvider, args: Map[String, String]): Engine = {
    new Engine(Nil, bean, for ((n, v) <- args) yield (n, Text(v)))
  }
}