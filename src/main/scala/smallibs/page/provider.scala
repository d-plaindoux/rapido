package smallibs.page

trait DataProvider {

  def values: List[DataProvider]

  def get(name: String): Option[DataProvider]

  def set(name: String, data: DataProvider): DataProvider

}

// ------------------------------------------------------------------

class ConstantProvider(value: String) extends DataProvider {
  def values: List[DataProvider] = Nil

  def get(name: String): Option[DataProvider] = None

  def set(name: String, data: DataProvider): DataProvider =
    throw new IllegalAccessException

  override def toString: String = value
}

// ------------------------------------------------------------------

class MapProvider(map: Map[String, DataProvider]) extends DataProvider {
  def values: List[DataProvider] = map.values.toList

  def get(name: String): Option[DataProvider] = map get name

  def set(name: String, data: DataProvider): DataProvider =
    Provider.map(map + (name -> data))
}

// ------------------------------------------------------------------

object Provider {
  def map(map: Map[String, DataProvider]): DataProvider = new MapProvider(map)

  def empty: DataProvider = map(Map())

  def list(list: DataProvider*): DataProvider =
    map(list.foldLeft[(Int, Map[String, DataProvider])](0, Map()) {
      (result, element) => (result._1 + 1, result._2 + (result._1.toString -> element))
    }._2)

  def constant(value: String): DataProvider = new ConstantProvider(value)
}
