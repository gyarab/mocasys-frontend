package mocasys

import scala.util.{Success, Failure}
import scala.scalajs.js
import org.scalajs.dom.ext._
import scala.concurrent.ExecutionContext.Implicits.global

class ApiClient(val apiUrl: String) {
    class PasswordLoginRequest(
            val username: String,
            val password: String,
        ) extends js.Object
    class PasswordLoginResp(val sessionToken: String) extends js.Object

    var authToken: Option[String] = None

    def loginWithPassword(username: String, password: String) = {
        Ajax.post(
            s"$apiUrl/auth/password",
            data = js.JSON.stringify(
                new PasswordLoginRequest(username, password)),
            headers = Map("Content-Type" -> "application/json"))
        .map { xhr =>
            val resp =
                js.JSON.parse(xhr.responseText)
                .asInstanceOf[PasswordLoginResp]
            this.authToken = Some(resp.sessionToken)
        }
    }
}
