//
// This file has been generated / Do not modify it
//

@[|------------------------------------------------------------------------------------------
    Higher attributes for object construction
   ------------------------------------------------------------------------------------------|]

@DEFINE::Attributes
    [|@OR
    [|@VAL::object[|Collections.List(@REP(, )::attributes[|"@VAL::name"|])|]|]
    [|[]|]|]

@DEFINE::Virtuals
    [|@OR
    [|@VAL::object[|Collections.List(@REP(, )::virtual[|"@VAL::name"|])|]|]
    [|[]|]|]

@[|------------------------------------------------------------------------------------------
    Service and method parameters
   ------------------------------------------------------------------------------------------|]

@DEFINE::ParameterNames
    [|@REP::params[|, @VAL::name|]|]

@DEFINE::ParametersTypes
    [|@REP::params[|,  @VAL::type::name @VAL::name|]|]

@DEFINE::ParametersTypesOnly
        [|@REP(, )::params[|@VAL::type::name @VAL::name|]|]

@DEFINE::ParametersTypesSpecification
        [|@REP(
)::params[|private final @VAL::type::name @VAL::name;|]|]

@DEFINE::ParametersInitialization
            [|@REP(
)::params[|this.@VAL::name = @VAL::name;|]|]

@DEFINE::ParametersValues
    [|Collections.List(@REP(, )::params[|@VAL::name|])|]

@[|------------------------------------------------------------------------------------------
     Path transformed using string interpolation and Path variables
   ------------------------------------------------------------------------------------------|]

@DEFINE::PathAsString
    [|"@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

@DEFINE::PathVariable
    [|@REP(, )::values[|@OPT[|Collections.List("@VAL::object"@REP::fields[|, "@VAL"|])|]|]|]

@DEFINE::PathVariables
    [|Collections.List(@USE::PathVariable)|]

@[|------------------------------------------------------------------------------------------
    Main for services generation
   ------------------------------------------------------------------------------------------|]
/*
 * Services:@REP(, )::services[|@VAL::name|]
 */

@OPT[|package @USE::package;|]

import @OPT[|@USE::package.|]core.BasicService;
import @OPT[|@USE::package.|]core.BasicType;
import @OPT[|@USE::package.|]core.Collections;
import @OPT[|@USE::package.|]core.JSon;

import java.util.Map;

@REP::types[|import static @OPT[|@USE::package.|]types.@VAL::name;
|]

public interface services {
@REP::services[|
    /*
     * Service @VAL::name
     */
    public class @VAL::name[|@VALService|] extends BasicService {
        @SET::serviceParameters[|@VAL::route[|@USE::ParametersValues|]|]
        @VAL::route[|@USE::ParametersTypesSpecification|]

        private @VAL::name[|@VALService|](String url@VAL::route[|@USE::ParametersTypes|]) {
            super(url, @VAL::route[|getPath(mergeData(@USE::serviceParameters), @VAL::path[|@USE::PathAsString, @USE::PathVariables)|]|]);
            @VAL::route[|@USE::ParametersInitialization|]
        }

        //
        // Public behaviors
        //
        @REP(  )::entries[|
        public @VAL::signature::output::name @VAL::name(@VAL::signature::inputs[|@REP(, )[|@VAL::type::name @VAL::name|])|] {
            final JSon data = mergeData(Collections.emptyList(BasicType.class).append(Collections.List(@VAL::signature::inputs[|@REP(, )[|@VAL::name|]|])).append(@USE::serviceParameters));
            final String path = @OR[|@VAL::path[|getPath(data, @USE::PathAsString, @USE::PathVariables);|]|][|"";|]@OR
            [|@VAL::params[|
            final JSon paramsObject = @VAL::name(data).toJson();
            final Map<String, JSon> params = getValues(paramsObject, Collections.emptyList(String.class).append(@USE::Attributes).append(@USE::Virtuals));|]|][||]@OR
            [|@VAL::body[|
            final JSon bodyObject = @VAL::name(data).toJson();
            final Map<String, JSon> body = getValues(bodyObject, Collections.emptyList(String.class).append(@USE::Attributes).append(@USE::Virtuals));|]|][||]@OR
            [|@VAL::header[|
            final JSon headerObject = @VAL::name(data).toJson();
            final Map<String, JSon> header = getValues(headerObject, Collections.emptyList(String.class).append(@USE::Attributes).append(@USE::Virtuals));|]|][||]
            return @VAL::signature::output::name(httpRequest(path, "@VAL::operation", @OR[|@VAL::params[|params|]|][|Collections.Map()|] ,@OR[|@VAL::body[|body|]|][|Collections.Map()|] ,@OR[|@VAL::header[|header|]|][|Collections.Map()|]));
        }
    |]
    }

    public static @VAL::name[|@VALService|] @VAL::name[|@VALService|](String url@VAL::route[|@USE::ParametersTypes|]) {
       return new @VAL::name[|@VALService|](url@VAL::route[|@USE::ParameterNames|]);
    }
|]
}