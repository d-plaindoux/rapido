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
import scala.util.parsing.input.Position

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

    val inputs = Types(params ++ service.signature.inputs)

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
      unlocate
  }

  def missingDefinitions(notifier: ErrorNotifier): ErrorNotifier = {
    val typeChecker = TypeChecker(entities)

    def missingDefinitions(pos: Position, atype: Type): ErrorNotifier =
      ErrorNotifier().locate(pos).undefined(typeChecker.missingDefinitions(atype)).unlocate

    entities.services.foldLeft[ErrorNotifier](notifier) {
      case (notifier, (name, service)) =>
        notifier ++
          missingDefinitions(service.pos, Types(service.route.params)) ++
          service.entries.foldLeft[ErrorNotifier](ErrorNotifier()) {
            (notifier, entry) =>
              notifier ++
                missingDefinitions(entry.pos, Types(entry.signature.inputs :+ entry.signature.output)) ++
                missingDefinitions(entry.pos, entry.action.header getOrElse Types(Nil)) ++
                missingDefinitions(entry.pos, entry.action.params getOrElse Types(Nil)) ++
                missingDefinitions(entry.pos, entry.action.body getOrElse Types(Nil))
          }
    }
  }

  def typeSpecificationErrors(notifier: ErrorNotifier): ErrorNotifier =
    if (notifier.hasError)
      notifier
    else
      entities.services.foldLeft[ErrorNotifier](notifier) {
        case (newNotifier, (name, definition)) =>
          definition.entries.foldLeft[ErrorNotifier](newNotifier) {
            (notifier, service) =>
              checkRouteService(notifier.locate(service.pos), definition.route.params, service)
          }
      }

  def pathSpecificationErrors(notifier: ErrorNotifier): ErrorNotifier = {
    val typeChecker = TypeChecker(entities)

    if (notifier.hasError)
      notifier
    else
      entities.services.foldLeft[ErrorNotifier](notifier) {
        case (notifier, (name, definition)) =>
          notifier.
            locate(definition.pos).
            path(typeChecker.acceptVirtualType(Types(definition.route.params), definition.route.path)).
            unlocate ++
            definition.entries.foldLeft[ErrorNotifier](ErrorNotifier()) {
              (notifier, service) =>
                val inputs = definition.route.params ++ service.signature.inputs
                  notifier.
                    locate(service.pos).
                    pathes(typeChecker.validateType(Types(inputs))).
                    pathes(typeChecker.validateType(Types(inputs :+ service.action.header.getOrElse(Types(Nil))))).
                    pathes(typeChecker.validateType(Types(inputs :+ service.action.params.getOrElse(Types(Nil))))).
                    pathes(typeChecker.validateType(Types(inputs :+ service.action.body.getOrElse(Types(Nil))))).
                    path(typeChecker.acceptVirtualType(Types(inputs), service.action.path.getOrElse(Path(Nil)))).
                    unlocate
            }
      }
  }
}

object ServiceChecker {
  def apply(entities: Entities): ServiceChecker = new ServiceChecker(entities)

  def apply(entities: List[Entity]): ServiceChecker = new ServiceChecker(Entities(entities))
}