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

package smallibs.rapido.page

import scala.Some
import smallibs.page.{Provider, DataProvider}
import smallibs.rapido.lang.ast._

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
        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[ServiceEntity])
          yield {
            val service = e.asInstanceOf[ServiceEntity]
            new ServiceProvider(service, new RouteProvider(service.route, types), types)
          })
        )
      case "routes" =>
        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[ServiceEntity])
          yield new RouteProvider(e.asInstanceOf[ServiceEntity].route, types))
        )
      case "clients" =>
        Some(Provider.set(
          for (e <- elements if e.isInstanceOf[ClientEntity])
          yield new ClientProvider(e.asInstanceOf[ClientEntity], this))
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

class RouteProvider(route: Route, types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("name", "params", "path")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(route.name))
      case "params" =>
        val params = route.params.foldLeft[(Int, List[(Int, Type)])](0, Nil)((i, t) => (i._1 + 1, i._2 ++ List((i._1, t))))
        Some(Provider.set(for ((i, e) <- params._2) yield new ParamProvider((f"sp_$i%d", e), types)))
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

class ClientProvider(client: ClientEntity, entities: EntitiesProvider) extends DataProvider with AbstractProvider {
  val keys = List("name", "provides")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(client.name))
      case "provides" =>
        Some(Provider.set(
          for (name <- client.provides;
               service <- entities.get("services").get.values
               if service.get("name").get.toString.equals(name))
          yield service)
        )
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
  val keys = List("bool", "int", "string", "opt", "array", "object")

  def deref(t: Type): Option[Type] =
    t match {
      case TypeIdentifier(n) =>
        (types get n) flatMap deref
      case TypeComposed(l, r) =>
        (deref(l), deref(r)) match {
          case (Some(TypeObject(l)), Some(TypeObject(r))) => Some(TypeObject(l ++ r))
          case _ => None
        }
      case _ => Some(t)
    }

  def get(name: String): Option[DataProvider] =
    (name, aType, deref(aType)) match {
      case ("name", TypeIdentifier(name), _) => Some(Provider.constant(name))
      case ("bool", _, Some(TypeBoolean)) => Some(Provider.constant("bool"))
      case ("int", _, Some(TypeNumber)) => Some(Provider.constant("int"))
      case ("string", _, Some(TypeString)) => Some(Provider.constant("string"))
      case ("opt", _, Some(TypeOptional(t))) => Some(new TypeProvider(t, types))
      case ("array", _, Some(TypeMultiple(t))) => Some(new TypeProvider(t, types))
      case ("object", _, Some(TypeObject(values))) => Some(new TypeObjectProvider(values, types))
      case _ => None
    }
}

case class TypeObjectProvider(definitions: Map[String, TypeAttribute], types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("attributes", "virtual")

  def get(name: String): Option[DataProvider] =
    name match {
      case "attributes" =>
        val concrete = for ((n, t) <- definitions if t.isInstanceOf[ConcreteTypeAttribute]) yield (n, t)
        val attributes = for ((n, ConcreteTypeAttribute(a, t)) <- concrete) yield new TypeAttributeProvider(n, a, t, types)
        Some(Provider.set(attributes.toList))
      case "virtual" =>
        val virtual = for ((n, t) <- definitions if t.isInstanceOf[VirtualTypeAttribute]) yield (n, t)
        val attributes = for ((n, VirtualTypeAttribute(p)) <- virtual) yield new TypeVirtualAttributeProvider(n, p)
        Some(Provider.set(attributes.toList))
      case _ => None
    }
}

class TypeAttributeProvider(aName: String, access: Option[Access], aType: Type, types: Map[String, Type]) extends DataProvider with AbstractProvider {
  val keys = List("name", "type")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(aName))
      case "get" => access flatMap {
        case GetAccess(n) => Some(Provider.constant(n.getOrElse(aName)))
        case _ => None
      }
      case "set" => access flatMap {
        case SetAccess(n) => Some(Provider.constant(n.getOrElse(aName)))
        case _ => None
      }
      case "set_get" => access flatMap {
        case SetGetAccess(n) => Some(Provider.constant(n.getOrElse(aName)))
        case _ => None
      }
      case "type" => Some(new TypeProvider(aType, types))
      case _ => None
    }
}

class TypeVirtualAttributeProvider(aName: String, path: Path) extends DataProvider with AbstractProvider {
  val keys = List("name", "values")

  def get(name: String): Option[DataProvider] =
    name match {
      case "name" => Some(Provider.constant(aName))
      case "values" => new PathProvider(path) get "values"
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
      case "inputs" =>
        val params = serviceType.inputs.foldLeft[(Int, List[(Int, Type)])](0, Nil)((i, t) => (i._1 + 1, i._2 ++ List((i._1, t))))
        Some(Provider.set(for ((i, e) <- params._2) yield new ParamProvider((f"fp_$i%d", e), types)))
      case "output" => Some(TypeProvider(serviceType.output, types))
      case _ => None
    }
}

//
// Main provider entry point
//

object RapidoProvider {
  def entities(elements: List[Entity]): DataProvider = new EntitiesProvider(elements)
}