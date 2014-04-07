//
// This file has been generated / Do not modify it
//

@[|------------------------------------------------------------------------------------------
    Higher attributes for object construction
   ------------------------------------------------------------------------------------------|]

@DEFINE::Attributes
    [|@OR
    [|@VAL::object[|List(@REP(, )::attributes[|"@VAL::name"|])|]|]
    [|[]|]|]

@DEFINE::Virtuals
    [|@OR
    [|@VAL::object[|List(@REP(, )::virtual[|"@VAL::name"|])|]|]
    [|[]|]|]

@[|------------------------------------------------------------------------------------------
    Service and method parameters
   ------------------------------------------------------------------------------------------|]

@DEFINE::ParameterNames
    [|@REP::params[|, @VAL::name|]|]

@DEFINE::ParametersTypes
    [|@REP::params[|,  @VAL::type::name @VAL::name|]|]

@DEFINE::ParametersValues
    [|List(@REP(, )::params[|@VAL::name|])|]

@[|------------------------------------------------------------------------------------------
     Path transformed using string interpolation and Path variables
   ------------------------------------------------------------------------------------------|]

@DEFINE::PathAsString
    [|"@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

@DEFINE::PathVariable
    [|@REP(, )::values[|@OPT[|List("@VAL::object"@REP::fields[|, "@VAL"|])|]|]|]

@DEFINE::PathVariables
    [|List(@USE::PathVariable)|]

@[|------------------------------------------------------------------------------------------
    Main for services generation
   ------------------------------------------------------------------------------------------|]
/*
 * Services:@REP(, )::services[|@VAL::name|]
 */

@OPT[|package @USE::package;|]

import static @OPT[|@USE::package.|]core.collections.List;
import static @OPT[|@USE::package.|]core.collections.emptyList;
import static @OPT[|@USE::package.|]core.collections.Map;

import @OPT[|@USE::package.|]core.BasicService;
import @OPT[|@USE::package.|]core.JSon;

@REP::types[|import static @OPT[|@USE::package.|]types.@VAL::name;
|]

public interface services {
@REP::services[|
    /*
     * Service @VAL::name
     */
    public class @VAL::name[|@VALService|] extends BasicService {
        @SET::serviceParameters[|@VAL::route[|@USE::ParametersValues|]|]

        public @VAL::name[|@VALService|](String url@VAL::route[|@USE::ParametersTypes|]) {
            super(url, @VAL::route[|getPath(mergeData(@USE::serviceParameters), @VAL::path[|@USE::PathAsString, @USE::PathVariables)|]|]);
        }

        //
        // Public behaviors
        //
        @REP(  )::entries[|
        public @VAL::signature::output::name @VAL::name(@VAL::signature::inputs[|@REP(, )[|@VAL::type::name @VAL::name|])|] {
            final JSon data = mergeData(List(@VAL::signature::inputs[|@REP(, )[|@VAL::name|]|]).append(@USE::serviceParameters));
            final String path = @OR[|@VAL::path[|getPath(data, @USE::PathAsString, @USE::PathVariables);|]|][|Success("");|]@OR
            [|@VAL::params[|
            final JSon paramsObject = @VAL::name(data).toJson();
            final Map<String, JSon> params = getValues(paramsObject, emptyList(String.class).append(@USE::Attributes).append(@USE::Virtuals));|]|][||]@OR
            [|@VAL::body[|
            final JSon bodyObject = @VAL::name(data).toJson();
            final Map<String, JSon> body = getValues(bodyObject, emptyList(String.class).append(@USE::Attributes).append(@USE::Virtuals));|]|][||]@OR
            [|@VAL::header[|
            final JSon headerObject = @VAL::name(data).toJson();
            final Map<String, JSon> header = getValues(headerObject, emptyList(String.class).append(@USE::Attributes).append(@USE::Virtuals));|]|][||]
            return @VAL::signature::output::name(httpRequest(path, "@VAL::operation", @OR[|@VAL::params[|params|]|][|null|] ,@OR[|@VAL::body[|body|]|][|null|] ,@OR[|@VAL::header[|header|]|][|null|]));
        }
    |]
    }

    public static @VAL::name[|@VALService|] @VAL::name[|@VALService|](String url) {
       return new @VAL::name[|@VALService|](url@VAL::route[|@USE::ParameterNames|]);
    }
|]
}