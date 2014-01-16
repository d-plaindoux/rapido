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

  def get(name: String): Option[DataProvider] = {
    val types = {
      for (e <- elements if e.isInstanceOf[TypeEntity]) yield {
        val entity = e.asInstanceOf[TypeEntity]
        (entity.name, entity.definition)
      }
    }.toMap

    name match {
      case "services" =>
        def routeByName(name: String): DataProvider = {
          val routes = for (e <- elements if e.isInstanceOf[RouteEntity]) yield e.asInstanceOf[RouteEntity]
          routes find (_.name == name) match {
            case None => throw new NoSuchElementException(name)
            case Some(route) => new RouteProvider(route, types)
          }
        }

        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[ServiceEntity])
          yield {
            val service = e.asInstanceOf[ServiceEntity]
            val route = routeByName(service.name)
            new ServiceProvider(service, route, types)
          })
        )
      case "routes" =>
        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[RouteEntity])
          yield new RouteProvider(e.asInstanceOf[RouteEntity], types))
        )
      case "clients" =>
        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[ClientEntity])
          yield new ClientProvider(e.asInstanceOf[ClientEntity]))
        )
      case "types" =>
        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[TypeEntity])
          yield new TypeDefinitionProvider(e.asInstanceOf[TypeEntity], types.toMap))
        )
      case _ => None
    }
  }
}

class ServiceProvider(service: ServiceEntity, route: DataProvider, types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("name", "entries", "route")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(service.name))
      case "entries" => Some(Provider.set(for (entry <- service.entries) yield new EntryProvider(entry, types)))
      case "route" => Some(route)
      case _ => None
    }
}

class RouteProvider(route: RouteEntity, types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("name", "params", "path")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(route.name))
      case "params" => Some(Provider.set(for (e <- route.params) yield new ParamProvider(e, types)))
      case "path" => Some(new PathProvider(route.path))
      case _ => None
    }
}

class ParamProvider(param: (String, Type), types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("name", "type")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(param._1))
      case "type" => Some(new TypeProvider(param._2, types))
      case _ => None
    }
}

class PathProvider(path: Path) extends DataProvider with AbstractProvider {
  val keys = List("values")

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
      case "object" => Some(Provider.constant(param.values.head))
      case "fields" => Some(Provider.set(for (param <- param.values.tail) yield Provider.constant(param)))
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

class TypeDefinitionProvider(kind: TypeEntity, types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("name", "definition")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(kind.name))
      case "definition" => Some(new TypeProvider(kind.definition, types))
      case _ => None
    }
}

case class TypeProvider(aType: Type, types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("bool", "int", "string", "opt", "rep", "object")

  def deref(t: Type): Option[Type] =
    t match {
      case TypeIdentifier(n) => (types get n) flatMap deref
      case _ => Some(t)
    }

  def get(name: String): Option[DataProvider] =
    (name, aType) match {
      case ("bool", TypeBoolean) => Some(Provider.constant("bool"))
      case ("int", TypeNumber) => Some(Provider.constant("int"))
      case ("string", TypeString) => Some(Provider.constant("string"))
      case ("opt", TypeOptional(t)) => Some(new TypeProvider(t, types))
      case ("rep", TypeMultiple(t)) => Some(new TypeProvider(t, types))
      case ("object", TypeObject(values)) =>
        Some(Provider.set(for ((n, t) <- values) yield new TypeAttributeProvider(n, t, types)))
      case ("object", TypeComposed(l, r)) =>
        (deref(l), deref(r)) match {
          case (Some(TypeObject(l)), Some(TypeObject(r))) => new TypeProvider(TypeObject(l ++ r), types).get(name)
          case _ => None
        }
      case (_, TypeIdentifier(_)) =>
        deref(aType) flatMap (t => new TypeProvider(t, types).get(name))
      case _ => None
    }
}

class TypeAttributeProvider(name: String, aType: Type, types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("name", "type")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(name))
      case "type" => Some(new TypeProvider(aType, types))
      case _ => None
    }
}

class EntryProvider(entry: Service, types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("name", "operation", "signature", "path", "params", "body", "header")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(entry.name))
      case "operation" => Some(Provider.constant(entry.action.operation.toString))
      case "signature" => Some(new ServiceTypeProvider(entry.signature, types))
      case "path" => for (p <- entry.action.path) yield new PathProvider(p)
      case "params" => for (b <- entry.action.params) yield TypeProvider(b, types)
      case "body" => for (b <- entry.action.body) yield TypeProvider(b, types)
      case "header" => for (b <- entry.action.header) yield TypeProvider(b, types)
      case _ => None
    }
}

class ServiceTypeProvider(serviceType: ServiceType, types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List()

  def get(name: String): Option[DataProvider] =
    name match {
      case "input" => for (t <- serviceType.input) yield TypeProvider(t, types)
      case "output" => Some(TypeProvider(serviceType.output, types))
      case "error" => for (t <- serviceType.error) yield TypeProvider(t, types)
      case _ => None
    }
}

//
// Main provider entry point
//

object RapidoProvider {
  def entities(elements: List[Entity]): DataProvider = new EntitiesProvider(elements)
}