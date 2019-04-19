package mocasys.routing

import liwec._
import liwec.routing._

class AppRouter extends Router {
    override def matchUrl(url: String) = {
        import mocasys.ui.main.LoginForm
        url match {
            case path"/" => new LoginForm()
        }
    }
}
