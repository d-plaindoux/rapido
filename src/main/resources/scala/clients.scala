//
// Clients providing main entry for the REST API.
//

@OPT[|package @USE::package|]

@REP::clients[|
//------------------------------------------------------------------------------------------
// Service client @VAL::name
//------------------------------------------------------------------------------------------

class @VAL::name(url:String) {
    @REP(    )::provides[|def @VAL = @VALService(url)|]
}

object @VAL::name {
    def apply(url:String): @VAL::name = new @VAL::name(url)
}
|]
