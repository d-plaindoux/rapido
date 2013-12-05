package smallibs.rapido.ast

sealed trait Operation

case object GET extends Operation

case object POST extends Operation

case object PUT extends Operation

case object DELETE extends Operation
