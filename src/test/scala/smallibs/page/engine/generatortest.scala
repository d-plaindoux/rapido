package smallibs.page.engine

//import org.specs2.mutable._

import org.specs2.mutable._
import scala.util.Success
import smallibs.page.DataProvider
import smallibs.page.ast._

case class Value(value: String) extends DataProvider {
  def get(name: String): Option[DataProvider] = None

  def set(name: String, bean: DataProvider): Unit = ???

  override def toString: String = value
}

case class Associations(map: Map[String, DataProvider]) extends DataProvider {
  def get(name: String): Option[DataProvider] = map get name

  def set(name: String, bean: DataProvider): Unit = ???
}

object PageSpec extends Specification {
  "Generator should" should {

    "provides a result with an input text" in {
      val engine = Engine(Associations(Map()))
      engine.generate(Text("Hello, World")) mustEqual Success("Hello, World")
    }

    "provides a result with an input Ident" in {
      val engine = Engine(Associations(Map("hello" -> Value("World"))))
      engine.generate(AnIdent("hello")) mustEqual Success("World")
    }

    "provides a result with an input String" in {
      val engine = Engine(Associations(Map("hello" -> Value("World"))))
      engine.generate(AString("hello")) mustEqual Success("\"World\"")
    }

    "provides a result with an input sequence" in {
      val engine = Engine(Associations(Map("hello" -> Value("Hello"), "world" -> Value("World"))))
      engine.generate(
        Sequence(List(AnIdent("hello"), Text(", "), AnIdent("world"), Text("!")))
      ) mustEqual Success("Hello, World!")
    }
  }
}