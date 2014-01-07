import httplib as http
import json


class basicService:
    def __init__(self, url):
        self.url = url

    def httpRequest(self,operation=None,input=None, header=None):
        connection = http.HTTPConnection(self.host)
        connection.request(operation, self.path, json.dumps(input), header)
        try:
            response = connection.getresponse()
            data = response.read()
            return json.loads(data)
        finally:
            connection.close()
@REP::services[

class @VAL::name(basicService):
    @VAL::route[def __init__(self, url@REP::params[, @VAL::name]):
        basicService.__init__(self, url)
        self.path = "@VAL::path"
        @REP::params[
        self.@VAL::name = @VAL::name]]
    @REP::entries[
    def @VAL::name(self,input=None, header=None):
        self.httpRequest(operation="@VAL::operation",
                         input=input,
                         header=header)
]]
