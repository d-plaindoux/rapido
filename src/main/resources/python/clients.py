#
# This file has been generated / Do not modify it
#

"""
Clients providing main entry for the REST API.
"""

from @OPT[|@USE::package|] import services

#
# Clients:@REP(,)::clients[| @VAL::name|]
#

@REP::clients[|
class @VAL::name:
    class secured:
        def __init__(self, url):
            @REP(            )::provides[|self.@VAL = services.@VAL("https", url)
|]
    def __init__(self, url):
        @REP(        )::provides[|self.@VAL = services.@VAL("http", url)
|]|]
