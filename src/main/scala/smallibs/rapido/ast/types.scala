package smallibs.rapido.ast

trait Type

case class TypeIdentifier(name: String) extends Type

case class TypeObject(values: Map[String, Type]) extends Type

case class TypeArray(value: Type) extends Type

case object TypeBoolean extends Type

case object TypeString extends Type

case object TypeNumber extends Type
