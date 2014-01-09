package smallibs.rapido.page

import scala.Some
import smallibs.page.{Provider, DataProvider}
import smallibs.rapido.ast._

trait AbstractProvider {
  self: DataProvider =>

  val keys: List[String]

  def values: List[DataProvider] =
    for (n <- keys if self.get(n) != None) yield self.get(n).get

  def set(name: String, data: DataProvider): DataProvider = throw new IllegalAccessException
}

class EntitiesProvider(elements: List[Entity]) extends DataProvider with AbstractProvider {
  val keys = List("services", "routes", "clients", "types")

  def get(name: String): Option[DataProvider] =
    name match {
      case "services" =>
        def routeByName(name: String): DataProvider = {
          val routes = for (e <- elements if e.isInstanceOf[RouteEntity]) yield e.asInstanceOf[RouteEntity]
          routes find (_.name == name) match {
            case None => throw new NoSuchElementException(name)
            case Some(route) => new RouteProvider(route)
          }
        }

        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[ServiceEntity])
          yield {
            val service = e.asInstanceOf[ServiceEntity]
            val route = routeByName(service.name)
            new ServiceProvider(service, route)
          })
        )
      case "routes" =>
        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[RouteEntity])
          yield new RouteProvider(e.asInstanceOf[RouteEntity]))
        )
      case "clients" =>
        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[ClientEntity])
          yield new ClientProvider(e.asInstanceOf[ClientEntity]))
        )
      case "types" =>
        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[TypeEntity])
          yield new TypeProvider(e.asInstanceOf[TypeEntity]))
        )
      case _ => None
    }
}

class ServiceProvider(service: ServiceEntity, route: DataProvider) extends DataProvider with AbstractProvider {
  val keys = List("name", "entries", "route")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(service.name))
      case "entries" => Some(Provider.set(for (entry <- service.entries) yield new EntryProvider(entry)))
      case "route" => Some(route)
      case _ => None
    }
}

class RouteProvider(route: RouteEntity) extends DataProvider with AbstractProvider {
  val keys = List("name", "params", "path")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(route.name))
      case "params" => Some(Provider.set(for (e <- route.params) yield new ParamProvider(e)))
      case "path" => Some(new PathProvider(route.path))
      case _ => None
    }
}

class ParamProvider(param: (String, Type)) extends DataProvider with AbstractProvider {
  val keys = List("name", "type")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(param._1))
      case "type" => Some(Provider.constant("TODO:type"))
      case _ => None
    }
}

class PathProvider(path: Path) extends DataProvider with AbstractProvider {
  val keys = List("name", "type")

  def get(name: String): Option[DataProvider] =
    name match {
      case "values" =>
        Some(Provider.set(
          for (p <- path.values)
          yield p match {
            case s@StaticLevel(_) => new StaticPathProvider(s)
            case d@DynamicLevel(_) => new DynamicPathProvider(d)
          }
        ))
      case _ => None
    }
}

class StaticPathProvider(path: StaticLevel) extends DataProvider with AbstractProvider {
  val keys = List("name", "type")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(path.name))
      case _ => None
    }
}

class DynamicPathProvider(param: DynamicLevel) extends DataProvider with AbstractProvider {
  val keys = List("name", "type")

  def get(name: String): Option[DataProvider] =
    name match {
      case "values" => Some(Provider.set(for (param <- param.values) yield Provider.constant(param)))
      case _ => None
    }
}

class ClientProvider(client: ClientEntity) extends DataProvider with AbstractProvider {
  val keys = List("name", "provides")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(client.name))
      case "provides" => Some(Provider.set(for (name <- client.provides) yield Provider.constant(name)))
      case _ => None
    }
}

class TypeProvider(kind: TypeEntity) extends DataProvider with AbstractProvider {
  val keys = List("name", "definition")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(kind.name))
      case "definition" => None
      case _ => None
    }
}

class EntryProvider(entry: Service) extends DataProvider with AbstractProvider {
  val keys = List("name", "operation", "signature")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(entry.name))
      case "operation" => Some(Provider.constant(entry.operation.toString))
      case "signature" => Some(Provider.constant("TODO:signature"))
      case _ => None
    }
}

//
// Main provider entry point
//

object RapidoProvider {
  def entities(elements: List[Entity]): DataProvider = new EntitiesProvider(elements)
}