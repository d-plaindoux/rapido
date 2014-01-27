@[|------------------------------------------------------------------------------------------
    Path array representation
   ------------------------------------------------------------------------------------------|]

@MACRO::PathVariable
    [|@REP(, )::values[|@OPT[|List("@VAL::object"@REP::fields[|, "@VAL"|])|]|]|]

@[|------------------------------------------------------------------------------------------
    Attribute specified with GET, SET
   ------------------------------------------------------------------------------------------|]

@MACRO::PushAccessVar[|@SET::AccessVar[|@OPT[|@USE::AccessVar,|]"@VAL::name"|]|]

@MACRO::GenerateGetterSetter
    [|@OR
    [|
  def @VAL::get: Try[JSon] =
    getValue(List(@USE::AccessVar))
|][|
  def @VAL::set(value:JSon): @USE::this =
    @USE::this(setValue(List(@USE::AccessVar),value))
|][|
  def @VAL::set_get: Try[JSon] =
    data getValue List(@USE::AccessVar)

  def @VAL::set_get(value:JSon): @USE::this =
    @USE::this(setValue(List(@USE::AccessVar),value))
|][||]|]

@MACRO::VariableGetterSetter
    [|@OPT
    [|@VAL::object[|@REP::attributes[|@USE::PushAccessVar@USE::GenerateGetterSetter@VAL::type[|@USE::VariableGetterSetter|]|]|]|]|]

@[|------------------------------------------------------------------------------------------
    Type parameters
   ------------------------------------------------------------------------------------------|]

@MACRO::SingleVariableAsParameter
    [|@OR
    [|, @VAL::set=None|]
    [|, @VAL::set_get=None|]
    [||]|]

@MACRO::VariablesAsParameter
    [|@OPT
    [|@VAL::object[|@REP::attributes[|@USE::SingleVariableAsParameter@VAL::type[|@USE::VariablesAsParameter|]|]|]|]|]

@MACRO::Types
    [|@OR
    [|@VAL::bool[|True|]|]
    [|@VAL::int[|0|]|]
    [|@VAL::string[|""|]|]
    [|@VAL::opt[|None|]|]
    [|@VAL::rep[|[]|]|]
    [|@VAL::object[|{@REP(, )::attributes[|'@VAL::name': @OR[|@VAL::set|][|@VAL::set_get|][|@VAL::type[|@USE::Types|]|]|]}|]|]|]

@[|------------------------------------------------------------------------------------------
    Virtual variables
   ------------------------------------------------------------------------------------------|]

@MACRO::PushArrayVar[|@SET::ArrayVar[|@OPT[|@USE::ArrayVar, |]'@VAL::name'|]|]

@MACRO::VirtualType
    [|@OR
    [|@VAL::opt[|@USE::VirtualType|]|]
    [|@VAL::rep[|@USE::VirtualType|]|]
    [|@VAL::object[|@REP::attributes
        [|@USE::PushArrayVar@VAL::type[|@USE::VirtualType|]|]@REP(        )::virtual
        [|setVirtualValue(List(@OPT[|@USE::ArrayVar,|]"@VAL::name"), @USE::PathVariable)|]|]|]
    [||]|]

@[|------------------------------------------------------------------------------------------
    Main for types generation
   ------------------------------------------------------------------------------------------|]

@OPT[|package @USE::package|]

import scala.util.{Failure, Success, Try}
import @OPT[|@USE::package.|]core.{JSon, Type}

@REP::types[|@SET::this[|@VAL::name|]
class @VAL::name(data:JSon) extends Type(data) {
  @VAL::definition[|@USE::VariableGetterSetter|]
  def toRaw: Any = {
    List(@VAL::definition[|@USE::VirtualType|])
    data.toRaw
  }
}

object @VAL::name {
  def apply(data:Any): @VAL::name = new @VAL::name(JSon(data).get)
}

|]
