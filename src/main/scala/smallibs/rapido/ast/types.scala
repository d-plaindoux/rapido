package smallibs.rapido.ast

trait Type

case class TypeIdentifier(name: String) extends Type

case class TypeObject(values: List[(String, Type)]) extends Type

case class TypeMultiple(value: Type) extends Type

case class TypeOptional(value: Type) extends Type

case object TypeBoolean extends Type

case object TypeString extends Type

case object TypeNumber extends Type

case class TypeComposed(left: Type, right: Type) extends Type
