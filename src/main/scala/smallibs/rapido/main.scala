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
    def getURL(path: String): URL =
      getClass getResource path

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
    Usage: --lang [python|scala] --api filename [--out filename]
              """

  def parserOptions(args: Array[String]): Map[Symbol, String] = {
    if (args.length == 0) {
      println(usage)
      exit(1)
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
        case option :: tail => println("Unknown option " + option)
          exit(1)
      }
    }

    nextOption(Map(), args.toList)
  }

  def rapido(spec: String, lang: String): String = {
    val specificationURL: URL = new File(spec).toURI.toURL
    val specification = RapidoParser.parseAll(RapidoParser.specifications, Resources getContent specificationURL).get
    val languageURL = Resources getURL s"/${lang}/clients.py"
    val language = PageParser.parseAll(PageParser.template, Resources getContent languageURL).get

    Engine(RapidoProvider.entities(specification)).generate(language).get.get
  }

  def main(args: Array[String]) = {
    val options = parserOptions(args)
    val generatedAPI = rapido((options get 'api).get, (options get 'lang).get)
    options get 'out match {
      case None => print(generatedAPI)
      case Some(name) => Resources saveContent(name, generatedAPI)
    }
  }
}