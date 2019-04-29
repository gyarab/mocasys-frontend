package mocasys.ui.pages

import scala.collection._
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
    var foodList: Option[Seq[DbRow]] = None
    var balance: String = ""
    var error: String = ""

    override def onMount() {
        fetchBalance()
        fetchFoodList()
    }

    // TODO: Replace with query builder
    val balanceQuery = """
        SELECT account_balance FROM diners
        WHERE id_person = session_person_get();
        """
    def foodListQuery(start: String, end: String) = s"""
        SELECT fa.day, fa.id_food, f.name, fa.kind, fc.kind as kind2
        FROM food_assignments AS fa
        LEFT JOIN food_choice AS fc ON fa.day = fc.day AND fa.kind = fc.kind
        JOIN food AS f ON f.id = fa.id_food
        WHERE fa.day BETWEEN '$start' AND '$end'
        ORDER BY day, fa.kind;
        """

    def fetchBalance() =
        AppState.apiClient.queryDb(balanceQuery)
        .onComplete {
            case Success(res) => {
                balance = res.rows.pop.pop.asInstanceOf[String]
                error = ""
            }
            case Failure(e) => {
                val response = e.asInstanceOf[AjaxException]
                val json = js.JSON.parse(response.xhr.responseText)
                error = json.message.toString()
            }
        }

    def fetchFoodList() =
        AppState.apiClient.queryDb(foodListQuery("2019-04-20", "2019-04-28"))
        .onComplete {
            case Success(res) => {
                foodList = Some(res)
                error = ""
            }
            case Failure(e) => {
                val response = e.asInstanceOf[AjaxException]
                val json = js.JSON.parse(response.xhr.responseText)
                error = json.message.toString()
            }
        }

    def foodByDate =
        foodList.map(
            _.groupBy(r => r("day").asInstanceOf[String])
            .toList
            .sortBy(_._1))

    def render: liwec.VNode = {
        val start: js.Date = new js.Date()
        val nDays: Integer = 7
        val end: js.Date = incrDate(start, nDays - 1)
        return scoped(
            div(cls := "foodSelection",
                h2(s"${start.toDateString()} - ${end.toDateString()}"),
                h3("Balance: " + (if (balance == "") "N/A" else balance)),
                div(cls := "error", error),
                foodByDate.map(fbd =>
                    div(cls := "foodList",
                        fbd.map { case (date, choices) =>
                            new Food(new js.Date(date), choices)
                        }
                    ),
                ),
            )
        )
    }

    cssScoped { import liwec.cssDsl._
        c.foodSelection -> (
            display := "grid",
            backgroundColor := "white",
            height := "100%",
            padding := "1.5em",

            c.foodList -> (
                gridColumn := "1 / 4",
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
