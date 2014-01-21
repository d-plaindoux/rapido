@MACRO::Attributes
    [|@OR
    [|@VAL::object[|[@REP(,)[|'@VAL::name'|]|]]|]
    [|None|]|]

@MACRO::ParameterNames
    [|@REP::params[|, @VAL::name|]|]

@MACRO::ParameterValues
    [|@REP(,)::values[|@OPT[|@VAL::object@REP::fields[|['@VAL']|]|]|]|]

@MACRO::RootParameterNames
    [|@VAL::route[|@USE::ParameterNames|]|]

@MACRO::PathAsString
    [|"/@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

@MACRO::PathVariables
    [|[@REP(,)::values[|@OPT[|['@VAL::object'@REP::fields[|, '@VAL'|]]|]|]]|]

@MACRO::PushVar[|@DEFINE::VarPath[|@OPT[|@USE::VarPath|]['@VAL::name']|]|]

@MACRO::GenerateGetterSetter
    [|@OR
    [|
    def @VAL::get(self):
        return self.data@USE::VarPath
|]
    [|
    def @VAL::set(self, value):
        self.data@USE::VarPath = value
        return self
|]
    [|
    def @VAL::set_get(self, value=None):
        if value is None:
            return self.data@USE::VarPath
        else:
            self.data@USE::VarPath = value
            return self
|]
    [||]|]

@MACRO::VariableGetterSetter
    [|@OPT
    [|@VAL::object[|@REP[|@USE::PushVar@USE::GenerateGetterSetter@VAL::type[|@USE::VariableGetterSetter|]|]|]|]|]

@MACRO::Types
    [|@OR
    [|@VAL::bool[|True|]|]
    [|@VAL::int[|0|]|]
    [|@VAL::string[|""|]|]
    [|@VAL::opt[|None|]|]
    [|@VAL::rep[|[]|]|]
    [|@VAL::object[|{@REP(, )[|'@VAL::name': @VAL::type[|@USE::Types|]|]|]}|]|]

import httplib as http
import json


class BasicService:

    #
    # Constructor
    #

    def __init__(self, url):
        self.url = url
        self.path = None

    #
    # Public behaviors
    #

    def http_request(self, path, operation, body=None, header=None, implicit_header=None):
        connection = http.HTTPConnection(self.url)

        if not header:
            header = {}

        if not body:
            body = {}

        if not implicit_header:
            implicit_header = {'Content-type': 'application/json'}

        complete_header = dict(implicit_header.items() + header.items())

        connection.request(operation, self.path + path, json.dumps(body), complete_header)
        try:
            response = connection.getresponse()
            data = response.read()
            return json.loads(data)
        finally:
            connection.close()

    def get_path(self, data, pattern, Attributes):
        return pattern % tuple([self.__get_value(data, attribute) for attribute in Attributes])

    @staticmethod
    def get_data(data, Attributes):
        return '&'.join([key + "=" + str(data[key]) for key in Attributes if data[key]])

    @staticmethod
    def get_object(data, Attributes):
        return dict([(key, data[key]) for key in Attributes if data[key]])

    @staticmethod
    def merge_data(datas):
        data = dict()

        for d in datas:
            if isinstance(d, Type):
                data = dict(data.items() + d.to_dict().items())
            else:
                data = dict(data.items() + d.items())

        return data

    #
    # Private behaviors
    #

    def __get_value(self, data, Attributes):
        if Attributes is None or not Attributes:
            return data
        else:
            return self.__get_value(data[Attributes[0]], Attributes[1:])

#
# Types:@REP(,)::types[| @VAL::name|]
#


class Type:

    def __init__(self):
        self.data = None

    def to_dict(self):
        return self.data

@REP::types[|
class @VAL::name(Type):

    def __init__(self, data=None):
        Type.__init__(self)

        if not data:
            self.data = @VAL::definition[|@USE::Types|]
        else:
            self.data = data
    @VAL::definition[|@USE::VariableGetterSetter|]
|]

#
# Services:@REP(,)::services[| @VAL::name|]
#

@REP::services[|
class __@VAL::name(BasicService):

    #
    # Constructor
    #

    def __init__(self, url@USE::RootParameterNames):
        @VAL::route[|BasicService.__init__(self, url)
        self.implicit_data = self.merge_data([@REP(, )::params[|@VAL::name|]])
        self.path = @VAL::path[|self.get_path(self.implicit_data, @USE::PathAsString, @USE::PathVariables)|]|]

    #
    # Public behaviors
    #

    @REP(    )::entries[|def @VAL::name(self, parameters=None):
        if not parameters:
            data = self.implicit_data
        else:
            data = self.merge_data([self.implicit_data, parameters])

        result = self.http_request(
            path=""@OPT[| + @VAL::path[|self.get_path(data, @USE::PathAsString, @USE::PathVariables)|]|],
            operation="@VAL::operation",
            body=@OR[|@VAL::body[|self.get_object(data, @USE::Attributes)|]|][|{}|],
            header=@OR[|@VAL::header[|self.get_object(data, @USE::Attributes)|]|][|{}|]
        )

        return @OR[|@VAL::result[|self.get_object(data,@USE::Attributes)|]|][|result|]

|]
#
# Service factory
#

def @VAL::name(url):
    return lambda@VAL::route[|@REP(,)::params[| @VAL::name|]|]: __@VAL::name(url@USE::RootParameterNames)
|]
#
# Clients:@REP(,)::clients[| @VAL::name|]
#

@REP::clients[|
class @VAL::name:
    def __init__(self, url):
        @REP(        )::provides[|self.@VAL = @VAL(url)
|]|]
