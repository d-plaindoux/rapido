package smallibs.rapido.ast

case class Service(name: String, operation: Operation, signature: ServiceType)

case class ServiceType(input: Option[Type], output: Type, error: Option[Type])