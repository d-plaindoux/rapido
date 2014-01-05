package smallibs.page.engine

//import org.specs2.mutable._
import org.specs2.mutable._
import smallibs.page.ast._
import smallibs.page.Bean

object DummyBean extends Bean {
  def get(name: String): Bean = ???

  def set(name: String, bean: Bean): Unit = ???
}

object RapidoSpec extends Specification {
  "Generator should" should {
    "provides a ident" in {
      val result = Engine(DummyBean).generate(Text("Hello, World"))
      result mustEqual "Hello, World"
    }
  }
}