package smallibs.rapido.generation

import org.specs2.mutable._
import scala.io.Source
import scala.util.Success
import smallibs.page.engine.Engine
import smallibs.page.syntax.PageParser
import smallibs.rapido.page.RapidoProvider
import smallibs.rapido.syntax.RapidoParser

object Resources {
  def content(path: String): String = {
    val source = Source.fromURL(getClass getResource path)
    try {
      source.getLines mkString "\n"
    } finally {
      source.close()
    }
  }
}

object Generation extends Specification {
  val entities = RapidoParser.parseAll(RapidoParser.specifications, Resources content "/specification.rest").get

  "Generator" should {

    "provides service names" in {
      val template = PageParser.parseAll(PageParser.template, Resources content "/template.01").get
      val expected = Success("places,place,")
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides type names" in {
      val template = PageParser.parseAll(PageParser.template, Resources content "/template.02").get
      val expected = Success("Error,Address,Place,Places,Empty,")
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides route names" in {
      val template = PageParser.parseAll(PageParser.template, Resources content "/template.03").get
      val expected = Success("place,places,")
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides client names" in {
      val template = PageParser.parseAll(PageParser.template, Resources content "/template.04").get
      val expected = Success("placesRest1,placesRest2,")
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides service names and entries name" in {
      val template = PageParser.parseAll(PageParser.template, Resources content "/template.05").get
      val expected = Success("places(list,create,),place(get,update,delete,),")
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides route names and parameters name" in {
      val template = PageParser.parseAll(PageParser.template, Resources content "/template.06").get
      val expected = Success("place(p,),places(),")
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

  }

  "Python Generator" should {

    "provides complete interface" in {
      val template = PageParser.parseAll(PageParser.template, Resources content "/template.01.py").get
      val expected = Success(Resources content "/template.01.py.result")
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

  }

}
