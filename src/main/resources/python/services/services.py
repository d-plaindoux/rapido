@[|------------------------------------------------------------------------------------------
    Higher attributes for object construction
   ------------------------------------------------------------------------------------------|]

@MACRO::Attributes
    [|@OR
    [|@VAL::object[|[@REP(, )::attributes[|'@VAL::name'|]]|]|]
    [|[]|]|]

@[|------------------------------------------------------------------------------------------
    Service and method parameters
   ------------------------------------------------------------------------------------------|]

@MACRO::ParameterNames
    [|@REP::params[|, @VAL::name|]|]

@[|------------------------------------------------------------------------------------------
     Path transformed using string interpolation and Path variables
   ------------------------------------------------------------------------------------------|]

@MACRO::PathAsString
    [|"/@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

@MACRO::PathVariable
    [|@REP(, )::values[|@OPT[|['@VAL::object'@REP::fields[|, '@VAL'|]]|]|]|]

@MACRO::PathVariables
    [|[@USE::PathVariable]|]

@[|------------------------------------------------------------------------------------------
    Main for services generation
   ------------------------------------------------------------------------------------------|]

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
        # TODO - Improve this code fragment
        try:
            response = connection.getresponse()
            data = response.read()
            try:
                return json.loads(data)
            except Exception, e:
                return response
        except Exception, e:
            return e
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

    def __get_value(self, data, attributes):
        if attributes is None or not attributes:
            return data
        else:
            return self.__get_value(data[attributes[0]], attributes[1:])

#
# Services:@REP(, )::services[|@VAL::name|]
#

@REP::services[|
class __@VAL::name(BasicService):

    #
    # Constructor
    #

    def __init__(self, url@VAL::route[|@USE::ParameterNames|]):
        @VAL::route[|BasicService.__init__(self, url)
        self.implicit_data = self.merge_data([@REP(, )::params[|@VAL::name|]])
        self.path = @VAL::path[|self.get_path(self.implicit_data, @USE::PathAsString, @USE::PathVariables)|]|]

    #
    # Public behaviors
    #

    @REP(    )::entries[|def @VAL::name(self@VAL::signature::inputs[|@REP[|, @VAL::name|])|]:
        data = self.merge_data([self.implicit_data@VAL::signature::inputs[|@REP[|, @VAL::name|]|]])

        result = self.http_request(
            path=""@OPT[| + @VAL::path[|self.get_path(data, @USE::PathAsString, @USE::PathVariables)|]|],
            operation="@VAL::operation",
            body=@OR[|@VAL::body[|self.get_object(data, @USE::Attributes)|]|][|{}|],
            header=@OR[|@VAL::header[|self.get_object(data, @USE::Attributes)|]|][|{}|]
        )

        return @OR[|@VAL::result[|self.get_object(data,@USE::Attributes)|]|][|result|]

|]|]
#
# Service factory
#
@REP::services[|

def @VAL::name(url):
    return lambda@VAL::route[|@REP(, )::params[| @VAL::name|]|]: __@VAL::name(url@VAL::route[|@USE::ParameterNames|])
|]
