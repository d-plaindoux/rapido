package smallibs.rapido.ast

case class Path(values: List[PathEntry])

trait PathEntry

case class StaticLevel(name: String) extends PathEntry

case class DynamicLevel(values: List[String]) extends PathEntry