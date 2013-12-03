package smallibs.rapido

sealed trait Entity 
case class Type(values: Map[String,Type]) extends Entity
case class Data(values: Map[String,DataEntry]) extends Entity 
case class Route() extends Entity

class TypeEntry(name:String, definition: Type)
class DataEntry(operation : Operation,in: Type,out: Type)
