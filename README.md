rapido
======

A restlet API design tool 

## Introduction

The *rAPIdo* project proposes a specific declarative language dedicated client side
rest service specification. Such specification describes:
- the resource path
- the method i.e. `GET`, `POST`, `PUT` and `DELETE`
- the input type i.e. JSON or ...
- the output type i.e. JSON or ...

Each service can be viewed as a function applied to a triplet (path,method,input)
and produces an output as a result.

```
type Address { path:String }
type Place extends Address { name:String }
type Empty {}

service places {
    GET                 => List[Place]
    POST    Place       => Place | Error
}

service template {
    GET                 => Place | Error
    PUT     Place       => Place | Error
    DELETE              => Empty | Error
}

route templates [/templates]
route template  [/templates/<id:String>]
```

## Python API

Based on the previous declaration a `python` example can be proposed.

``` python
# Create the service defining the rest root path
services = service.get("http://myserver/rest");

# Retrieve all elements
descriptions = [ element.get() for element in service.templates.get() ]

# Create one element
template service.templates.post({path:"a/b/c"})

[ element.get() for element in service.templates.delete() ]
```
