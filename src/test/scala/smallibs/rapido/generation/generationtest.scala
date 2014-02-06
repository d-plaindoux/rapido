/*
 * Copyright (C)2014 D. Plaindoux.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package smallibs.rapido.generation

import org.specs2.mutable._
import scala.io.Source
import scala.util.Success
import smallibs.page.engine.Engine
import smallibs.page.lang.syntax.PageParser
import smallibs.rapido.page.RapidoProvider
import smallibs.rapido.lang.syntax.RapidoParser
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
      val expected = Success(Some("places,place,"))
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
      val expected = Success(Some("places(),place(sp_0,),"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides route path" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.07").get
      val expected = Success(Some("\"/places\" % (),\"/places/%s\" % (name)"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides route path with parameters" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.08").get
      val expected = Success(Some("Error={code:int,reason:string};Address={address:string?};Place={address:string?,name:string};Places={places:{address:string?,name:string}*};Empty={}"))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }

    "provides type atoms" in {
      val template = PageParser.parseAll(PageParser.template, Resources getContent "/template.09").get
      val expected = Success(Some("Error(code,reason) ;Address(address) ;Place(address,name) ;Places(address,name) ;Empty() "))
      Engine(RapidoProvider.entities(entities)).generate(template) mustEqual expected
    }
  }
}
