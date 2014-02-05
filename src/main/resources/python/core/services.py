#
#  Copyright (C)2014 D. Plaindoux.
#
#  This program is free software; you can redistribute it and/or modify it
#  under the terms of the GNU Lesser General Public License as published
#  by the Free Software Foundation; either version 2, or (at your option) any
#  later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Lesser General Public License for more details.
#
#  You should have received a copy of the GNU Lesser General Public License
#  along with this program; see the file COPYING.  If not, write to
#  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
#

from @OPT[|@USE::package.|]core import types
from sets import Set
import httplib as http
import json

class UnsupportedProtocol(Exception):
    def __init__(self, protocol):
        self.protocol = protocol


class BasicService:

    #
    # Constructor
    #

    def __init__(self, protocol, url):
        if self.protocol == "http":
            self.connectionFactory = http.HTTPConnection
        elif self.protocol == "https":
            self.connectionFactory = http.HTTPSConnection
        else:
            raise UnsupportedProtocol(protocol)

        self.protocol = protocol
        self.url = url
        self.path = None

    #
    # Public behaviors
    #

    def http_request(self, path, operation, body=None, header=None, implicit_header=None):
        connection = self.connectionFactory(self.url)

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
        def deep_merge(data1, data2):
            if type(data1) is not dict or type(data2) is not dict:
                return data2
            intersection = set(data1.keys()) & set(data2.keys())
            data2Only = set(data2.keys()) - set(data1.keys())
            for k in data2Only:
                data1[k] = data2[k]
            for k in intersection:
                data1[k] = deep_merge(data1[k],data2[k])
            return data1

        data = dict()

        for d in datas:
            if isinstance(d, types.Type):
                data = deep_merge(data, d.to_dict())
            else:
                data = deep_merge(data, d)

        return data

    #
    # Private behaviors
    #

    def __get_value(self, data, attributes):
        if attributes is None or not attributes:
            return data
        else:
            return self.__get_value(data[attributes[0]], attributes[1:])
