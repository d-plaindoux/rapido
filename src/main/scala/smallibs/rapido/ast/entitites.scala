package smallibs.rapido.ast

trait Entity

case class TypeEntity(name: String, value: Type) extends Entity

case class ServiceEntity(name: String, value: Services) extends Entity

case class RouteEntity() extends Entity

