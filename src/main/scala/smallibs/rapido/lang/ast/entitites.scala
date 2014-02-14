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

package smallibs.rapido.lang.ast

import scala.util.parsing.input.Positional

trait Entity extends Positional {
  def name: String
}

case class TypeEntity(name: String, definition: Type) extends Entity

case class ServiceEntity(name: String, route: Route, entries: List[Service]) extends Entity

case class ClientEntity(name: String, provides: List[String]) extends Entity

case class Route(name: String, params: List[Type], path: Path)

case class Entities(values: List[Entity]) {

  def types: Map[String, TypeEntity] =
    (for (e <- values if e.isInstanceOf[TypeEntity]) yield (e.name, e.asInstanceOf[TypeEntity])).toMap

  def services: Map[String, ServiceEntity] =
    (for (e <- values if e.isInstanceOf[ServiceEntity]) yield (e.name, e.asInstanceOf[ServiceEntity])).toMap

  def clients: Map[String, ClientEntity] =
    (for (e <- values if e.isInstanceOf[ClientEntity]) yield (e.name, e.asInstanceOf[ClientEntity])).toMap

}