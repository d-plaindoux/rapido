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

  def composeAttribute(att1: TypeAttribute, att2: TypeAttribute): TypeAttribute =
    (att1, att2) match {
      case (ConcreteTypeAttribute(a1, t1: TypeRecord), ConcreteTypeAttribute(a2, t2: TypeRecord)) =>
        ConcreteTypeAttribute(a2, derefType(TypeComposed(t1, t2)))
      case _ => att2
    }

  def composeSetOfAttributes(v1: Map[String, TypeAttribute], v2: Map[String, TypeAttribute]): Map[String, TypeAttribute] = {
    val k1 = Set(v1.keysIterator.toList: _*)
    val k2 = Set(v2.keysIterator.toList: _*)
    val intersection = k1 & k2
    val r1 = for (key <- intersection) yield key -> composeAttribute(v1.get(key).get, v2.get(key).get)
    val r2 = v1.filterKeys(!intersection.contains(_)) ++ v2.filterKeys(!intersection.contains(_))
    (r1 ++ r2).toMap
  }

  def derefType(value: TypeRecord): TypeObject =
    value match {
      case value: TypeObject => value
      case TypeIdentifier(name) => deref(name)
      case TypeComposed(t1, t2) =>
        val TypeObject(v1) = derefType(t1)
        val TypeObject(v2) = derefType(t2)
        TypeObject(composeSetOfAttributes(v1, v2))
    }

  def deref(name: String): TypeObject =
    derefType((entities.types get name).get.definition)

  def acceptType(receiver: Type, value: Type): Boolean =
    (receiver, value) match {
      case (TypeBoolean, TypeBoolean) => true
      case (TypeNumber, TypeNumber) => true
      case (TypeString, TypeString) => true
      case (TypeIdentifier(name1), TypeIdentifier(name2)) =>
        name1 == name2
      case (TypeIdentifier(name), _) =>
        acceptType(deref(name), value)
      case (_, TypeIdentifier(name)) =>
        acceptType(receiver, deref(name))
      case (TypeOptional(receiver), TypeOptional(value)) =>
        acceptType(receiver, value)
      case (TypeOptional(receiver), _) =>
        acceptType(receiver, value)
      case (TypeMultiple(receiver), TypeMultiple(value)) =>
        acceptType(receiver, value)
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
              case Some(att1) => acceptType(attributeType(att1), attributeType(att2))
            }
        }
      case _ => false
    }
}

object TypeChecker {
  def apply(entities: Entities): TypeChecker = new TypeChecker(entities)

  def apply(entities: Entity*): TypeChecker = new TypeChecker(Entities(entities.toList))
}

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