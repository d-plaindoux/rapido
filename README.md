rAPIdo
======

A rest (and may be more) API design tool

## Introduction

The *rAPIdo* project proposes a specific declarative language dedicated to client side
rest service specification. Such specification describes:
- the resource path
- the method i.e. `GET`, `HEAD` `POST`, `PUT` and `DELETE`
- the input type
- the output type

Each service can be viewed as a function applied to a triplet (path,method,input)
and produces an output as a result. Such design gives a language independent formalism
which can be generated to chosen targeted languages.

```
type Error = { @get code:Int, @get reason:String }
type Address = { @{get,set} address:String? }
type Place = Address with { @{get,set} name:String }
type Empty = {}

service places [places] {
	list:         => Place* or Error = GET
	create: Place => Place or Error  = POST BODY[Place]
}

service place(Place) [places/<name>] {
   	get:            => Place or Error = GET
   	update: Address => Empty or Error = PUT BODY[Address]
   	delete:         => Empty or Error = DELETE
}

client placesRest provides places, place
```

Once such specification is done client API can be automatically generated targeting languages
like Scala, Java, Javascript, Python etc. 

For more information read the wiki.