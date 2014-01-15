package smallibs.page.ast

trait Template

//
// Models
//

case class Value(name: Option[String], content: Option[Template]) extends Template

case class Define(name: String, content: Template) extends Template

case class Use(name: String) extends Template

case class Repetition(name: Option[String], separator: Option[String], content: Option[Template]) extends Template

case class Optional(name: Option[String], content: Option[Template]) extends Template

case class Alternate(name: Option[String], content: List[Template]) extends Template

case object NoTemplate extends Template

case class Text(text: String) extends Template

case class Sequence(sequence: List[Template]) extends Template
