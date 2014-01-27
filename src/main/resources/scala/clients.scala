//
// Clients providing main entry for the REST API.
//

import @OPT[|@USE::package.|]services

@REP::clients[|
class @VAL::name(url:String) {
    @REP(    )::provides[|def @VAL = service.@VAL(url)|]
}
|]
