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
Attribute type for get and set operations
  ------------------------------------------------------------------------------------------|]

@DEFINE::DefineType
  [|@OR
  [|@VAL::name|]
  [|@VAL::array[|List[@USE::DefineType]|]|]
[|@VAL::object[|Map<String, Object>|]|]
[|@VAL::string[|String|]|]
[|@VAL::bool[|Boolean|]|]
[|@VAL::int[|Long|]|]
[|@VAL::opt[|@USE::DefineType|]|]|]

@DEFINE::GetType
  [|@OR
  [|Type.data(@VAL::name.fromJSon)|]
[|@VAL::array[|Type.list(@USE::GetType)|]|]
[|@VAL::object[|Type.map|]|]
[|@VAL::string[|Type.string|]|]
[|@VAL::bool[|Type.bool|]|]
[|@VAL::int[|Type.integer|]|]
[|@VAL::opt[|@USE::GetType|]|]|]

@[|------------------------------------------------------------------------------------------
    Attribute specified with GET, SET
   ------------------------------------------------------------------------------------------|]

@DEFINE::PushAccessVar[|@SET::AccessVar[|@OPT[|@USE::AccessVar, |]"@VAL::name"|]|]

@DEFINE::GenerateGetterSetter
    [|@OR
    [|
  def @VAL::get: Try[@VAL::type[|@USE::DefineType|]] =
    getValue(List(@USE::AccessVar)).map(@VAL::type[|@USE::GetType|])
|][|
  def @VAL::set(value: @VAL::type[|@USE::DefineType|]): @USE::this =
    new @USE::this(setValue(List(@USE::AccessVar), JSon(value).get))
|][|
  def @VAL::set_get: Try[@VAL::type[|@USE::DefineType|]] =
    getValue(List(@USE::AccessVar)).map(@VAL::type[|@USE::GetType|])

  def @VAL::set_get(value: @VAL::type[|@USE::DefineType|]): @USE::this =
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

import scala.util.Try
import @OPT[|@USE::package.|]core.{JSon, ObjectData, BasicType, VirtualValue, Type}

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

  def fromJSon(data: JSon): @VAL::name = new @VAL::name(data)
}
|]
