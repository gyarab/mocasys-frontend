package mocasys.routing

import liwec._
import liwec.routing._
import mocasys.ui.pages._

class AppRouter extends Router {
    override def matchUrl(url: String) = {
        url match {
            case path"/" => new LoginPage()
            case path"/users" => new UsersPage()
            case path"/food-selection" => new FoodSelection()
            case path"/food-assignment" => new FoodAssignmentPage()
            case path"/profile" => new DinerProfilePage()
            case path"/diners" => new DinersPage()
            case path"/food" => new FoodPage()
            case _ => new NotFoundPage()
        }
    }
}
