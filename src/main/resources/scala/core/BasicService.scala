//
// Core service
//

// from @OPT[|@USE::package.|]core

package @OPT[|@USE::package.|] core

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait BasicService {
  val url: String
  val path: String

  //
  // Public behaviors
  //

  def http_request(path: String, operation: String, body: Option[Any], header: Option[Any], implicit_header: Option[Any]): Try[Any] =
    ???

  def get_path(data: JSon, pattern: String, attributes: List[List[String]]): Try[String] = {
    (attributes map (data getValue (_))).foldRight[Try[List[JSon]]](Success(Nil)) {
      case (Success(e), Success(l)) => Success(e :: l)
      case (Failure(e), _) => Failure(e)
      case (_, f@Failure(e)) => f
    } map {
      pattern.format(_)
    }
  }

}