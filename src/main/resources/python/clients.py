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

    #
    # Constructor
    #

    def __init__(self,url):
        self.url=url

    #
    # Public behaviors
    #

    def httpRequest(self,
                    path,
                    operation=None,
                    params="",
                    body={},
                    header={},
                    implicit_header={'Content-type':'application/json'}):
        complete_header = dict(implicit_header.items() + header.items())
        connection = http.HTTPConnection(self.url)
        connection.request(operation, path + params, json.dumps(body), complete_header)
        try:
            response = connection.getresponse()
            data = response.read()
            return json.loads(data)
        finally:
            connection.close()

    def getPath(self, input, pattern, attributes):
        return pattern % tuple([ self._getValue(input,attribute) for attribute in attributes ])

    def getParameters(self, input, attributes):
        return '&'.join([ key+"="+str(input[key]) for key in attributes if input[key] ])

    def getObject(self, input, attributes):
        return dict([ (key,input[key]) for key in attributes if input[key] ])

    #
    # Private behaviors
    #

    def _getValue(self, input, attributes):
        if attributes is None or not attributes:
            return input

        return self._getValue(input[attributes[0]],attributes[1:])

#
# Services:@REP(,)::services[| @VAL::name|]
#

@REP::services[|
class __@VAL::name(BasicService):

    #
    # Constructor
    #

    def __init__(self, url@USE::rootParamNames):
        @VAL::route[|BasicService.__init__(self,url)
        @VAL::path[|self.path = @USE::pathAsString % (@USE::paramValues)|]
        @REP(        )::params[|self.@VAL::name = @VAL::name
|]|]
    #
    # Public behaviors
    #

    @REP(    )::entries[|def @VAL::name(self, input={}):
        result = self.httpRequest(self.path@OPT[| + '?' + @VAL::path[|self.getPath(input,@USE::pathAsString,@USE::pathVariables)|]|],
                                  operation="@VAL::operation",
                                  params=@OR[|@VAL::params[|getParameters(input,@USE::attributes)|])|][|""|],
                                  body=@OR[|@VAL::body[|self.getObject(input,@USE::attributes)|]|][|None|],
                                  header=@OR[|@VAL::header[|self.getObject(input,@USE::attributes)|]|][|{}|])
        return @OR[|@VAL::result[|self.getObject(input,@USE::attributes)|]|][|result|]

|]
#
# Service factory
#

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
