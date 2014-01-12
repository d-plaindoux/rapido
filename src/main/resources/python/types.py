#
# Types:@REP::types[| @VAL::name|]
#

@REP::types[|
class Class@VAL::name:
    def __init__(self@REP::params[|, VAL::name=None|]):
        @REP::params[|
        self.@VAL::name=VAL::name|]
    @REP::params[|
    def get_VAL::name(self):
        return self.@VAL::name

    def set_VAL::name(self, value):
        self.@VAL::name = value
        return self
|]

def @VAL::name(@REP::params[|, VAL::name=None|]):
    return new Class@VAL::name(@REP::params[|@VAL::name=VAL::name|])
]]