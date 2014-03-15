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

import smallibs.rapido.lang.ast.{Entities, Entity}

class SpecificationChecker(entities: Entities) {

  def findConflicts(notifier: ErrorNotifier): ErrorNotifier = {
    type Conflicts = Map[String, List[Entity]]

    def find(entities: List[Entity], conflicts: Conflicts): Conflicts =
      entities match {
        case Nil =>
          conflicts
        case entity :: otherEntities =>
          val newConflicts = conflicts get entity.name match {
            case None => conflicts + (entity.name -> List(entity))
            case Some(l) => conflicts + (entity.name -> (l ++ List(entity)))
          }
          find(otherEntities, newConflicts)
      }
    find(entities.values, Map()).foldLeft[ErrorNotifier](notifier) {
      case (notifier, (name, entity :: otherEntities)) if otherEntities.size > 0 =>
        notifier.locate(entity.pos).conflict(name, otherEntities.map {
          _.pos
        }).unlocate
      case (notifier, _) =>
        notifier
    }
  }

  def validateSpecification(notifier: ErrorNotifier): ErrorNotifier = {
    // Check the specification right now
    val typeChecker = TypeChecker(entities)
    val serviceChecker = ServiceChecker(entities)

    // Missing definitions
    notifier.
      findWith(this.findConflicts).
      findWith(typeChecker.missingDefinitions).
      findWith(serviceChecker.missingDefinitions).
      findWith(serviceChecker.typeSpecificationErrors).
      findWith(serviceChecker.pathSpecificationErrors)
  }
}

object SpecificationChecker {
  def apply(entities: List[Entity]): SpecificationChecker = new SpecificationChecker(Entities(entities))

  def apply(entities: Entity*): SpecificationChecker = this(entities.toList)
}