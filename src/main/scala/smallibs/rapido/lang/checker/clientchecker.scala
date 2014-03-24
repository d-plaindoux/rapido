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

/**
 * The type check validates the specification checking object
 * type compatibilities and definition in each service scope
 */

// ---------------------------------------------------------------------------------------------------------------------
// Service checker
// ---------------------------------------------------------------------------------------------------------------------

class ClientChecker(entities: Entities) {

  def missingDefinitions(notifier: ErrorNotifier): ErrorNotifier =
    entities.clients.foldLeft[ErrorNotifier](notifier) {
      case (notifier, (name, client)) =>
        (for (name <- client.provides if !entities.services.contains(name))
        yield name) match {
          case Nil => notifier
          case l =>
            notifier.
              locate(client.pos).
              undefined(l).
              unlocate
        }
    }
}

object ClientChecker {
  def apply(entities: Entities): ClientChecker = new ClientChecker(entities)

  def apply(entities: List[Entity]): ClientChecker = this(Entities(entities))

  def apply(entities: Entity*): ClientChecker = this(entities.toList)
}