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

class ServiceChecker(entities: Entities) {

  def checkService(params: List[TypeRecord], service: Service): Boolean = {
    val checker = TypeChecker(entities)

    val inputs = (params ++ service.signature.inputs).foldRight[TypeRecord](TypeObject(Map())) {
      (result, current) => TypeComposed(result, current)
    }

    val acceptHeader = service.action.header match {
      case None => true
      case Some(value) => checker.acceptType(value, inputs)
    }

    val acceptParams = service.action.params match {
      case None => true
      case Some(value) => checker.acceptType(value, inputs)
    }

    val acceptBody = service.action.body match {
      case None => true
      case Some(value) => checker.acceptType(value, inputs)
    }

    acceptHeader && acceptParams && acceptBody
  }

  def checkServices: Boolean =
    entities.services forall {
      case (name, definition) => definition.entries forall {
        service => checkService(definition.route.params, service)
      }
    }

}

object ServiceChecker {
  def apply(entities: Entities): ServiceChecker = new ServiceChecker(entities)

  def apply(entities: List[Entity]): ServiceChecker = new ServiceChecker(Entities(entities))
}