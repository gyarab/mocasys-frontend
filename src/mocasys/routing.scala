package mocasys.routing

import liwec._
import liwec.routing._
import mocasys.ui.pages._

class AppRouter extends Router {
    override def matchUrl(url: String) = {
        url match {
            case path"/" => new LoginPage()
            case path"/users" => new UsersPage()
            case path"/foods" => new FoodSelection()
            case path"/profile" => new DinerProfilePage()
            case path"/diners" => new DinersPage()
            case path"/food-assignment" => new FoodAssignmentPage()
            case path"/drag" => new FoodAssignmentPageProofOfConcept()
            case _ => new NotFoundPage()
        }
    }
}
