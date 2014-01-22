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

package smallibs.rapido

import scala.io.Source
import java.net.URL
import java.io.{PrintWriter, File}
import smallibs.rapido.syntax.RapidoParser
import smallibs.page.syntax.PageParser
import smallibs.page.engine.Engine
import smallibs.rapido.page.RapidoProvider

object GenAPI {

  object Resources {
    def getURL(path: String): Option[URL] =
      getClass getResource path match {
        case null => None
        case url => Some(url)
      }

    def getContent(path: URL): String = {
      val source = Source fromURL path
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

  val usage = """
    Usage: rapido --lang [python|scala] --api filename [--out filename]
              """

  def parserOptions(args: Array[String]): Map[Symbol, String] = {
    if (args.length == 0) {
      println(usage)
      sys.exit(1)
    }

    def nextOption(map: Map[Symbol, String], list: List[String]): Map[Symbol, String] = {
      list match {
        case Nil => map
        case "--lang" :: value :: tail =>
          nextOption(map ++ Map('lang -> value), tail)
        case "--model" :: value :: tail =>
          nextOption(map ++ Map('model -> value), tail)
        case "--api" :: value :: tail =>
          nextOption(map ++ Map('api -> value), tail)
        case "--out" :: value :: tail =>
          nextOption(map ++ Map('out -> value), tail)
        case option :: tail => println("Unknown option " + option)
          sys.exit(1)
      }
    }

    nextOption(Map(), args.toList)
  }

  def rapido(spec: String, lang: Option[String], model: Option[String]): String = {
    val specificationURL: URL = new File(spec).toURI.toURL
    val specification = RapidoParser.parseAll(RapidoParser.specifications, Resources getContent specificationURL)
    if (!specification.successful) {
      throw new Exception(specification.toString)
    }

    val templateURL = (lang, model) match {
      case (Some(lang), _) => (Resources getURL s"/$lang/clients.py") getOrElse {
        throw new Exception(s"unsupported language $lang")
      }
      case (_, Some(model)) => new File(model).toURI.toURL
      case _ => throw new Exception("missing lang or model")
    }

    val template = PageParser.parseAll(PageParser.template, Resources getContent templateURL)
    if (!template.successful) {
      throw new Exception(template.toString)
    }

    Engine(RapidoProvider.entities(specification.get)).generate(template.get).get.get
  }

  def main(args: Array[String]) = {
    try {
      val options = parserOptions(args)
      val generatedAPI = rapido((options get 'api).get, options get 'lang, options get 'model)
      options get 'out match {
        case None => print(generatedAPI)
        case Some(name) => Resources saveContent(name, generatedAPI)
      }
    } catch {
      case e: Throwable =>
        println(e.getMessage)
        e.printStackTrace
    }
  }
}