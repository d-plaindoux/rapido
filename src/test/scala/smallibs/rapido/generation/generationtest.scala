package smallibs.rapido.generation

import org.specs2.mutable._
import scala.io.Source
import scala.util.Success
import smallibs.page.engine.Engine
import smallibs.page.syntax.PageParser
import smallibs.rapido.page.RapidoProvider
import smallibs.rapido.syntax.RapidoParser
import java.io.{File, PrintWriter}

object Resources {
  def getContent(path: String): String = {
    val source = Source.fromURL(getClass getResource path)
    try {
      source.getLines mkString "\n"
    } finally {
      source.close()
    }
  }

  def saveContent(path: String, content: String) = {
    val source = new PrintWriter(new File(path))
    try {
      source.write(content)
    } finally {
      source.close()
    }
  }
}

object Generation extends Specification {
  val entities = RapidoParser.parseAll(RapidoParser.specifications, Resources getContent "/specification.rest").get

  "Generator" should {

    "provides service names" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.01").get
      val expected = Success(Some("places,place,"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides type names" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.02").get
      val expected = Success(Some("Error,Address,Place,Places,Empty,"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides route names" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.03").get
      val expected = Success(Some("place,places,"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides client names" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.04").get
      val expected = Success(Some("placesRest1,placesRest2,"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides service names and entries name" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.05").get
      val expected = Success(Some("places(list,create),place(get,update,delete)"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides route names and parameters name" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.06").get
      val expected = Success(Some("place(p,),places(),"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides route path" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.07").get
      val expected = Success(Some("\"/places/%s\" % (p.name),\"/places\" % ()"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

  }

  "Python Generator" should {

    "provides complete interface" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.01.py").get
      val expected = Resources getContent "/template.01.py.result"
      val result = Engine(RapidoProvider.entities(entities)).generate(template).get.get
      Resources saveContent("/tmp/expected", expected)
      Resources saveContent("/tmp/result", result)
      result mustEqual expected
    }

  }

}
