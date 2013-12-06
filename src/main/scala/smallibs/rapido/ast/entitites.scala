package smallibs.rapido.ast

trait Entity

case class TypeEntity(name: String, value: Type) extends Entity

case class ServiceEntity(name: String, value: List[Service]) extends Entity

case class RouteEntity(name: String, params: List[(String, Type)], path: Path) extends Entity

case class ClientEntity(name: String, services: List[String]) extends Entity

