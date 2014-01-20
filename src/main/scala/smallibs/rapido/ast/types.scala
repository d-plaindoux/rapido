package smallibs.rapido.ast

trait Type

case class TypeIdentifier(name: String) extends Type

case class TypeObject(values: Map[String, (Option[Access], Type)]) extends Type

case class TypeMultiple(value: Type) extends Type

case class TypeOptional(value: Type) extends Type

case object TypeBoolean extends Type

case object TypeString extends Type

case object TypeNumber extends Type

case class TypeComposed(left: Type, right: Type) extends Type

trait Access {
  val n: Option[String]

  def name: Option[String] = this.n
}

case class GetAccess(n: Option[String]) extends Access

case class SetAccess(n: Option[String]) extends Access

case class SetGetAccess(n: Option[String]) extends Access