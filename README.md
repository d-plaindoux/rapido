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
type Error = { code:Int, reason:String }
type Address = { address:String? }
type Place = Address with { name:String }
type Empty = {}

service places {
	list:         => Place* or Error = GET
	create: Place => Place or Error  = POST BODY[Place]
}

service place {
   	get:            => Place or Error = GET
   	update: Address => Empty or Error = PUT BODY[Address]
   	delete:         => Empty or Error = DELETE
}

route places         [places]
route place(p:Place) [places/<p.name>]

client placesRest provides places, place
```

Once such specification is done client API can be automatically generated targeting languages
like Scala, Java, Javascript, Python etc. 

For more information read the wiki.