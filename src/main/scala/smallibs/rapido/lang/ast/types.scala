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

//---------------------------------------------------------------------------------------------------
// Type definitions
//---------------------------------------------------------------------------------------------------

trait Type

case class TypeIdentifier(name: String) extends Type

case class TypeObject(values: Map[String, TypeAttribute]) extends Type

case class TypeMultiple(value: Type) extends Type

case class TypeOptional(value: Type) extends Type

case object TypeBoolean extends Type

case object TypeString extends Type

case object TypeNumber extends Type

case class TypeComposed(left: Type, right: Type) extends Type

//---------------------------------------------------------------------------------------------------
// Attribute definitions
//---------------------------------------------------------------------------------------------------

trait TypeAttribute

case class ConcreteTypeAttribute(access: Option[Access], kind: Type) extends TypeAttribute

case class VirtualTypeAttribute(value: Path) extends TypeAttribute

//---------------------------------------------------------------------------------------------------
// Attribute access mode
//---------------------------------------------------------------------------------------------------

trait Access {
  val n: Option[String]

  def name: Option[String] = this.n
}

case class GetAccess(n: Option[String]) extends Access

case class SetAccess(n: Option[String]) extends Access

case class SetGetAccess(n: Option[String]) extends Access
