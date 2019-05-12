package mocasys.routing

import liwec._
import liwec.routing._
import mocasys.ui.pages._
import mocasys.AppState

class AppRouter extends Router {
    override def matchUrl(url: String) = {
        url match {
            case path"/" =>
                AppState.loggedInUser match {
                    case Some(_) => new FoodSelection()
                    case None => new LoginPage()
                }
            case path"/users" => new UsersPage()
            case path"/food-selection" => new FoodSelection()
            case path"/food-assignment" => new FoodAssignmentPage()
            case path"/profile" => new DinerProfilePage()
            case path"/diners" => new DinersPage()
            case path"/food" => new FoodPage()
            case path"/people" => new PeoplePage()
            case _ => new NotFoundPage()
        }
    }
}
