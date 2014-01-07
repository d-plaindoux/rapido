package smallibs.page.ast

trait Template

//
// Models
//

case class Value(name: Option[String], content: Option[Template]) extends Template

case class Repetition(name: Option[String], content: Template) extends Template

case object NoTemplate extends Template

case class Text(text: String) extends Template

case class Sequence(sequence: List[Template]) extends Template
