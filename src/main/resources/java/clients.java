//
// This file has been generated / Do not modify it
//

@OPT[|package @USE::package;|]

@[|------------------------------------------------------------------------------------------
        Service and method parameters
   ------------------------------------------------------------------------------------------|]

@DEFINE::ParametersTypesOnly
        [|@REP(, )::params[|types.@VAL::type::name @VAL::name|]|]

@DEFINE::ParameterNames
        [|@REP::params[|, @VAL::name|]|]

@[|------------------------------------------------------------------------------------------
        Main for clients generation
   ------------------------------------------------------------------------------------------|]

interface clients {
@REP::clients[|
    /*
     * Service client @VAL::name
     */

    class @VAL::name {
        private final String url;

        @VAL::name(String url) {
            this.url = url;
        }

        @REP::provides[|
        final services.@VAL::name[|@VALService|] @VAL::name(@VAL::route[|@USE::ParametersTypesOnly|]) {
            return services.@VAL::name[|@VALService|](url@VAL::route[|@USE::ParameterNames|]);
        };
        |]
        public static @VAL::name fromURL(String url) {
            return new @VAL::name(url);
        }

        public static @VAL::name secured(String url) {
            return fromURL(String.format("https://%s", url));
        }
    }

    public static @VAL::name @VAL::name(String url) {
        return @VAL::name.fromURL(String.format("http://%s", url));
    }
|]
}