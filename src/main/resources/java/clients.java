//
// This file has been generated / Do not modify it
//

@OPT[|package @USE::package;|]

interface clients {
@REP::clients[|
    /*
     * Service client @VAL::name
     */

    class @VAL::name {
        @REP(        )::provides[|final public services.@VALService @VAL;
|]
        @VAL::name(String url) {
            @REP(    )::provides[|this.@VAL = services.@VALService(url);
        |]}

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