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

import org.specs2.mutable._
import smallibs.rapido.lang.ast._
import smallibs.rapido.lang.checker.types.TypeChecker
import smallibs.rapido.lang.ast.ServiceEntity
import smallibs.rapido.lang.ast.Route
import smallibs.rapido.lang.ast.TypeEntity

object TypeCheckerTest extends Specification {

  "Type composition" should {
    "of attributes returns the second one when its not an object type" in {
      val att1 = ConcreteTypeAttribute(None, TypeString)
      val att2 = ConcreteTypeAttribute(None, TypeNumber)
      TypeChecker().composeAttribute(att1, att2) mustEqual att2
    }

    "of different objects returns both in one" in {
      val v1 = Map("a" -> ConcreteTypeAttribute(None, TypeString))
      val v2 = Map("b" -> ConcreteTypeAttribute(None, TypeNumber))
      val t1 = TypeObject(v1)
      val t2 = TypeObject(v2)
      val tr = TypeObject(v1 ++ v2)
      TypeChecker().unfoldType(TypeComposed(t1, t2)) mustEqual tr
    }

    "of different object with same attributes overrides object type" in {
      val v1 = Map("a" -> ConcreteTypeAttribute(None, TypeString))
      val v2 = Map("a" -> ConcreteTypeAttribute(None, TypeNumber))
      val t1 = TypeObject(v1)
      val t2 = TypeObject(v2)
      TypeChecker().unfoldType(TypeComposed(t1, t2)) mustEqual t2
    }

    "of different encapsulated object with different attributes returns encapsulation of both in one" in {
      val v1 = Map("a" -> ConcreteTypeAttribute(None, TypeString))
      val v2 = Map("b" -> ConcreteTypeAttribute(None, TypeNumber))
      val t1 = TypeObject(v1)
      val t2 = TypeObject(v2)
      val tr = TypeObject(v1 ++ v2)
      val ft = (t: Type) => TypeObject(Map("c" -> ConcreteTypeAttribute(None, t)))
      TypeChecker().unfoldType(TypeComposed(ft(t1), ft(t2))) mustEqual ft(tr)
    }

    "of different encapsulated object with same attributes overrides encapsulation of object type" in {
      val v1 = Map("a" -> ConcreteTypeAttribute(None, TypeString))
      val v2 = Map("a" -> ConcreteTypeAttribute(None, TypeNumber))
      val t1 = TypeObject(v1)
      val t2 = TypeObject(v2)
      val ft = (t: Type) => TypeObject(Map("c" -> ConcreteTypeAttribute(None, t)))
      TypeChecker().unfoldType(TypeComposed(ft(t1), ft(t2))) mustEqual ft(t2)
    }
  }

  "Entities universe" should {

    "with only one definition has not conflict" in {
      val e1 = TypeEntity("t1", TypeObject(Map()))
      TypeChecker(e1).findConflicts mustEqual Map()
    }

    "with two different definitions has not conflict" in {
      val e1 = TypeEntity("t1", TypeObject(Map()))
      val e2 = TypeEntity("t2", TypeObject(Map()))
      TypeChecker(e1, e2).findConflicts mustEqual Map()
    }

    "with two different types with the same name is a conflict" in {
      val e1 = TypeEntity("t1", TypeObject(Map()))
      val e2 = TypeEntity("t1", TypeObject(Map()))
      TypeChecker(e1, e2).findConflicts mustEqual Map("t1" -> List(e1, e2))
    }

    "with two different type and service with the same name is a conflict" in {
      val e1 = TypeEntity("t1", TypeObject(Map()))
      val e2 = ServiceEntity("t1", Route("", Nil, Path(Nil)), Nil)
      TypeChecker(e1, e2).findConflicts mustEqual Map("t1" -> List(e1, e2))
    }

    "with two different type and client with the same name is a conflict" in {
      val e1 = TypeEntity("t1", TypeObject(Map()))
      val e2 = ClientEntity("t1", Nil)
      TypeChecker(e1, e2).findConflicts mustEqual Map("t1" -> List(e1, e2))
    }

    "with two different service and client with the same name is a conflict" in {
      val e1 = ServiceEntity("t1", Route("", Nil, Path(Nil)), Nil)
      val e2 = ClientEntity("t1", Nil)
      TypeChecker(e1, e2).findConflicts mustEqual Map("t1" -> List(e1, e2))
    }
  }

  "Type Entity" should {
    "be consistent if its a native type like int, bool or string" in {
      TypeChecker().missingDefinitions(TypeString) mustEqual Nil
    }

    "be consistent if its a referenced and defined type" in {
      TypeChecker(TypeEntity("a", TypeObject(Map()))).missingDefinitions(TypeIdentifier("a")) mustEqual Nil
    }

    "be inconsistent if its a referenced and undefined type" in {
      TypeChecker().missingDefinitions(TypeIdentifier("a")) mustEqual List("a")
    }

    "be inconsistent if its an optional referenced and undefined type" in {
      TypeChecker().missingDefinitions(TypeOptional(TypeIdentifier("a"))) mustEqual List("a")
    }

    "be inconsistent if its a multiple referenced and undefined type" in {
      TypeChecker().missingDefinitions(TypeMultiple(TypeIdentifier("a"))) mustEqual List("a")
    }

    "be inconsistent if its an object with a referenced and undefined type" in {
      val t = TypeObject(Map("a" -> ConcreteTypeAttribute(None, TypeIdentifier("a"))))
      TypeChecker().missingDefinitions(t) mustEqual List("a")
    }
  }

  "Type definition" should {
    "be valid when no external reference is done" in {
      TypeChecker().validateType(TypeObject(Map("a" -> ConcreteTypeAttribute(None, TypeString)))) mustEqual Nil
    }

    "be valid with a virtual attribute referencing a defined type" in {
      TypeChecker().validateType(TypeObject(Map(
        "a" -> ConcreteTypeAttribute(None, TypeString),
        "b" -> VirtualTypeAttribute(Path(List(DynamicLevel(List("a")))))
      ))) mustEqual Nil
    }
/*
    "be invalid with a virtual attribute referencing an undefined type" in {
      TypeChecker().validateType(TypeObject(Map(
        "b" -> VirtualTypeAttribute(Path(List(DynamicLevel(List("a")))))
      ))) mustEqual List(Path(List(DynamicLevel(List("a")))))
    }
*/
  }

  "SubTyping" should {
    "accept same native types" in {
      TypeChecker().acceptType(TypeString, TypeString) mustEqual None
    }

    "reject different native types" in {
      TypeChecker().acceptType(TypeString, TypeNumber) mustEqual Some((TypeString, TypeNumber))
    }

    "accept optional type versus type" in {
      TypeChecker().acceptType(TypeOptional(TypeString), TypeString) mustEqual None
    }

    "accept optional type versus optional type" in {
      TypeChecker().acceptType(TypeOptional(TypeString), TypeOptional(TypeString)) mustEqual None
    }

    "accept optional object with an optional attribute versus empty object type" in {
      val t1: TypeObject = TypeObject(Map("a" -> ConcreteTypeAttribute(None, TypeOptional(TypeString))))
      val t2: TypeObject = TypeObject(Map())
      TypeChecker().acceptType(t1, t2) mustEqual Some((t1, t2))
    }

    "reject empty object type versus optional object with an optional attribute" in {
      val t1: TypeObject = TypeObject(Map())
      val t2: TypeObject = TypeObject(Map("a" -> ConcreteTypeAttribute(None, TypeOptional(TypeString))))
      TypeChecker().acceptType(t1, t2) mustEqual None
    }

    "accept optional object with an optional attribute versus empty object type" in {
      val t1: TypeObject = TypeObject(Map("a" -> ConcreteTypeAttribute(None, TypeOptional(TypeString))))
      val t2: TypeObject = TypeObject(Map())
      TypeChecker().acceptType(t1, t2) mustEqual Some((t1, t2))
    }

    "accept type with virtual definition as-is" in {
      val t1: TypeObject = TypeObject(Map("a" -> VirtualTypeAttribute(Path(Nil))))
      val t2: TypeObject = TypeObject(Map())
      TypeChecker().acceptType(t1, t2) mustEqual None
    }
  }
}
