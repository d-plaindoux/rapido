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

sealed trait Type

case class TypeMultiple(value: Type) extends Type {
  override def toString: String = s"($value)*"
}

case class TypeOptional(value: Type) extends Type {
  override def toString: String = s"($value)?"
}

sealed trait TypeAtomic extends Type

// Internal use for virtual types validation
case object TypeBot extends Type

case object TypeBoolean extends TypeAtomic {
  override def toString: String = "bool"
}

case object TypeString extends TypeAtomic {
  override def toString: String = "string"
}

case object TypeNumber extends TypeAtomic {
  override def toString: String = "int"
}

sealed trait TypeRecord extends Type

case class TypeIdentifier(name: String) extends TypeRecord {
  override def toString: String = name
}

case class TypeObject(values: Map[String, TypeAttribute]) extends TypeRecord {
  override def toString: String = (for ((n, v) <- values) yield s"$n: $v").toList.mkString("{", ",", "}")
}

case class TypeComposed(left: TypeRecord, right: TypeRecord) extends TypeRecord {
  override def toString: String = s"$left with $right"
}

object Types {
  def apply(types: List[TypeRecord]): TypeRecord =
    types.toList match {
      case List(e) =>
        e
      case l => l.foldLeft[TypeRecord](TypeObject(Map())) {
        (result, current) => TypeComposed(result, current)
      }
    }
}

//---------------------------------------------------------------------------------------------------
// Attribute definitions
//---------------------------------------------------------------------------------------------------

sealed trait TypeAttribute

case class ConcreteTypeAttribute(access: Option[Access], kind: Type) extends TypeAttribute {
  override def toString: String = kind.toString
}

case class VirtualTypeAttribute(value: Path) extends TypeAttribute {
  override def toString: String = "string"
}

//---------------------------------------------------------------------------------------------------
// Attribute access mode
//---------------------------------------------------------------------------------------------------

sealed trait Access {
  val n: Option[String]

  def name: Option[String] = this.n
}

case class GetAccess(n: Option[String]) extends Access

case class SetAccess(n: Option[String]) extends Access

case class SetGetAccess(n: Option[String]) extends Access
