package smallibs.rapido.ast

trait Entity

case class TypeEntity(name: String, value: Type) extends Entity

case class ServiceEntity(entries: List[ServiceEntry]) extends Entity

case class RouteEntity() extends Entity

class ServiceEntry(name: String, operation: Operation, in: Type, out: Type, err: Type)

