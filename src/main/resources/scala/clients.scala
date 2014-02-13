//
// This file has been generated / Do not modify it
//

//------------------------------------------------------------------------------------------
// Clients providing main entry for the REST API.
//------------------------------------------------------------------------------------------

@OPT[|package @USE::package|]

@REP::clients[|
//------------------------------------------------------------------------------------------
// Service client @VAL::name
//------------------------------------------------------------------------------------------

class @VAL::name(url:String) {
  @REP::provides[|def @VAL = @VALService(url)
  |]
}

object @VAL::name {
  object secured {
    def apply(url:String): @VAL::name = new @VAL::name(s"https://$url")
  }

  def apply(url:String): @VAL::name = new @VAL::name(s"http://$url")
}
|]
