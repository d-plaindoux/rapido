package smallibs.rapido.ast

sealed trait Operation

case object GET extends Operation

case object HEAD extends Operation

case object POST extends Operation

case object PUT extends Operation

case object DELETE extends Operation

case class UserDefine(name: String) extends Operation {
  override def toString: String = name
}
