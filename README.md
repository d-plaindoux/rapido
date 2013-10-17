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
type Error { code:Int, reason:String }
type Address { address:String }
type Place extends Address { name:String }
type Empty {}

model places {
    list   GET            => Array[Place]
    create POST   Place   => Place or Error
}

model place {
    get    GET            => Place or Error
    update PUT    Address => Place or Error
    delete DELETE         => Empty or Error
}

route places         [/places]
route place(p:Place) [/places/<p.name>]
```

Once such specification is done client API can be automatically generated targeting languages
like Scala, Java, Javascript and Python. 

#### Python

For instance based on the previous declaration a `python` example can be proposed.

``` python
# Create the service defining the rest root path
api = rapido.client("http://at.home:1337/rest");

# Retrieve all place names
allPlaces = api.places.list()

# Create one element
aPlace = api.places.create({"name":"Eat at Joe's","address":"Somewhere ..."})

# Update it ...
aPlace = api.place(aPlace).update({"address":"A new address for Eat at Joe's"})

# Delete it ...
api.place(aPlace).delete()

# Delete all ...
[ api.place(aPlace).delete() for name in allPlaceNames ]
```

#### Scala 

Same example in `scala` ...

``` scala
// Create the service defining the rest root path
val api = rapido.client("http://at.home:1337/rest");

// Retrieve all place names
val allPlaces = api.places.list()

// Create one element
val aPlace = api.places.create({"name":"Eat at Joe's","address":"Somewhere ..."})

// Update it ...
val aPlace = api.place(aPlace).update({"address":"A new address for Eat at Joe's"})

//Delete it ...
api.place(aPlace).delete()

//Delete all ...
for(aPlace <- allPlaceNames) yield api.place(aPlace).delete()
```
