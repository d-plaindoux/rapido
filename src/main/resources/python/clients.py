"""
Clients providing main entry for the REST API.
"""

from @OPT[|@USE::package|] import services

#
# Clients:@REP(,)::clients[| @VAL::name|]
#

@REP::clients[|
class @VAL::name:
    def __init__(self, url):
        @REP(        )::provides[|self.@VAL = services.@VAL(url)
|]|]
