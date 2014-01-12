#
# Clients:@REP::clients[| @VAL::name|]
#
@REP::clients[|
class @VAL::name(ClientAPI):
    def __init__(self, url):
        ClientAPI.__init__(self, url)
        @REP(        )::provides[|self.@VAL = Service_@VAL
|]|]
# end of file
