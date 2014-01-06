import httplib as http
import json

@rep:services[
class @value:name:
    def __init__(self @rep:params[,@value:name]):
        @rep:params[
        self.@value:name = @value:name
        ]

    @rep:entries[
    def @value:name(self,input=None):
        path = "@value:path"
        headers = {"Content-Type": "application/json"}
        connection = http.HTTPConnection(url)
        connection.request("@value:operation", path, json.dumps(none), headers)
        try:
            response = connection.getresponse()
            data = response.read()
            dd = json.loads(data)
        finally:
            connection.close()
    ]
]
