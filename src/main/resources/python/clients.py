@DEFINE::attributes
    [|@OR
    [|@VAL::object[|[@REP(,)[|'@VAL::name'|]|]]|]
    [|None|]|]

@DEFINE::paramNames
    [|@REP::params[|, @VAL::name|]|]

@DEFINE::paramValues
    [|@REP(,)::values[|@OPT[|@VAL::object@REP::fields[|['@VAL']|]|]|]|]

@DEFINE::rootParamNames
    [|@VAL::route[|@USE::paramNames|]|]

@DEFINE::pathAsString
    [|"@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

@DEFINE::pathVariables
    [|[@REP(,)::values[|@OPT[|['@VAL::object'@REP::fields[|,'@VAL'|]]|]|]]|]

import httplib as http
import json


class BasicService:
    def __init__(self,url):
        self.url=url

    def httpRequest(self,
                    path,
                    operation=None,
                    input=None,
                    header={},
                    implicit_header={'Content-type':'application/json'}):
        complete_header = dict(implicit_header.items() + header.items())
        connection = http.HTTPConnection(self.url)
        connection.request(operation, path, json.dumps(input), complete_header)
        try:
            response = connection.getresponse()
            data = response.read()
            return json.loads(data)
        finally:
            connection.close()

    def getPath(self, input, pattern, attributes):
        return "TODO"

    def getParameters(self, input, attributes):
        return '&'.join([ key+"="+str(input[key]) for key in attributes if input[key] ]

    def getObject(self, input, attributes):
        result = {}
        [ result[key] = input[key] for key in attributes if input[key] ]
        return result

#
# Services:@REP(,)::services[| @VAL::name|]
#

@REP::services[|
class __@VAL::name(BasicService):
    def __init__(self, url@USE::rootParamNames):
        @VAL::route[|BasicService.__init__(self,url)
        @VAL::path[|self.path = @USE::pathAsString % (@USE::paramValues)|]
        @REP(        )::params[|self.@VAL::name = @VAL::name
|]|]
    @REP(    )::entries[|def @VAL::name(self, input={}):
        result = self.httpRequest(self.path@OPT[| + '?' + @VAL::path[|self.getPath(input,@USE::pathAsString,@USE::pathVariables)|]|],
                                  operation="@VAL::operation",
                                  params=@OR[|@VAL::params[|getParameters(input,@USE::attributes)|])|][|None|],
                                  body=@OR[|@VAL::body[|self.getObject(input,@USE::attributes)|]|][|None|],
                                  header=@OR[|@VAL::header[|self.getObject(input,@USE::attributes)|]|][|{}|])
        return @OR[|@VAL::result[|self.getObject(input,@USE::attributes)|]|][|result|]

|]
def Service_@VAL::name(url):
    return lambda @VAL::route[|@REP(,)::params[|@VAL::name|]|]: __@VAL::name(url@USE::rootParamNames)
|]
#
# Clients:@REP(,)::clients[| @VAL::name|]
#

@REP::clients[|
class @VAL::name:
    def __init__(self, url):
        @REP(        )::provides[|self.@VAL = Service_@VAL(url)
|]|]
