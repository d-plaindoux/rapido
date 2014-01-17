package smallibs.rapido.ast

case class Service(name: String, action: Action, signature: ServiceType)

case class ServiceType(input: Option[Type], output: Type, error: Option[Type])

case class Action(operation: Operation, path: Option[Path], params: Option[Type],
                  body: Option[Type], header: Option[Type], result: Option[Type])