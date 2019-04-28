package mocasys.ui.components

import scala.collection.mutable.ListBuffer
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

class FoodList(val start: js.Date, val nDays: Integer, val foodList: js.Array[js.Array[js.Any]]) extends Component {
    var error = ""
    var shouldFetch: Boolean = true

    def splitByDate: Seq[Seq[Seq[Any]]] = {
        val rows = parseQdbRows(foodList)
        println("1")
        val output = new ListBuffer[Seq[Seq[Any]]]()
        println("2")
        println(rows)
        var currentDate = rows(0)(0)
        println("3")
        var sameDate = new ListBuffer[Seq[Any]]()
        println("4")
        for (row <- rows) {
            println(row(0), currentDate)
            if (row(0) == currentDate) {
                sameDate += row
            } else {
                output += sameDate.toList
                currentDate = row(0)
                sameDate = new ListBuffer[Seq[Any]]()
                sameDate += row
            }
        }
        output += sameDate.toList
        println(output)
        return output.toList
    }

    def render: liwec.VNode = {
        return scoped(
            div(cls := "foodList",
                div(cls := "list",
                    if (foodList.length > 0) {
                        for (_ <- 0 to 3) yield new Food(null),
                    } else {
                        for (food <- splitByDate) yield new Food(food),
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
