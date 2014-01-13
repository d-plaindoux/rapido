import httplib as http
import json


class BasicService:
    def __init__(self,url):
        self.url=url

    def httpRequest(self,operation=None,input=None, header={}):
        if header['Content-type'] is None:
            header = dict(

        connection = http.HTTPConnection(self.url)
        connection.request(operation, self.path, json.dumps(input), header)
        try:
            response = connection.getresponse()
            data = response.read()
            print data
            return json.loads(data)
        finally:
            connection.close()

#
# Services:@REP(,)::services[| @VAL::name|]
#

@REP::services[|
class __@VAL::name(BasicService):
    @VAL::route[|def __init__(self, url@REP::params[|, @VAL::name|]):
        BasicService.__init__(self,url)
        @VAL::path[|self.path = "@REP::values[|@OR[|/@VAL::name|][|/%s|]|]" % (@REP(,)::values[|@OPT[|@VAL::object@REP::fields[|['@VAL']|]|]|])|]
        @REP(        )::params[|self.@VAL::name = @VAL::name
|]|]
    @REP(    )::entries[|def @VAL::name(self,input=None, header={}):
        return self.httpRequest(operation="@VAL::operation",
                                input=input,
                                header=header)

|]
def Service_@VAL::name(url):
    return lambda @VAL::route[|@REP(,)::params[|@VAL::name|]|]: __@VAL::name(url@VAL::route[|@REP::params[|, @VAL::name|]|])
|]
#
# Clients:@REP(,)::clients[| @VAL::name|]
#

@REP::clients[|
class @VAL::name:
    def __init__(self, url):
        @REP(        )::provides[|self.@VAL = Service_@VAL(url)
|]|]
