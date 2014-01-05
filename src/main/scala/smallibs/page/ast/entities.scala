package smallibs.page.ast

trait Template

//
// Models
//

case class AnIdent(name: String) extends Template

case class AString(name: String) extends Template

case class ARepetition(name: String, content: Template) extends Template

//
// Intrinsic
//

case class Text(text: String) extends Template

case class Sequence(sequence: List[Template]) extends Template
