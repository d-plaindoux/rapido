#
# This file has been generated / Do not modify it
#

@[|------------------------------------------------------------------------------------------
    Path array representation
   ------------------------------------------------------------------------------------------|]

@DEFINE::PathVariable
    [|@REP(, )::values[|@OPT[|['@VAL::object'@REP::fields[|, '@VAL'|]]|]|]|]

@[|------------------------------------------------------------------------------------------
    Attribute specified with GET, SET
   ------------------------------------------------------------------------------------------|]

@DEFINE::PushAccessVar[|@SET::AccessVar[|@OPT[|@USE::AccessVar, |]'@VAL::name'|]|]

@DEFINE::GenerateGetterSetter
    [|@OR
    [|
    def @VAL::get(self):
        return self.get_value(self.__data, [@OPT[|@USE::AccessVar, |]'@VAL::name'])
|][|
    def @VAL::set(self, value):
        self.set_value(self.__data, [@OPT[|@USE::AccessVar|]], '@VAL::name', value)
        return self
|][|
    def @VAL::set_get(self, value=None):
        if value is None:
            return self.get_value(self.__data, [@OPT[|@USE::AccessVar, |]'@VAL::name'])
        else:
            self.set_value(self.__data, [@OPT[|@USE::AccessVar|]], '@VAL::name', value)
            return self
|][||]|]

@DEFINE::VariableGetterSetter
    [|@OPT
    [|@VAL::object[|@REP::attributes[|@USE::GenerateGetterSetter@USE::PushAccessVar@VAL::type[|@USE::VariableGetterSetter|]|]|]|]|]

@[|------------------------------------------------------------------------------------------
    Type parameters
   ------------------------------------------------------------------------------------------|]

@DEFINE::SingleVariableAsParameter
    [|@OR
    [|, @VAL::set=None|]
    [|, @VAL::set_get=None|]
    [||]|]

@DEFINE::VariablesAsParameter
    [|@OPT
    [|@VAL::object[|@REP::attributes[|@USE::SingleVariableAsParameter@VAL::type[|@USE::VariablesAsParameter|]|]|]|]|]

@DEFINE::Types
    [|@OR
    [|@VAL::bool[|True|]|]
    [|@VAL::int[|0|]|]
    [|@VAL::string[|""|]|]
    [|@VAL::opt[|None|]|]
    [|@VAL::array[|[]|]|]
    [|@VAL::object[|{@REP(, )::attributes[|'@VAL::name': @OR[|@VAL::set|][|@VAL::set_get|][|@VAL::type[|@USE::Types|]|]|]}|]|]|]

@[|------------------------------------------------------------------------------------------
    Virtual variables
   ------------------------------------------------------------------------------------------|]

@DEFINE::PushArrayVar[|@SET::ArrayVar[|@OPT[|@USE::ArrayVar, |]'@VAL::name'|]|]

@DEFINE::VirtualType
    [|@OR
    [|@VAL::opt[|@USE::VirtualType|]|]
    [|@VAL::array[|@USE::VirtualType|]|]
    [|@VAL::object[|@REP::attributes
        [|@USE::PushArrayVar@VAL::type[|@USE::VirtualType|]|]@REP::virtual
        [|        self.set_virtual_value(data, [@OPT[|@USE::ArrayVar|]], '@VAL::name', @USE::PathVariable)
|]|]|]
    [||]|]

@[|------------------------------------------------------------------------------------------
    Main for types generation
   ------------------------------------------------------------------------------------------|]
"""
Types:@REP(, )::types[|@VAL::name|]
"""

from @OPT[|@USE::package.|]core.types import Type

@REP::types[|
class @VAL::name(Type):

    def __init__(self, data=None):
        Type.__init__(self)
        if not data:
            self.__data = dict()
        else:
            self.__data = data
    @VAL::definition[|@USE::VariableGetterSetter|]
    def to_dict(self):
        data = self.__data.copy()
        # Review this code / Wrong generation
@VAL::definition[|@USE::VirtualType|]
        return data

|]
