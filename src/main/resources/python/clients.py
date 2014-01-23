from @OPT[|@USE::package.|]types import *
from @OPT[|@USE::package.|]services import *

#
# Clients:@REP(,)::clients[| @VAL::name|]
#

@REP::clients[|
class @VAL::name:
    def __init__(self, url):
        @REP(        )::provides[|self.@VAL = @VAL(url)
|]|]
