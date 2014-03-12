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

package smallibs.rapido.lang.checker

import smallibs.rapido.lang.ast._
import scala.Some

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

  def checkRouteService(notifier: ErrorAtPositionNotifier, params: List[TypeRecord], service: Service): ErrorNotifier = {
    val checker = TypeChecker(entities)

    val inputs = (params ++ service.signature.inputs).foldLeft[TypeRecord](TypeObject(Map())) {
      (result, current) => TypeComposed(result, current)
    }

    val acceptHeader: Option[(Type, Type)] = service.action.header match {
      case None => None
      case Some(value) => checker.acceptType(value, inputs)
    }

    val acceptParams: Option[(Type, Type)] = service.action.params match {
      case None => None
      case Some(value) => checker.acceptType(value, inputs)
    }

    val acceptBody: Option[(Type, Type)] = service.action.body match {
      case None => None
      case Some(value) => checker.acceptType(value, inputs)
    }

    notifier.
      subtype(acceptHeader).
      subtype(acceptParams).
      subtype(acceptBody).
      terminate
  }

  def missingDefinitions(notifier: ErrorNotifier): ErrorNotifier = {
    val typeChecker = TypeChecker(entities)
    entities.services.foldLeft[ErrorNotifier](notifier) {
      case (map, (name, service)) =>
        val missingDefinitionsInParameters: List[String] = service.route.params.flatMap {
          typeChecker.missingDefinitions(_)
        }
        val missingDefinitionsInEntries: List[String] = service.entries.flatMap {
          entry =>
            entry.signature.inputs.flatMap {
              typeChecker.missingDefinitions(_)
            } ++ typeChecker.missingDefinitions(entry.signature.output)
        }
        missingDefinitionsInParameters ++ missingDefinitionsInEntries match {
          case Nil => notifier
          case undefined => notifier.atPosition(service.pos).undefined(undefined).terminate
        }
    }
  }

  def checkTypeServices(notifier: ErrorNotifier): ErrorNotifier =
    entities.services.foldLeft[ErrorNotifier](notifier) {
      case (notifier, (name, definition)) =>
        definition.entries.foldLeft[ErrorNotifier](notifier) {
          (notifier, service) => checkRouteService(notifier.atPosition(service.pos), definition.route.params, service)
        }
    }
}

object ServiceChecker {
  def apply(entities: Entities): ServiceChecker = new ServiceChecker(entities)

  def apply(entities: List[Entity]): ServiceChecker = new ServiceChecker(Entities(entities))
}