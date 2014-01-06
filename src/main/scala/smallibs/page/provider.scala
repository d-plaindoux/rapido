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
    new MapProvider(map + (name -> data))
}

// ------------------------------------------------------------------

object Provider {
  def map(values: (String, DataProvider)*): DataProvider =
    new MapProvider(values.toMap)

  def empty: DataProvider =
    map()

  def list(list: DataProvider*): DataProvider =
    new MapProvider(list.foldLeft[(Int, Map[String, DataProvider])](0, Map()) {
      (result, element) => (result._1 + 1, result._2 + (result._1.toString -> element))
    }._2)

  def constant(value: String): DataProvider =
    new ConstantProvider(value)
}
