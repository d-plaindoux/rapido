package smallibs.rapido

import scala.io.Source
import smallibs.rapido.syntax.RapidoParser
import java.net.URL
import java.io.File
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
  }

  def main(args: Array[String]) {
    val specificationURL: URL = new File(args(0)).toURI.toURL
    val specification = RapidoParser.parseAll(RapidoParser.specifications, Resources getContent specificationURL).get
    val languageURL = Resources getURL args(1)
    val language = PageParser.parseAll(PageParser.template, Resources getContent languageURL).get

    print(Engine(RapidoProvider.entities(specification)).generate(language).get)
  }

}