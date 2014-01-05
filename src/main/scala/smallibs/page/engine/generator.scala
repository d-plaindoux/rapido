package smallibs.page.engine

import smallibs.page.Bean
import smallibs.page.ast.{Text, Template}

class Engine(bean: Bean) {

  def generate(template: Template): String =
    template match {
      case Text(t) => t
      case _ => ???
    }

  def engine(path: List[String]): Engine =
    Engine(path.foldLeft(bean)((bean, b) => bean get b))

}

object Engine {
  def apply(bean: Bean): Engine = new Engine(bean)
}