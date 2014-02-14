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

package smallibs.rapido.lang.checker.types

import smallibs.rapido.lang.ast._
import scala.Some

/**
 * The type check validates the specification checking object
 * type compatibilities and definition in each service scope
 */
class TypeChecker(entities: Entities) {

  type Conflicts = Map[String, List[Entity]]

  def findConflicts: Conflicts = {
    def find(entities: List[Entity], conflicts: Conflicts): Conflicts =
      entities match {
        case Nil =>
          conflicts
        case entity :: entities =>
          val newConflicts = conflicts get entity.name match {
            case None => conflicts + (entity.name -> List(entity))
            case Some(l) => conflicts + (entity.name -> (l ++ List(entity)))
          }
          find(entities, newConflicts)
      }
    for (e <- find(entities.values, Map()) if e._2.size > 1) yield e
  }

  def missingDefinitions(value: Type): List[String] =
    value match {
      case TypeBoolean | TypeNumber | TypeString => Nil
      case TypeIdentifier(name) =>
        if (entities.types contains name) Nil else List(name)
      case TypeOptional(value) =>
        missingDefinitions(value)
      case TypeMultiple(value) =>
        missingDefinitions(value)
      case TypeObject(value) =>
        value.map {
          case (_, ConcreteTypeAttribute(_, value)) => missingDefinitions(value)
          case (_, VirtualTypeAttribute(value)) => Nil
        }.flatten.toList
    }

  def acceptType(types: Map[String, Type], receiver: Type, value: Type): Boolean =
    (receiver, value) match {
      case (TypeBoolean, TypeBoolean) => true
      case (TypeNumber, TypeNumber) => true
      case (TypeString, TypeString) => true
      case (TypeBoolean, TypeBoolean) => true
      case (TypeOptional(receiver), _) => acceptType(types, receiver, value)
      case (TypeMultiple(receiver), TypeMultiple(value)) => acceptType(types, receiver, value)
      case (TypeIdentifier(name1), TypeIdentifier(name2)) if name1 == name2 => true
      case (TypeIdentifier(name), _) => acceptType(types, (types get name).get, value)
      case (_, TypeIdentifier(name)) => acceptType(types, receiver, (types get name).get)
      case (TypeObject(map1), TypeObject(map2)) =>
        def attributeType(attribute: TypeAttribute): Type =
          attribute match {
            case ConcreteTypeAttribute(_, type2) => type2
            case VirtualTypeAttribute(_) => TypeString
          }
        map2 forall {
          case (name, att2) =>
            map1 get name match {
              case None => false
              case Some(att1) => acceptType(types, attributeType(att1), attributeType(att2))
            }
        }
    }
}

object TypeChecker {
  def apply(entities: Entities): TypeChecker = new TypeChecker(entities)

  def apply(entities: Entity*): TypeChecker = new TypeChecker(Entities(entities.toList))
}
