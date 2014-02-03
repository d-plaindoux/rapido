rAPIdo
======

A rest (and may be more) API design tool

## Quick overview

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
type Places = { @get(values) places : Places* }
type Nothing = {}

service places [places] {
	list:         => Places     = GET
	create: Place => Place      = POST BODY[Place]
}

service place(Place) [places/<name>] {
   	get:            => Place    = GET
   	update: Address => Nothing  = PUT BODY[Address]
   	delete:         => Nothing  = DELETE
}

client placesRest provides places, place
```

Once such specification is done client API can be automatically generated targeting languages
like Scala, Java, Javascript, Python etc. 

## More resources ...

More information are available in the wiki

## License

Copyright (C)2014 D. Plaindoux.

This program is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published
by the Free Software Foundation; either version 2, or (at your option) any
later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; see the file COPYING.  If not, write to
the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
