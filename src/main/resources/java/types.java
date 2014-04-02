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

@DEFINE::PushAccessVar
    [|@SET::AccessVar[|@OPT[|@USE::AccessVar, |]"@VAL::name"|]|]

@DEFINE::GenerateGetterSetter
    [|@OR
    [|
      public Json @VAL::set_get() {
        return data.getValue(List(@USE::AccessVar))
      }
|][|
      public @USE::this @VAL::set_get(JSon value) {
        return new @USE::this(setValue(List(@USE::AccessVar), JSon(value).get))
      }
|][|
      public Json @VAL::set_get() {
        return data.getValue(List(@USE::AccessVar))
      }

      public @USE::this @VAL::set_get(JSon value) {
        return new @USE::this(setValue(List(@USE::AccessVar), JSon(value).get))
      }
|][||]|]

@DEFINE::VariableGetterSetter
    [|@OPT
    [|@VAL::object[|@REP::attributes[|@USE::PushAccessVar@USE::GenerateGetterSetter@VAL::type[|@USE::VariableGetterSetter|]|]|]|]|]

@[|------------------------------------------------------------------------------------------
    Virtual variables
   ------------------------------------------------------------------------------------------|]

@DEFINE::PushArrayVar
    [|@SET::ArrayVar[|@OPT[|@USE::ArrayVar, |]"@VAL::name"|]|]

@DEFINE::VirtualType
    [|@OR
    [|@VAL::opt[|@USE::VirtualType|]|]
    [|@VAL::array[|@USE::VirtualType|]|]
    [|@VAL::object[|@REP::attributes
        [|@USE::PushArrayVar@VAL::type[|@USE::VirtualType|]|]@REP::virtual[| :+
        new BasicType.VirtualValue(List(@OPT[|@USE::ArrayVar, |]"@VAL::name"), @USE::PathAsString, List(@USE::PathVariable)) |]|]|]
    [||]|]

@[|------------------------------------------------------------------------------------------
    Main for types generation
   ------------------------------------------------------------------------------------------|]

@OPT[|package @USE::package|]

import @OPT[|@USE::package.|]core.*

interface Types {

@REP::types[|
    @SET::this[|@VAL::name|]
    public static @VAL::name @VAL::name() {
        return new @VAL::name(ObjectData(Map()));
    }

    public static @VAL::name @VAL::name(JSon data) {
        return new @VAL::name(data);
    }

    public static class @VAL::name extends BasicType {

      private @VAL::name(JSon in) {
        super(in, Nil@VAL::definition[|@USE::VirtualType|])
      }

      @VAL::definition[|@USE::VariableGetterSetter|]
    }

    //------------------------------------------------------------------------------------------

|]
}