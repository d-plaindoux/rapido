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

  "Entities universe" should {

    "with only one definition has not conflict" in {
      val e1 = TypeEntity("t1", TypeNumber)
      TypeChecker(e1).findConflicts mustEqual Map()
    }

    "with two different definitions has not conflict" in {
      val e1 = TypeEntity("t1", TypeNumber)
      val e2 = TypeEntity("t2", TypeNumber)
      TypeChecker(e1, e2).findConflicts mustEqual Map()
    }

    "with two different types with the same name is a conflict" in {
      val e1 = TypeEntity("t1", TypeNumber)
      val e2 = TypeEntity("t1", TypeNumber)
      TypeChecker(e1, e2).findConflicts mustEqual Map("t1" -> List(e1, e2))
    }

    "with two different type and service with the same name is a conflict" in {
      val e1 = TypeEntity("t1", TypeNumber)
      val e2 = ServiceEntity("t1", Route("", Nil, Path(Nil)), Nil)
      TypeChecker(e1, e2).findConflicts mustEqual Map("t1" -> List(e1, e2))
    }

    "with two different type and client with the same name is a conflict" in {
      val e1 = TypeEntity("t1", TypeNumber)
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
      TypeChecker(TypeEntity("a", TypeNumber)).missingDefinitions(TypeIdentifier("a")) mustEqual Nil
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

  "SubTyping" should {
    "accept same native types" in {
    }
  }
}
