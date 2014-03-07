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

import smallibs.rapido.lang.ast.{Entity, Entities}
import smallibs.rapido.lang.checker.{TypeChecker, ServiceChecker}

object SpecificationChecker {

  def validateSpecification(specification:List[Entity]): Unit = {
    // Check the specification right now
    val checker = TypeChecker(Entities(specification))
    val conflicts = checker.findConflicts

    // Duplicated symbols
    if (!conflicts.isEmpty) {
      conflicts.map {
        case (name,entities) =>
          println(s"symbol defined more than once $name")
      }

      throw new Exception(s"[Aborted] find duplicated names")
    }

    // Missing definitions
    val missing = checker.missingDefinitions

    if (!missing.isEmpty) {
      missing.map {
        case (name,list) =>
          println(s"undefined symbol(s) in $name: ${list.addString(new StringBuilder, "(", ", ", ")").toString}")
      }

      throw new Exception(s"[Aborted] find undefined symbols")
    }

    // Type incompatibility
    val typeErrors = ServiceChecker(specification).checkServices

    if (!typeErrors.isEmpty) {
      typeErrors map {
        case ((sn, rn), (t1, t2)) =>
          println(s"[WARNING] type error in service [$sn#$rn]")
          println(s"          $t1 cannot be generated using $t2")
      }

      throw new Exception(s"[Aborted] find incompatoible types")
    }

  }


}