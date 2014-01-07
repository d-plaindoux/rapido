package smallibs.rapido.ast

trait Entity

case class TypeEntity(name: String, definition: Type) extends Entity

case class ServiceEntity(name: String, entries: List[Service]) extends Entity

case class RouteEntity(name: String, params: List[(String, Type)], path: Path) extends Entity

case class ClientEntity(name: String, provides: List[String]) extends Entity

