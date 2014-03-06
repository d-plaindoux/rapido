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

package smallibs.rapido.lang.checker.services

import smallibs.rapido.lang.ast._
import scala.Some
import smallibs.rapido.lang.checker.types.TypeChecker

/**
 * The type check validates the specification checking object
 * type compatibilities and definition in each service scope
 */

sealed trait ServiceError

case class SubTypingError(receive: Type, value: Type) extends ServiceError

case class VirtualTypeError(path: Path) extends ServiceError

// ---------------------------------------------------------------------------------------------------------------------
// Service checker
// ---------------------------------------------------------------------------------------------------------------------

class ServiceChecker(entities: Entities) {

  def checkRouteService(params: List[TypeRecord], service: Service): Option[(Type, Type)] = {
    val checker = TypeChecker(entities)

    val inputs = (params ++ service.signature.inputs).foldRight[TypeRecord](TypeObject(Map())) {
      (result, current) => TypeComposed(result, current)
    }

    val acceptHeader = service.action.header match {
      case None => None
      case Some(value) => checker.acceptType(value, inputs)
    }

    val acceptParams = service.action.params match {
      case None => None
      case Some(value) => checker.acceptType(value, inputs)
    }

    val acceptBody = service.action.body match {
      case None => None
      case Some(value) => checker.acceptType(value, inputs)
    }

    acceptHeader orElse {
      acceptParams orElse {
        acceptBody
      }
    }
  }

  def checkServices: Option[((String, String), (Type, Type))] =
    entities.services.foldLeft[Option[((String, String), (Type, Type))]](None) {
      case (Some(r), _) => Some(r)
      case (None, (name, definition)) =>
        definition.entries.foldLeft[Option[((String, String), (Type, Type))]](None) {
          case (Some(r), _) => Some(r)
          case (None, service) =>
            checkRouteService(definition.route.params, service) map {
              error => ((definition.name, service.name), error)
            }
        }
    }
}

object ServiceChecker {
  def apply(entities: Entities): ServiceChecker = new ServiceChecker(entities)

  def apply(entities: List[Entity]): ServiceChecker = new ServiceChecker(Entities(entities))
}