import scala.concurrent.ExecutionContext.Implicits.global
import liwec.Watched

package object mocasys {
    class AppStateCls extends Watched {
        val apiClient = new ApiClient(Config.middleendApiUrl)
        var loggedInUser: Option[String] = None

        def loginWithPassword(username: String, password: String) = {
            // Fetch user info into an object eventually
            apiClient.loginWithPassword(username, password)
            .map { _ => this.loggedInUser = Some(username) }
        }
    }

    /** An object holding all global state for the web app. All global mutable
     *  state is evil, except for the state in this object ;) */
    lazy val AppState = new AppStateCls().createSetProxy()
}
