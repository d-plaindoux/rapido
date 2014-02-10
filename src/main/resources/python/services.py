@[|------------------------------------------------------------------------------------------
    Higher attributes for object construction
   ------------------------------------------------------------------------------------------|]

@MACRO::Attributes
    [|@OR
    [|@VAL::object[|[@REP(, )::attributes[|'@VAL::name'|]]|]|]
    [|[]|]|]


@MACRO::Virtuals
    [|@OR
    [|@VAL::object[|[@REP(, )::virtual[|"@VAL::name"|]]|]|]
    [|[]|]|]

@[|------------------------------------------------------------------------------------------
    Service and method parameters
   ------------------------------------------------------------------------------------------|]

@MACRO::ParameterNames
    [|@REP::params[|, @VAL::name|]|]

@[|------------------------------------------------------------------------------------------
     Path transformed using string interpolation and Path variables
   ------------------------------------------------------------------------------------------|]

@MACRO::PathAsString
    [|"/@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

@MACRO::PathVariable
    [|@REP(, )::values[|@OPT[|['@VAL::object'@REP::fields[|, '@VAL'|]]|]|]|]

@MACRO::PathVariables
    [|[@USE::PathVariable]|]

@[|------------------------------------------------------------------------------------------
    Main for services generation
   ------------------------------------------------------------------------------------------|]
"""
Services:@REP(, )::services[|@VAL::name|]
"""

from @OPT[|@USE::package.|]core import services
@OPT[|from @USE::package |]import types

@REP::services[|
class __@VAL::name(services.BasicService):

    #
    # Constructor
    #

    def __init__(self, protocol, url@VAL::route[|@USE::ParameterNames|]):
        @VAL::route[|services.BasicService.__init__(self, protocol, url)
        self.implicit_data = self.merge_data([@REP(, )::params[|@VAL::name|]])
        self.path = @VAL::path[|self.get_path(self.implicit_data, @USE::PathAsString, @USE::PathVariables)|]|]

    #
    # Public behaviors
    #

    @REP(    )::entries[|def @VAL::name(self@VAL::signature::inputs[|@REP[|, @VAL::name|])|]:
        data = self.merge_data([self.implicit_data@VAL::signature::inputs[|@REP[|, @VAL::name|]|]])

        result = self.http_request(
            path=@OR[|@VAL::path[|self.get_path(data, @USE::PathAsString, @USE::PathVariables)|]|][|""|],
            operation="@VAL::operation",
            params=@OR[|@VAL::params[|self.get_object(types.@VAL::name(data).to_dict(), @USE::Attributes + @USE::Virtuals)|]|][|{}|],
            body=@OR[|@VAL::body[|self.get_object(types.@VAL::name(data).to_dict(), @USE::Attributes + @USE::Virtuals)|]|][|{}|],
            header=@OR[|@VAL::header[|self.get_object(types.@VAL::name(data).to_dict(), @USE::Attributes + @USE::Virtuals)|]|][|{}|]
        )

        return @VAL::signature::output[|types.@VAL::name(result)|]

|]|]
#
# Service factories
#
@REP::services[|

def @VAL::name(proto, url):
    return lambda@VAL::route[|@REP(, )::params[| @VAL::name|]|]: __@VAL::name(proto, url@VAL::route[|@USE::ParameterNames|])
|]
