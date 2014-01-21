package smallibs.rapido.ast

trait Entity

case class TypeEntity(name: String, definition: Type) extends Entity

case class ServiceEntity(name: String, route: Route, entries: List[Service]) extends Entity

case class ClientEntity(name: String, provides: List[String]) extends Entity

case class Route(name: String, params: List[Type], path: Path)

