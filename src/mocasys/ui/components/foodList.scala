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
import mocasys.ui.main._
import mocasys.ui.tables._
import mocasys.ApiClient._
import scala.scalajs.js.timers._

class FoodList(val start: js.Date, val nDays: Integer, val foodList: Array[Array[Any]]) extends Component {
    var error = ""
    var shouldFetch: Boolean = true

    def render: liwec.VNode = {
        return scoped(
            div(cls := "foodList",
                div(cls := "list",
                    if (foodList.isEmpty) {
                        for (i <- 0 to 3) yield new Food(null, Array()),
                    } else {
                        for (i <- 0 until nDays) yield new Food(incrDate(start, i), foodList(i)),
                    }
                )
            )
        )
    }

    cssScoped { import liwec.cssDsl._
        c.foodList -> (
            height := "100%",
        )
    }
}
