package mocasys.ui.components

import scala.util.{Success, Failure}
import scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.ext._
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ui.components._
import mocasys.ui.main.textInput
import mocasys.ui.tables._
import mocasys.ApiClient._

class FoodList extends Component {
    def render = scoped(
        div(cls := "foodList",
            h2("25.4.2019 - 27.4.2019"),
            div(cls := "list",
                new Food(new js.Date(), true),
                new Food(new js.Date(2019, 3, 26)),
                new Food(new js.Date(2019, 3, 27)),
            )
        )
    )

    cssScoped { import liwec.cssDsl._
        c.foodList -> (
            height := "100%",

            c.list -> (
                padding := "1.5em",
            ),
        )
    }
}
