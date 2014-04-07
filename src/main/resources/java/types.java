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
        public JSon @VAL::get() {
            return data.getValue(List(@USE::AccessVar));
        }
|][|
        public @USE::this @VAL::set(Object value) {
            return new @USE::this(setValue(List(@USE::AccessVar), JSon.apply(value)));
        }
|][|
        public Json @VAL::set_get() {
            return data.getValue(List(@USE::AccessVar))
        }

        public @USE::this @VAL::set_get(Object value) {
            return new @USE::this(setValue(List(@USE::AccessVar), JSon.apply(value)));
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
        [|@USE::PushArrayVar@VAL::type[|@USE::VirtualType|]|]@REP::virtual[|.
            append(new BasicType.VirtualValue(List(@OPT[|@USE::ArrayVar, |]"@VAL::name"), @USE::PathAsString, List(@USE::PathVariable))) |]|]|]
    [||]|]

@[|------------------------------------------------------------------------------------------
    Main for types generation
   ------------------------------------------------------------------------------------------|]

@OPT[|package @USE::package;|]

import static @OPT[|@USE::package.|]core.collections.List;
import static @OPT[|@USE::package.|]core.collections.emptyList;
import static @OPT[|@USE::package.|]core.collections.Map;

import @OPT[|@USE::package.|]core.BasicType;
import @OPT[|@USE::package.|]core.JSon;

public interface types {

@REP::types[|
    @SET::this[|@VAL::name|]
    public static @VAL::name @VAL::name() {
        return new @VAL::name(JSon.apply(Map()));
    }

    public static @VAL::name @VAL::name(JSon data) {
        return new @VAL::name(data);
    }

    public static class @VAL::name extends BasicType {

        private @VAL::name(JSon in) {
            super(in, emptyList(BasicType.VirtualValue.class)@VAL::definition[|@USE::VirtualType|]);
        }
@VAL::definition[|@USE::VariableGetterSetter|]
    }

    //------------------------------------------------------------------------------------------
|]
}