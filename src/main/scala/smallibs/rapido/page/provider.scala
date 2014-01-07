package smallibs.rapido.page

import scala.Some
import smallibs.page.{Provider, DataProvider}
import smallibs.rapido.ast._

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
        def routeByName(name: String): DataProvider = {
          val routes = for (e <- elements if e.isInstanceOf[RouteEntity]) yield e.asInstanceOf[RouteEntity]
          routes find (_.name == name) match {
            case None => throw new NoSuchElementException
            case Some(route) => new RouteProvider(route)
          }
        }

        for (e <- elements if e.isInstanceOf[ServiceEntity])
        yield {
          val service = e.asInstanceOf[ServiceEntity]
          val route = routeByName(service.name)
          new ServiceProvider(service, route)
        }
      case "routes" =>
        for (e <- elements if e.isInstanceOf[RouteEntity])
        yield new RouteProvider(e.asInstanceOf[RouteEntity])
      case "clients" =>
        for (e <- elements if e.isInstanceOf[ClientEntity])
        yield new ClientProvider(e.asInstanceOf[ClientEntity])
      case "types" =>
        for (e <- elements if e.isInstanceOf[TypeEntity])
        yield new TypeProvider(e.asInstanceOf[TypeEntity])
    }
}

class ServiceProvider(service: ServiceEntity, route: DataProvider) extends DataProvider with AbstractProvider {
  val keys = List("name", "entries", "route")

  def getAsList(name: String): List[DataProvider] =
    name match {
      case "name" => List(Provider.constant(service.name))
      case "entries" => for (entry <- service.entries) yield new EntryProvider(entry)
      case "route" => List(route)
    }
}

class RouteProvider(route: RouteEntity) extends DataProvider with AbstractProvider {
  val keys = List("name", "params", "path")

  def getAsList(name: String): List[DataProvider] =
    name match {
      case "name" => List(Provider.constant(route.name))
      case "params" => for (e <- route.params) yield Provider.constant(e._1)
      case "path" => List(Provider.constant("TODO:signature"))
    }
}

class ClientProvider(client: ClientEntity) extends DataProvider with AbstractProvider {
  val keys = List("name", "provides")

  def getAsList(name: String): List[DataProvider] =
    name match {
      case "name" => List(Provider.constant(client.name))
      case "provides" => for (name <- client.provides) yield Provider.constant(name)
    }
}

class TypeProvider(kind: TypeEntity) extends DataProvider with AbstractProvider {
  val keys = List("name", "definition")

  def getAsList(name: String): List[DataProvider] =
    name match {
      case "name" => List(Provider.constant(kind.name))
      case "definition" => List(Provider.constant("TODO:definition"))
    }
}

class EntryProvider(entry: Service) extends DataProvider with AbstractProvider {
  val keys = List("name", "operation", "signature")

  def getAsList(name: String): List[DataProvider] =
    name match {
      case "name" => List(Provider.constant(entry.name))
      case "operation" => List(Provider.constant(entry.operation.toString))
      case "signature" => List(Provider.constant("TODO:signature"))
    }
}

//
// Main provider entry point
//

object RapidoProvider {
  def entities(elements: List[Entity]): DataProvider = new EntitiesProvider(elements)
}