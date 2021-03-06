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
class TypeChecker(entities: Entities) {

  // -------------------------------------------------------------------------------------------------------------------
  // Check missing type definitions
  // -------------------------------------------------------------------------------------------------------------------

  def missingDefinitions(value: Type): List[String] =
    value match {
      case TypeBoolean | TypeNumber | TypeString | TypeBot => Nil
      case TypeIdentifier(name) =>
        if (entities.types contains name) Nil else List(name)
      case TypeOptional(value) =>
        missingDefinitions(value)
      case TypeComposed(l, r) =>
        missingDefinitions(l) ++ missingDefinitions(r)
      case TypeMultiple(value) =>
        missingDefinitions(value)
      case TypeObject(value) =>
        value.flatMap {
          case (_, ConcreteTypeAttribute(_, value)) => missingDefinitions(value)
          case (_, VirtualTypeAttribute(value)) => Nil
        }.toList
    }

  def missingDefinitions(notifier: ErrorNotifier): ErrorNotifier =
    entities.types.foldLeft[ErrorNotifier](notifier) {
      case (notifier, (name, aType)) =>
        notifier.locate(aType.pos).undefined(missingDefinitions(aType.definition)).unlocate
    }

  // -------------------------------------------------------------------------------------------------------------------
  // Type unfolding operation and composition
  // -------------------------------------------------------------------------------------------------------------------

  def composeAttribute(att1: TypeAttribute, att2: TypeAttribute): TypeAttribute =
    (att1, att2) match {
      case (ConcreteTypeAttribute(a1, t1: TypeRecord), ConcreteTypeAttribute(a2, t2: TypeRecord)) =>
        ConcreteTypeAttribute(a2, unfoldType(TypeComposed(t1, t2)))
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

  def unfoldType(value: TypeRecord): TypeObject =
    value match {
      case value: TypeObject => value
      case TypeIdentifier(name) => unfoldTypeByName(name)
      case TypeComposed(t1, t2) =>
        val TypeObject(v1) = unfoldType(t1)
        val TypeObject(v2) = unfoldType(t2)
        TypeObject(composeSetOfAttributes(v1, v2))
    }

  def unfoldTypeByName(name: String): TypeObject =
    unfoldType((entities.types get name).get.definition)

  // -------------------------------------------------------------------------------------------------------------------
  // Virtual type corner
  // -------------------------------------------------------------------------------------------------------------------

  def acceptVirtualType(initial: Type, path: Path): Option[Path] = {
    path.values.foldLeft[Option[Path]](None) {
      case (Some(r), _) => Some(r)
      case (None, StaticLevel(_)) => None
      case (None, DynamicLevel(l)) =>
        val synthetizedType = l.foldRight[Type](TypeBot) {
          case (e, r) => TypeObject(Map(e -> ConcreteTypeAttribute(None, r)))
        }
        acceptType(synthetizedType, initial) match {
          case None => None
          case Some(_) => Some(path)
        }
    }
  }

  def virtualDefinitions(value: Type): List[Path] =
    value match {
      case TypeOptional(value) =>
        virtualDefinitions(value)
      case TypeMultiple(value) =>
        virtualDefinitions(value)
      case t@TypeIdentifier(name) =>
        virtualDefinitions(unfoldType(t))
      case t@TypeComposed(_, _) =>
        virtualDefinitions(unfoldType(t))
      case TypeObject(value) =>
        value.map {
          case (_, ConcreteTypeAttribute(_, value)) =>
            virtualDefinitions(value)
          case (_, VirtualTypeAttribute(value)) =>
            List(value)
        }.flatten.toList
      case _ => Nil
    }

  def isVirtual(value: Type): Boolean =
    value match {
      case TypeOptional(value) =>
        isVirtual(value)
      case TypeMultiple(value) =>
        isVirtual(value)
      case t@TypeIdentifier(name) =>
        isVirtual(unfoldType(t))
      case t@TypeComposed(_, _) =>
        isVirtual(unfoldType(t))
      case TypeObject(value) =>
        value.forall {
          case (name, ConcreteTypeAttribute(access, value)) =>
            isVirtual(value)
          case (_, VirtualTypeAttribute(value)) =>
            true
        }
      case _ => false
    }

  def validateType(value: Type): List[Path] =
    for (path <- virtualDefinitions(value)
         if acceptVirtualType(value, path) != None)
    yield path

  // -------------------------------------------------------------------------------------------------------------------
  // Check whether a value can be used in place of type receiver
  // -------------------------------------------------------------------------------------------------------------------

  def acceptType(receiver: Type, value: Type): Option[(Type, Type)] =
    (receiver, value) match {
      case (TypeBot, _:TypeAtomic) => None
      case (TypeBoolean, TypeBoolean) => None
      case (TypeNumber, TypeNumber) => None
      case (TypeString, TypeString) => None

      case (TypeComposed(l, r), _) =>
        acceptType(unfoldType(TypeComposed(l, r)), value)
      case (_, TypeComposed(l, r)) =>
        acceptType(receiver, unfoldType(TypeComposed(l, r)))

      case (TypeIdentifier(name1), TypeIdentifier(name2)) if name1 == name2 =>
        None
      case (TypeIdentifier(name), _) =>
        acceptType(unfoldTypeByName(name), value)
      case (_, TypeIdentifier(name)) =>
        acceptType(receiver, unfoldTypeByName(name))

      case (TypeOptional(receiver), TypeOptional(value)) =>
        acceptType(receiver, value)
      case (TypeOptional(receiver), _) =>
        acceptType(receiver, value)
      case (TypeMultiple(receiver), TypeMultiple(value)) =>
        acceptType(receiver, value)

      case (TypeObject(map1), TypeObject(map2)) =>
        def valueType(attribute: TypeAttribute): Type =
          attribute match {
            case ConcreteTypeAttribute(_, type1) =>
              type1
            case VirtualTypeAttribute(l) =>
              TypeString
          }
        map1.foldLeft[Option[(Type, Type)]](None) {
          case (None, (name, ConcreteTypeAttribute(_, t1))) =>
            map2 get name match {
              case None if isVirtual(t1) => None
              case None => Some((receiver, value))
              case Some(att2) =>
                val t2 = valueType(att2)
                acceptType(t1, t2)
            }
          case (None, (name, VirtualTypeAttribute(_))) =>
            None
          case (Some(l), _) =>
            Some(l)
        }
      case _ => Some((receiver, value))
    }

  // -------------------------------------------------------------------------------------------------------------------
  // Check whether a value can be used in place of type receiver
  // -------------------------------------------------------------------------------------------------------------------

  def acceptVirtualType(receiver: Type, value: Type): List[Path] =
    for (path <- virtualDefinitions(receiver)
         if acceptVirtualType(value, path) != None)
    yield path
}

object TypeChecker {
  def apply(entities: Entities): TypeChecker = new TypeChecker(entities)

  def apply(entities: List[Entity]): TypeChecker = this(Entities(entities))

  def apply(entities: Entity*): TypeChecker = this(entities.toList)
}
