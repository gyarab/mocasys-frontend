package mocasys.routing

import liwec._
import liwec.routing._
import mocasys.ui.pages._

class AppRouter extends Router {
    override def matchUrl(url: String) = {
        url match {
            case path"/" => new LoginPage()
            case path"/users" => new UsersPage()
            case _ => new NotFoundPage()
        }
    }
}
