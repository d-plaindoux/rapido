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

@MACRO::ParametersTypes
    [|@REP::params[|, @VAL::name: @VAL::type::name|]|]

@MACRO::ParametersValues
    [|List(@REP(, )::params[|@VAL::name|])|]

@[|------------------------------------------------------------------------------------------
     Path transformed using string interpolation and Path variables
   ------------------------------------------------------------------------------------------|]

@MACRO::PathAsString
    [|"@REP::values[|@OR[|@VAL::name|][|%s|]|]"|]

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


import scala.util.{Try, Success, Failure}
import @OPT[|@USE::package.|]core.BasicService

@REP::services[|
//------------------------------------------------------------------------------------------
// Service @VAL::name
//------------------------------------------------------------------------------------------
class @VAL::name[|@VALService|](override val url: String@VAL::route[|@USE::ParametersTypes|]) extends BasicService {
  @SET::serviceParameters[|@VAL::route[|@USE::ParametersValues|]|]

  val path: String = @VAL::route[|getPath(mergeData(@USE::serviceParameters).get, @VAL::path[|@USE::PathAsString, @USE::PathVariables)|].get|]

  //
  // Public behaviors
  //
  @REP(  )::entries[|
  def @VAL::name(@VAL::signature::inputs[|@REP(, )[|@VAL::name: @VAL::type::name|])|]: Try[@VAL::signature::output::name] = {
    (for (data <- mergeData(List(@VAL::signature::inputs[|@REP(, )[|@VAL::name|]|]) ++ @USE::serviceParameters);
          path <- @OR[|@VAL::path[|getPath(data, @USE::PathAsString, @USE::PathVariables)|]|][|Success("")|]@OR
          [|@VAL::body[|;
          body <- getValue(data, @USE::Attributes)|]|][||]@OR
          [|@VAL::header[|;
          header <- getValue(data, @USE::Attributes)|]|][||])
    yield httpRequest(path, "@VAL::operation", @OR[|@VAL::body[|Some(body)|]|][|None|] ,@OR[|@VAL::header[|Some(header)|]|][|None|])) flatMap {
      case Success(e) => Success(new @VAL::signature::output::name(e))
      case Failure(f) => Failure(f)
    }
  }
|]
}

object @VAL::name[|@VALService|] {
  def apply(url:String): (@VAL::route[|@REP(, )::params[|@VAL::type::name|]|]) => @VAL::name[|@VALService|] = (@VAL::route[|@REP(, )::params[|@VAL::name: @VAL::type::name|]|]) => new @VAL::name[|@VALService|](url@VAL::route[|@USE::ParameterNames|])
}
|]
