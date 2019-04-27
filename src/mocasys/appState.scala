import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.window.localStorage
import liwec.Watched
import mocasys.routing.AppRouter

package object mocasys {
    class AppStateCls extends Watched {
        val apiClient = new ApiClient(Config.middleendApiUrl)
        val router = new AppRouter()
        var _loggedInUser: Option[String] = None

        apiClient.authToken = Option(localStorage.getItem("apiAuthToken"))

        def loggedInUser: Option[String] = {
            if (this._loggedInUser == None) {
                this._loggedInUser = Some(localStorage.getItem("username"))
            }
            return this._loggedInUser
        }

        def loginWithPassword(username: String, password: String) = {
            // Fetch user info into an object eventually
            apiClient.loginWithPassword(username, password)
            .map { resp =>
                localStorage.setItem("apiAuthToken", resp.sessionToken)
                localStorage.setItem("username", username)
                this._loggedInUser = Some(username)
            }
        }
    }

    /** An object holding all global state for the web app. All global mutable
     *  state is evil, except for the state in this object ;) */
    lazy val AppState = new AppStateCls().createSetProxy()
}
