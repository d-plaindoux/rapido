import httplib as http
import json

@REP::services[
class @VAL:::name:
    def __init__(self @REP::params[,@VAL::name]):
        self.path = "@VAL::route[@VAL::path]"
        @REP::params[
        self.@VAL::name = @VAL::name
        ]

    @REP::entries[
    def @VAL::name(self,input=None):
        headers = {"Content-Type": "application/json"}
        connection = http.HTTPConnection(url)
        connection.request("@VAL::operation", self.path, json.dumps(none), headers)
        try:
            response = connection.getresponse()
            data = response.read()
            return json.loads(data)
        finally:
            connection.close()
    ]
]
