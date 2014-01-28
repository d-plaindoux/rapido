//
// Clients providing main entry for the REST API.
//

@OPT[|package @USE::package|]

@REP::clients[|class @VAL::name(url:String) {
    @REP(    )::provides[|def @VAL = @VALService(url)|]
}
|]
