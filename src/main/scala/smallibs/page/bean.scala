package smallibs.page

trait Bean {

  def get(name: String): Bean

  def set(name: String, bean: Bean): Unit

}