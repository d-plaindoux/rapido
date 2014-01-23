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
import scala.util.parsing.json.{JSONObject, JSONArray, JSON}
import smallibs.page.DataProvider

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
        case "--api" :: value :: tail =>
          nextOption(map ++ Map('api -> value), tail)
        case "--out" :: value :: tail =>
          nextOption(map ++ Map('out -> value), tail)
        case "--" :: _ =>
          map
        case option :: tail => println("Unknown option " + option)
          sys.exit(1)
      }
    }

    nextOption(Map(), args.toList)
  }

  def generateAll(provider: DataProvider, outputDirectory: File, inputDirectory: File, files: Any): List[(File, String)] =
    files match {
      case file: String =>
        val templateURL = (Resources getURL s"$inputDirectory/$file") getOrElse {
          throw new Exception(s"File $file not found $inputDirectory")
        }
        val template = PageParser.parseAll(PageParser.template, Resources getContent templateURL)
        if (!template.successful) {
          throw new Exception(template.toString)
        }
        List((new File(outputDirectory, file), Engine(provider).generate(template.get).get.get))
      case JSONArray(l) =>
        l.foldRight[List[(File, String)]](Nil) {
          (e, l) => l ++ generateAll(provider, outputDirectory, inputDirectory, e)
        }
      case JSONObject(l) =>
        l.foldRight[List[(File, String)]](Nil) {
          (e, l) => l ++ generateAll(provider, new File(outputDirectory, e._1), new File(inputDirectory, e._1), e._2)
        }
      case _ => Nil
    }

  def rapido(spec: String, lang: String): List[(File, String)] = {
    val specificationContent = Resources getContent new File(spec).toURI.toURL
    val specification = RapidoParser.parseAll(RapidoParser.specifications, specificationContent)
    if (!specification.successful) {
      throw new Exception(specification.toString)
    }

    val filesURL = (Resources getURL s"/$lang/files.rdo") getOrElse {
      throw new Exception(s"File files.rdo for $lang is not available")
    }

    val description = JSON parseRaw (Resources getContent filesURL) getOrElse {
      throw new Exception(s"File files.rdo for $lang is not using JSON formalism")
    }

    generateAll(RapidoProvider.entities(specification.get), new File("."), new File(s"/$lang"), description)
  }

  def main(args: Array[String]) = {
    try {
      val options = parserOptions(args)
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
            Resources saveContent(file.getPath, entry._2)
        }
      }

      rapido((options get 'api).get, (options get 'lang).getOrElse {
        throw new Exception("option --lang must be specified")
      }).foreach(output)
    } catch {
      case e: Throwable =>
        println(e.getMessage)
    }
  }
}

