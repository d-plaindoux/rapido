package smallibs.rapido.ast

import smallibs.page.{Provider, DataProvider}

trait AbstractProvider {
  val keys: List[String]

  def get(name: String): Option[DataProvider] = Some(Provider.list(getAsList(name)))

  def set(name: String, data: DataProvider): DataProvider = throw new IllegalAccessException

  def values: List[DataProvider] = (for (name <- keys) yield getAsList(name)).flatten

  def getAsList(name: String): List[DataProvider]
}

class EntitiesProvider(elements: List[Entity]) extends DataProvider with AbstractProvider {
  val keys = List("services", "routes", "clients", "types")

  def getAsList(name: String): List[DataProvider] =
    name match {
      case "services" =>
        for (e <- elements if e.isInstanceOf[ServiceEntity])
        yield new ServiceProvider(e.asInstanceOf[ServiceEntity])
      case "routes" => ???
      case "clients" => ???
      case "types" => ???
    }
}

class ServiceProvider(service: ServiceEntity) extends DataProvider with AbstractProvider {
  val keys = List("name", "entries")

  def getAsList(name: String): List[DataProvider] =
    name match {
      case "name" => List(Provider.constant(service.name))
      case "entries" => for (entry <- service.entries) yield new EntryProvider(entry)
    }
}

class EntryProvider(entry: Service) extends DataProvider with AbstractProvider {
  val keys = List("name", "operation", "signature")

  def getAsList(name: String): List[DataProvider] =
    name match {
      case "name" => List(Provider.constant(entry.name))
      case "operation" => List(Provider.constant(entry.operation.toString))
      case "signature" => ???
    }
}

//
// Main provider entry point
//

object RapidoProvider {
  def entities(elements: List[Entity]): DataProvider = new EntitiesProvider(elements)
}