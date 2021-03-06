//
// This file has been generated / Do not modify it
//

@OPT[|package @USE::package|]

object clients {
@REP::clients[|
  /*
   * Service client @VAL::name
   */

  class @VAL::name(url:String) {
    @REP(  )::provides[|def @VAL::name = @VAL::name[|@VAL|]Service(url)
  |]}

  object @VAL::name {
    object fromURL {
      def apply(url:String): @VAL::name = new @VAL::name(url)
    }

    object secured {
      def apply(url:String): @VAL::name = fromURL(s"https://$url")
    }

    def apply(url:String): @VAL::name = fromURL(s"http://$url")
  }

|]
}
