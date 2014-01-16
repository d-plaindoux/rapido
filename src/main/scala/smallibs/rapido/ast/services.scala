package smallibs.rapido.ast

case class Service(name: String, action: Action, signature: ServiceType)

case class ServiceType(input: Option[Type], output: Type, error: Option[Type])

case class Action(operation: Operation, path: Option[Path], param: Option[String], body: Option[String], header: Option[String])