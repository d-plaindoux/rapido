//
// This file has been generated / Do not modify it
//

@[|------------------------------------------------------------------------------------------
    Path string and array representation
   ------------------------------------------------------------------------------------------|]

@DEFINE::PathAsString
  [|"@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

@DEFINE::PathVariable
    [|@REP(, )::values[|@OPT[|List("@VAL::object"@REP::fields[|, "@VAL"|])|]|]|]

@[|------------------------------------------------------------------------------------------
    Attribute specified with GET, SET
   ------------------------------------------------------------------------------------------|]

@DEFINE::PushAccessVar[|@SET::AccessVar[|@OPT[|@USE::AccessVar, |]"@VAL::name"|]|]

@DEFINE::GenerateGetterSetter
    [|@OR
    [|
  def @VAL::get: Try[JSon] =
    getValue(List(@USE::AccessVar))
|][|
  def @VAL::set(value: Any): @USE::this =
    new @USE::this(setValue(List(@USE::AccessVar), JSon(value).get))
|][|
  def @VAL::set_get: Try[JSon] =
    data getValue List(@USE::AccessVar)

  def @VAL::set_get(value: Any): @USE::this =
    new @USE::this(setValue(List(@USE::AccessVar), JSon(value).get))
|][||]|]

@DEFINE::VariableGetterSetter
    [|@OPT
    [|@VAL::object[|@REP::attributes[|@USE::PushAccessVar@USE::GenerateGetterSetter@VAL::type[|@USE::VariableGetterSetter|]|]|]|]|]

@[|------------------------------------------------------------------------------------------
    Virtual variables
   ------------------------------------------------------------------------------------------|]

@DEFINE::PushArrayVar[|@SET::ArrayVar[|@OPT[|@USE::ArrayVar, |]"@VAL::name"|]|]

@DEFINE::VirtualType
    [|@OR
    [|@VAL::opt[|@USE::VirtualType|]|]
    [|@VAL::array[|@USE::VirtualType|]|]
    [|@VAL::object[|@REP::attributes
        [|@USE::PushArrayVar@VAL::type[|@USE::VirtualType|]|]@REP::virtual[| :+
        VirtualValue(List(@OPT[|@USE::ArrayVar, |]"@VAL::name"), @USE::PathAsString, List(@USE::PathVariable)) |]|]|]
    [||]|]

@[|------------------------------------------------------------------------------------------
    Main for types generation
   ------------------------------------------------------------------------------------------|]

@OPT[|package @USE::package|]

import scala.util.{Failure, Success, Try}
import @OPT[|@USE::package.|]core.{JSon, ObjectData, GenericType, VirtualValue}

@REP::types[|@SET::this[|@VAL::name|]
//------------------------------------------------------------------------------------------
// Type @VAL::name
//------------------------------------------------------------------------------------------

class @VAL::name(in: JSon) extends BasicType(in) {
  val virtualValues = Nil@VAL::definition[|@USE::VirtualType|]
  @VAL::definition[|@USE::VariableGetterSetter|]
}

object @VAL::name {
  def apply(): @VAL::name = new @VAL::name(ObjectData(Map()))

  def fromData(data: JSon): @VAL::name = new @VAL::name(data)
}
|]
