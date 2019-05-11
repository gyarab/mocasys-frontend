import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import org.scalajs.dom.window._
import liwec.Watched
import mocasys.routing.AppRouter
import mocasys.ApiClient._
import mocasys.ui.components._

package object mocasys {
    class AppStateCls extends Watched {
        val apiClient = new ApiClient(Config.middleendApiUrl)
        val router = new AppRouter()
        val messenger: Messenger = new Messenger()
        var _loggedInUser: Option[String] = None
        var _permissions: Option[js.Array[String]] = None

        apiClient.authToken = Option(localStorage.getItem("apiAuthToken"))

        def rerender() = {
            for(f <- this.changeCallbacks) f(this)
        }

        // TODO: Find a way to refactor this
        def loggedInUser: Option[String] = {
            if (this._loggedInUser == None) {
                // We need to be careful with assignments, because each will
                // fire onChange events, even if the value is the same
                Option(localStorage.getItem("username")) match {
                    case username @ Some(_) => this._loggedInUser = username
                    case _ => {}
                }
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

        def reportQueryDbError[T](f: Future[T]) =
            f.transform {
                case f @ Failure(e) => {
                    val ApiError(_, msg) = e
                    this.messenger.addMessage(
                        new ErrorMessage(s"Error querying database: $msg"))
                    f
                }
                case s => s
            }

        def queryDb(query: String, params: Seq[Any] = Seq()) =
            reportQueryDbError(this.apiClient.queryDb(query, params))

        def multiQueryDb(query: String, params: Seq[Any] = Seq()) =
            reportQueryDbError(this.apiClient.multiQueryDb(query, params))
    }

    /** An object holding all global state for the web app. All global mutable
     *  state is evil, except for the state in this object ;) */
    lazy val AppState = new AppStateCls().createSetProxy()
}
