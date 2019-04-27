package mocasys.ui.pages

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

class FoodSelection extends Component {
    var balance: String = ""
    var foodList: Array[Array[Any]] = Array[Array[Any]]()
    var error: String = ""
    var shouldFetch: Boolean = true

    // TODO: Replace with query builder
    val balanceQuery = "SELECT account_balance FROM diners;"
    def foodListQuery(start: String, end: String) =
        "SELECT * FROM food_assignments AS fa " +
        "LEFT JOIN food_choice AS fc " +
        "ON fa.day = fc.day AND fa.kind = fc.kind " +
        s"WHERE (fa.day BETWEEN '$start' AND '$end') " +
        "AND fc.id_diner = session_person_get()";

    def fetchBalance = 
        AppState.apiClient.queryDb(balanceQuery)
        .onComplete {
            case Success(res) => {
                shouldFetch = false
                balance = res.rows.pop.pop.asInstanceOf[String]
                error = ""
            }
            case Failure(e) => {
                val response = e.asInstanceOf[AjaxException]
                val json = js.JSON.parse(response.xhr.responseText)
                shouldFetch = false // Let the user decide whether to retry
                error = json.message.toString()
            }
        }

    def fetchFoodList =
        AppState.apiClient.queryDb(foodListQuery("2019-04-20", "2019-04-27"))
        .onComplete {
            case Success(res) => {
                shouldFetch = false
                println(res)
                error = ""
            }
            case Failure(e) => {
                val response = e.asInstanceOf[AjaxException]
                val json = js.JSON.parse(response.xhr.responseText)
                shouldFetch = false // Let the user decide whether to retry
                error = json.message.toString()
            }
        }

    def render: liwec.VNode = {
        val start: js.Date = new js.Date()
        val nDays: Integer = 7
        val end: js.Date = incrDate(start, nDays - 1)
        val balanceStr: String = if (balance == "") "N/A" else s"Balance: $balance"
        if (shouldFetch) {
            fetchBalance
            fetchFoodList
        }
        return scoped(
            div(cls := "foodSelection",
                if (AppState.loggedInUser == None) 
                    p("Login before continuing")
                else
                    h2(s"${start.toDateString()} - ${end.toDateString()}"),
                    h3(balanceStr),
                    div(cls := "foodList", new FoodList(start, nDays, foodList)),
            )
        )
    }

    cssScoped { import liwec.cssDsl._
        c.foodSelection -> (
            display := "grid",
            marginLeft := "14em", // Placeholder
            backgroundColor := "white",
            height := "100%",
            padding := "1.5em",
            
            c.foodList -> (
                padding := "1.5em",
                gridColumnStart := "1",
                gridColumnEnd := "4",
            ),

            e.h2 -> (
                gridColumnStart := "1",
                gridColumnEnd := "2",
                gridRow := "1 / 2",
            ),

            e.h3 -> (
                gridColumn := "3 / 3",
            ),
        )
    }
}
