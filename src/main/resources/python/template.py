import httplib as http
import json

@rep:services[
class @ident:name:
    def __init__(self @rep:params[,@ident:name]):
        @rep:params[
        self.@ident:name = @ident:name
        ]

    @rep:entries[
    def @ident:name(self,input=None):
        path = @string:path
        headers = {"Content-Type": "application/json"}
        connection = http.HTTPConnection(url)
        connection.request(@string:operation, path, json.dumps(none), headers)
        try:
            response = connection.getresponse()
            data = response.read()
            dd = json.loads(data)
        finally:
            connection.close()
    ]
]
