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

import java.io.File
import smallibs.rapido.lang.syntax.RapidoParser
import smallibs.page.lang.syntax.PageParser
import smallibs.page.engine.Engine
import smallibs.rapido.page.RapidoProvider
import scala.util.parsing.json.{JSONObject, JSONArray, JSON}
import smallibs.page.{Provider, DataProvider}
import smallibs.rapido.utils.{Options, Resources}
import smallibs.rapido.lang.checker.SpecificationChecker

object GenAPI {

  val usage = """
    Usage: rapido --lang [python|scala] --api filename [--out filename] [-- <name>=<value>*]
              """

  def generateAll(arguments: Map[String, String], provider: DataProvider, outputDirectory: String => File, inputDirectory: String => File, files: Any): List[(File, String)] =
    files match {
      case file: String =>
        val fileName = inputDirectory(file).getPath
        val templateURL = (Resources getURL fileName) getOrElse {
          throw new Exception(s"File $fileName not found")
        }
        val template = PageParser.parseAll(PageParser.template, Resources getContent templateURL)
        if (!template.successful) {
          throw new Exception(template.toString)
        }
        List((outputDirectory(file), Engine(provider, arguments).generate(template.get).get.get))
      case JSONArray(l) =>
        l.foldRight[List[(File, String)]](Nil) {
          (e, l) => l ++ generateAll(arguments, provider, outputDirectory, inputDirectory, e)
        }
      case JSONObject(l) =>
        l.foldRight[List[(File, String)]](Nil) {
          (e, l) => l ++ generateAll(arguments, provider, outputDirectory, inputDirectory, e._2)
        }
      case _ => Nil
    }

  def rapido(arguments: Map[String, String], spec: String, lang: String): List[(File, String)] = {

    def getFiles(data: Any): Option[Any] =
      data match {
        case JSONObject(l) => l get "files"
        case _ => None
      }

    def getRequiredArguments(data: Any): Option[Any] =
      data match {
        case JSONObject(l) => l get "arguments"
        case _ => None
      }

    val specificationContent = Resources getContent new File(spec).toURI.toURL
    val specification = RapidoParser.parseAll(RapidoParser.specifications, specificationContent)

    if (!specification.successful) {
      throw new Exception(specification.toString)
    }

    // Check the specification right now
    SpecificationChecker.validateSpecification(specification.get)

    val filesURL = (Resources getURL s"/$lang/files.rdo") getOrElse {
      throw new Exception(s"File files.rdo for $lang is not available")
    }

    val description = JSON parseRaw (Resources getContent filesURL) getOrElse {
      throw new Exception(s"File files.rdo for $lang is not using JSON formalism")
    }

    val requiredArguments = getRequiredArguments(description) getOrElse {
      Map()
    }

    val files = getFiles(description) getOrElse {
      throw new Exception("JSON must provides files i.e. { files: [ ... ], ... } ")
    }

    // TODO - Clean this ugly code
    val outputNameGenerator = arguments get "package" match {
      case Some(packageName) => (input: String) => {
        val template = PageParser.parseAll(PageParser.template, input).get
        new File(Engine(Provider.empty, Map("package" -> packageName)).generate(template).get.get)
      }
      case None => (input: String) => new File(input)
    }

    val inputNameGenerator = (input: String) => {
      val template = PageParser.parseAll(PageParser.template, input).get
      new File(Engine(Provider.empty, Map("lang" -> lang)).generate(template).get.get)
    }


    generateAll(arguments, RapidoProvider.entities(specification.get), outputNameGenerator, inputNameGenerator, files)
  }

  def checkArguments(required: List[String], provided: List[String]): List[String] =
    for (r <- required if !(provided contains r)) yield r

  def main(args: Array[String]) = {
    try {
      val (options, arguments) = Options parse(usage, args)
      val output = options get 'out match {
        case None => {
          (entry: (File, String)) =>
            println("--- File " + entry._1 + " ----")
            println(entry._2)
        }
        case Some(name) => {
          (entry: (File, String)) =>
            val file = new File(new File(name), entry._1.getPath) // :(
            println("[generate] File " + file)
            file.getParentFile.mkdirs()
            Resources saveContent(file, entry._2)
        }
      }

      rapido(arguments, (options get 'api).get, (options get 'lang).getOrElse {
        throw new Exception("option --lang must be specified")
      }).foreach(output)
    } catch {
      case e: Throwable =>
        println(e.getMessage)
    }
  }
}

