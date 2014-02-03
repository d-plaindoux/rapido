"""
Core service
"""

from @OPT[|@USE::package.|]core import types

import httplib as http
import json


class BasicService:

    #
    # Constructor
    #

    def __init__(self, proto, url):
        self.url = url
        self.path = None

    #
    # Public behaviors
    #

    def http_request(self, protocol, path, operation, body=None, header=None, implicit_header=None):
        if protocol == "http":
            connection = http.HTTPConnection(self.url)
        else:
            connection = http.HTTPSConnection(self.url)

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
            if isinstance(d, types.Type):
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
