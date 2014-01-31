@[|------------------------------------------------------------------------------------------
    Higher attributes for object construction
   ------------------------------------------------------------------------------------------|]

@MACRO::Attributes
    [|@OR
    [|@VAL::object[|List(@REP(, )::attributes[|"@VAL::name"|])|]|]
    [|[]|]|]

@[|------------------------------------------------------------------------------------------
    Service and method parameters
   ------------------------------------------------------------------------------------------|]

@MACRO::ParameterNames
    [|@REP::params[|, @VAL::name|]|]

@[|------------------------------------------------------------------------------------------
     Path transformed using string interpolation and Path variables
   ------------------------------------------------------------------------------------------|]

@MACRO::PathAsString
    [|"/@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

@MACRO::PathVariable
    [|@REP(, )::values[|@OPT[|List("@VAL::object"@REP::fields[|, "@VAL"|])|]|]|]

@MACRO::PathVariables
    [|List(@USE::PathVariable)|]

@[|------------------------------------------------------------------------------------------
    Main for services generation
   ------------------------------------------------------------------------------------------|]
//------------------------------------------------------------------------------------------
// Services:@REP(, )::services[|@VAL::name|]
//------------------------------------------------------------------------------------------

@OPT[|package @USE::package|]

import scala.util.{Try, Success}

import @OPT[|@USE::package.|]core.BasicService
import @OPT[|@USE::package.|]core.JSon

@REP::services[|
//------------------------------------------------------------------------------------------
// Service @VAL::name
//------------------------------------------------------------------------------------------

class @VAL::name[|@VALService|](override val url:String, parameters:JSon*) extends BasicService {

  val path: String = @VAL::route::path[|getPath(mergeData(parameters.toList).get, @USE::PathAsString, @USE::PathVariables).get|]

  //
  // Public behaviors
  //
  @REP(  )::entries[|
  def @VAL::name(@VAL::signature::inputs[|@REP(, )[|@VAL::name: JSon|])|]: Try[JSon] = {
    (for (data <- mergeData(List(@VAL::signature::inputs[|@REP(, )[|@VAL::name|]|]) ++ parameters.toList);
          path <- @OR[|@VAL::path[|getPath(data, @USE::PathAsString, @USE::PathVariables)|]|][|Success("")|])
    yield httpRequest(path, "@VAL::operation", @OR[|@VAL::body[|Some(getValue(data, @USE::Attributes).get)|]|][|None|],@OR[|@VAL::header[|Some(getValue(data, @USE::Attributes).get)|]|][|None|])) flatMap {
      result => @OR[|@VAL::result[|getValue(data, @USE::Attributes)|]|][|result|]
    }
  }
|]
}

object @VAL::name[|@VALService|] {
  def apply(url:String): (@VAL::route[|@REP(, )::params[|JSon|]|]) => BasicService = (@VAL::route[|@REP(, )::params[|@VAL::name: JSon|]|]) => new @VAL::name[|@VALService|](url@VAL::route[|@USE::ParameterNames|])
}
|]
