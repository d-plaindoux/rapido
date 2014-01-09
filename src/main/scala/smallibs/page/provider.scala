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

class RecordProvider(map: Map[String, DataProvider]) extends DataProvider {
  def values: List[DataProvider] = map.values.toList

  def get(name: String): Option[DataProvider] = map get name

  def set(name: String, data: DataProvider): DataProvider =
    new RecordProvider(map + (name -> data))
}

// ------------------------------------------------------------------

class SetProvider(set: List[DataProvider]) extends DataProvider {
  def values: List[DataProvider] = set

  def get(name: String): Option[DataProvider] =
    throw new IllegalAccessException

  def set(name: String, data: DataProvider): DataProvider =
    throw new IllegalAccessException
}

// ------------------------------------------------------------------

object Provider {
  def constant(value: String): DataProvider =
    new ConstantProvider(value)

  def record(values: (String, DataProvider)*): DataProvider =
    record(values.toMap)

  def record(values: Map[String, DataProvider]): DataProvider =
    new RecordProvider(values)

  def empty: DataProvider =
    set()

  def set(provides: DataProvider*): DataProvider =
    set(provides.toList)

  def set(providers: List[DataProvider]): DataProvider =
    new SetProvider(providers)
}
