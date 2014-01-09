import httplib as http
import json


class basicService:
    def __init__(self):
        pass

    def httpRequest(self,operation=None,input=None, header=None):
        connection = http.HTTPConnection(self.path)
        connection.request(operation, self.path, json.dumps(input), header)
        try:
            response = connection.getresponse()
            data = response.read()
            return json.loads(data)
        finally:
            connection.close()
@REP::services[|

class @VAL::name(basicService):
    @VAL::route[|def __init__(self, url@REP::params[|, @VAL::name|]):
        basicService.__init__(self)
        self.url = url
        self.path = "TODO:path"
        @REP::params[|
        self.@VAL::name = @VAL::name|]|]
    @REP::entries[|
    def @VAL::name(self,input=None, header=None):
        self.httpRequest(operation="@VAL::operation",
                         input=input,
                         header=header)
|]|]
