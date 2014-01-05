package smallibs.page

trait DataProvider {

  def get(name: String): Option[DataProvider]

  def set(name: String, bean: DataProvider): Unit

}