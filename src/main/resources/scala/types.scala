@[|------------------------------------------------------------------------------------------
    Path string and array representation
   ------------------------------------------------------------------------------------------|]

@MACRO::PathAsString
  [|"/@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

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
    new @USE::this(setValue(List(@USE::AccessVar),value).get)
|][|
  def @VAL::set_get: Try[JSon] =
    data getValue List(@USE::AccessVar)

  def @VAL::set_get(value:JSon): @USE::this =
    new @USE::this(setValue(List(@USE::AccessVar),value).get)
|][||]|]

@MACRO::VariableGetterSetter
    [|@OPT
    [|@VAL::object[|@REP::attributes[|@USE::PushAccessVar@USE::GenerateGetterSetter@VAL::type[|@USE::VariableGetterSetter|]|]|]|]|]

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
        [|getVirtualData(List(@OPT[|@USE::ArrayVar,|]"@VAL::name"), @USE::PathAsString, List(@USE::PathVariable))|]|]|]
    [||]|]

@[|------------------------------------------------------------------------------------------
    Main for types generation
   ------------------------------------------------------------------------------------------|]

@OPT[|package @USE::package|]

import scala.util.{Failure, Success, Try}
import @OPT[|@USE::package.|]core.{JSon, ObjectData, Type}

@REP::types[|@SET::this[|@VAL::name|]
//------------------------------------------------------------------------------------------
// Type @VAL::name
//------------------------------------------------------------------------------------------

class @VAL::name(in:JSon) extends Type {
  val data = in
  @VAL::definition[|@USE::VariableGetterSetter|]
  def toJson: Try[JSon] = {
    List[Try[JSon]](@VAL::definition[|@USE::VirtualType|]).foldRight[Try[JSon]](Success(data)) {
      (current, result) => result flatMap (value => result flatMap (_ extend value))
    }
  }
}

object @VAL::name {
  def apply(): @VAL::name = new @VAL::name(ObjectData(Map()))
}

|]
