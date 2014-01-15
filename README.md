rAPIdo
======

A rest API design tool 

## Introduction

The *rAPIdo* project proposes a specific declarative language dedicated to client side
rest service specification. Such specification describes:
- the resource path
- the method i.e. `GET`, `POST`, `PUT` and `DELETE`
- the input type
- the output type

Each service can be viewed as a function applied to a triplet (path,method,input)
and produces an output as a result. Such design gives a language independent formalism
wich can be generated to multiple targeted languages. 

```
type Error = { code:Int; reason:String }
type Address = { address:String? }
type Place = Address with { name:String }
type Places = Place*
type Empty = {}

service places {
	list:         => Places = GET
	create: Place => Place or Error = POST
}

service place {
   	get:            => Place or Error = GET
   	update: Address => Empty or Error = PUT
   	delete:         => Empty or Error = DELETE
}

route places         [/places]
route place(p:Place) [/places/<p.name>]

client placesRest provides places, place
```

Once such specification is done client API can be automatically generated targeting languages
like Scala, Java, Javascript, Python etc. 

#### Python

For instance based on the previous declaration a `python` example can be proposed.

``` python
# Create the service defining the rest root path
client = placesRest("at.home:1337")

# Retrieve all place names
allPlaces = client.places().list()

# Create one element
aPlace = client.places().create(name="Eat at Joe's", address="Somewhere ...")

# Update it ...
client.place(aPlace).update(address="A new address for Eat at Joe's")

# Delete it ...
client.place(aPlace).delete()

# Delete all ...
[ client.place(aPlace).delete() for name in allPlaces ]
```

#### Scala 

Same example in `scala` (may be we have to rely on asynchronous layer in order
to have a better integration with reactive programming approach).

``` scala
// Create the service defining the rest root path
val client = placesRest("at.home:1337")

// Retrieve all place names
val allPlaces = client.places().list()

// Create one element
val aPlace = client.places().create("name" -> "Eat at Joe's", "address" -> "Somewhere ...")

// Update it ...
client.place(aPlace).update("address" -> "A new address for Eat at Joe's")

//Delete it ...
client.place(aPlace).delete()

//Delete all ...
for(aPlace <- allPlaces) yield client.place(aPlace).delete()
```

#### Javascript

Same example in `javascript`.

``` javascript
// Create the service defining the rest root path
var client = placesRest("at.home:1337");

// Retrieve all place names
var allPlaces = client.places().list();

// Create one element
var aPlace = client.places().create({name:"Eat at Joe's", address: "Somewhere ..."});

// Update it ...
client.place(aPlace).update({address: "A new address for Eat at Joe's"});

//Delete it ...
client.place(aPlace).delete();

//Delete all ...
allPlaces.forEach(function(aPlace) {
    client.place(aPlace).delete()
});
```
