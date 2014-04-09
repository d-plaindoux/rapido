//
// This file has been generated / Do not modify it
//

@[|------------------------------------------------------------------------------------------
    Path string and array representation
   ------------------------------------------------------------------------------------------|]

@DEFINE::PathAsString
    [|"@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

@DEFINE::PathVariable
    [|@REP(, )::values[|@OPT[|Collections.List("@VAL::object"@REP::fields[|, "@VAL"|])|]|]|]

@[|------------------------------------------------------------------------------------------
        Attribute type for get and set operations
   ------------------------------------------------------------------------------------------|]

@DEFINE::DefineType
    [|@OR
        [|@VAL::name|]
        [|@VAL::array[|List<@USE::DefineType>|]|]
        [|@VAL::object[|Map<String, Object>|]|]
        [|@VAL::string[|String|]|]
        [|@VAL::bool[|boolean|]|]
        [|@VAL::int[|long|]|]
        [|@VAL::opt[|@USE::DefineType|]|]|]

@DEFINE::GetType
    [|@OR
        [|Type.data(types::@VAL::name)|]
        [|@VAL::array[|Type.list(@USE::GetType)|]|]
        [|@VAL::object[|Type.map()|]|]
        [|@VAL::string[|Type.string()|]|]
        [|@VAL::bool[|Type.bool()|]|]
        [|@VAL::int[|Type.integer()|]|]
        [|@VAL::opt[|@USE::GetType|]|]|]

@[|------------------------------------------------------------------------------------------
    Attribute specified with GET, SET
   ------------------------------------------------------------------------------------------|]

@DEFINE::PushAccessVar
    [|@SET::AccessVar[|@OPT[|@USE::AccessVar, |]"@VAL::name"|]|]

@DEFINE::GenerateGetterSetter
    [|@OR
    [|
        public @VAL::type[|@USE::DefineType|] @VAL::get() {
            return @VAL::type[|@USE::GetType|].apply(getValue(Collections.List(@USE::AccessVar)));
        }
|][|
        public @USE::this @VAL::set(@VAL::type[|@USE::DefineType|] value) {
            return new @USE::this(setValue(Collections.List(@USE::AccessVar), JSon.apply(value)));
        }
|][|
        public @VAL::type[|@USE::DefineType|] @VAL::set_get() {
            return @VAL::type[|@USE::GetType|].apply(getValue(Collections.List(@USE::AccessVar)))
        }

        public @USE::this @VAL::set_get(@VAL::type[|@USE::DefineType|] value) {
            return new @USE::this(setValue(Collections.List(@USE::AccessVar), JSon.apply(value)));
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
            append(new BasicType.VirtualValue(Collections.List(@OPT[|@USE::ArrayVar, |]"@VAL::name"), @USE::PathAsString, Collections.List(@USE::PathVariable))) |]|]|]
    [||]|]

@[|------------------------------------------------------------------------------------------
    Main for types generation
   ------------------------------------------------------------------------------------------|]

@OPT[|package @USE::package;|]

import @OPT[|@USE::package.|]core.BasicType;
import @OPT[|@USE::package.|]core.JSon;
import @OPT[|@USE::package.|]core.Type;
import @OPT[|@USE::package.|]core.Collections;

import java.util.List;
import java.util.Map;

public interface types {

@REP::types[|
    @SET::this[|@VAL::name|]
    public static @VAL::name @VAL::name() {
        return new @VAL::name(JSon.apply(Collections.Map()));
    }

    public static @VAL::name @VAL::name(JSon data) {
        return new @VAL::name(data);
    }

    public static class @VAL::name extends BasicType {

        private @VAL::name(JSon in) {
            super(in, Collections.emptyList(BasicType.VirtualValue.class)@VAL::definition[|@USE::VirtualType|]);
        }
@VAL::definition[|@USE::VariableGetterSetter|]
    }

    //------------------------------------------------------------------------------------------
|]
}