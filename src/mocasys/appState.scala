import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import org.scalajs.dom.window._
import liwec.Watched
import mocasys.routing.AppRouter
import scala.util.{Success, Failure}

package object mocasys {
    class AppStateCls extends Watched {
        val apiClient = new ApiClient(Config.middleendApiUrl)
        val router = new AppRouter()
        var _loggedInUser: Option[String] = None
        var _permissions: Option[js.Array[String]] = None

        apiClient.authToken = Option(localStorage.getItem("apiAuthToken"))

        // TODO: Find a way to refactor this
        def loggedInUser: Option[String] = {
            if (this._loggedInUser == None) {
                this._loggedInUser = Option(localStorage.getItem("username"))
            }
            return this._loggedInUser
        }

        def permissions: Option[js.Array[String]] = {
            if (this._permissions == None) {
                this._permissions = Option(
                    js.JSON.parse(localStorage.getItem("permissions"))
                    .asInstanceOf[js.Array[String]]
                )
            }
            return this._permissions
        }

        def loginWithPassword(username: String, password: String) =
            // Fetch user info into an object eventually
            apiClient.loginWithPassword(username, password)
            .map { resp =>
                localStorage.setItem("apiAuthToken", resp.sessionToken)
                localStorage.setItem("username", username)
                this._loggedInUser = Some(username)
                this.router.goToUrl("food-selection")
                this.fetchPermissions
            }

        def changeLoginPassword(username: String,
                                currentPassword: String,
                                newPassword: String) =
            apiClient.changeLoginPassword(username, currentPassword, newPassword)
            .map { resp =>
                println(resp)
            }
        
        def fetchPermissions =
            apiClient.queryDb("SELECT name FROM permissions;")
            .onComplete {
                case Success(res) => localStorage.setItem("permissions",
                    js.JSON.stringify(for (row <- res.rows) yield row(0)))
                case Failure(e) => println(e)
            }

        def logout = {
            localStorage.removeItem("apiAuthToken")
            localStorage.removeItem("username")
            this.apiClient.authToken = None
            this._loggedInUser = None
            this.router.goToUrl("")
        }

    }

    /** An object holding all global state for the web app. All global mutable
     *  state is evil, except for the state in this object ;) */
    lazy val AppState = new AppStateCls().createSetProxy()
}
