package mocasys.routing

import liwec._
import liwec.routing._

class AppRouter extends Router {
    override def matchUrl(url: String) = {
        import mocasys.ui.main.{LoginPage, UsersPage}
        url match {
            case path"/" => new LoginPage()
            case path"/users" => new UsersPage()
        }
    }
}
