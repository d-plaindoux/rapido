from @OPT[|@ARG::package.|]types import *
from @OPT[|@ARG::package.|]services import *

#
# Clients:@REP(,)::clients[| @VAL::name|]
#

@REP::clients[|
class @VAL::name:
    def __init__(self, url):
        @REP(        )::provides[|self.@VAL = @VAL(url)
|]|]
