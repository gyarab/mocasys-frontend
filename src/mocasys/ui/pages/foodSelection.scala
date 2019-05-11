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
import mocasys.ui.functionComponents._
import mocasys.ui.tables._
import mocasys.ApiClient._

class FoodSelection(val dinerId: Option[Int] = None) extends Component {
    var foodList: Option[Seq[DbRow]] = None
    var balance: String = ""
    var startDate: js.Date = new js.Date()
    var endDate: js.Date = incrDate(startDate, 6)
    // Prev dates will be set on food list fetch
    var prevStartDate: js.Date = null
    var prevEndDate: js.Date = null

    override def onMount() {
        fetchBalance
        fetchFoodListIfDateDiff
    }

    def id: String = dinerId.getOrElse("session_person_get()").toString

    def fetchBalance() =
        AppState.queryDb(
          s"""SELECT diner_balance(${id})""")
        .onComplete {
            case Success(res) => {
                balance = res.rows.pop.pop.asInstanceOf[String]
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
            }
        }

    def fetchFoodList() =
        // TODO: Cleaner - with / without session_person_get
        AppState.queryDb(
            s"""SELECT fa.day, fa.id_food, f.name, fa.kind,
                fa.option, fc.option as option2, ordered
            FROM food_assignments AS fa
            LEFT JOIN food_choice AS fc ON fa.day = fc.day
                AND fa.kind = fc.kind
                AND fc.id_diner = ${id}
            JOIN food AS f ON f.id = fa.id_food
            WHERE fa.day BETWEEN $$1 AND $$2
            ORDER BY day, fa.kind, option""",
            Seq(isoDate(startDate), isoDate(endDate))
        ).onComplete {
            case Success(res) => {
                foodList = Some(res)
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
            }
        }
    
    def fetchFoodListIfDateDiff =
        if (startDate != prevStartDate || endDate != prevEndDate) {
            prevStartDate = startDate
            prevEndDate = endDate
            fetchFoodList()
        }

    def foodByDate =
        foodList.map(
            _.groupBy(r => r("day").asInstanceOf[String])
            .toList
            .sortBy(_._1))
    
    def renderInfoBar: liwec.VNode =
        div(cls := "firstRow borderRadius boxShadowBig",
            label(cls := "dateStart",
                span(cls := "borderShadowColor3 bgColor2 borderRadius",
                    "Date Start"),
                textInput(isoDate(startDate),
                    { str => startDate =
                        (if (str.isEmpty()) startDate else new js.Date(str))},
                    "date"
                ),
            ),
            label(cls := "dateEnd",
                span(cls := "borderShadowColor3 bgColor2 borderRadius",
                    "Date End"),
                textInput(isoDate(endDate),
                    { str => endDate =
                        (if (str.isEmpty()) endDate else new js.Date(str))},
                    "date"
                ),
            ),
            label(cls := "balance",
                span(cls := "borderShadowColor3 bgColor2 borderRadius balanceLabel", "Balance"),
                span(cls := "balanceValue", (if (balance == "") "N/A" else balance)),
            ),
            dinerId.map(id =>
                label(cls := "balance",
                    span(cls := "borderShadowColor3 bgColor2 borderRadius balanceLabel", "dinerId"),
                    span(cls := "balanceValue", id.toString),
                ),
            )
        )

    def render: liwec.VNode = {
        fetchFoodListIfDateDiff
        return scoped(
            div(cls := "foodSelection",
                renderInfoBar,
                foodByDate.map(fbd =>
                    div(cls := "foodList",
                        fbd.map { case (date, choices) =>
                            new FoodChooser(this, new js.Date(date), choices)
                        }
                    ),
                ),
            )
        )
    }

    cssScoped { import liwec.cssDsl._
        c.foodSelection (
            display := "grid",
            gridTemplateRows := "auto 2em 100%",
            gridRowGap := "0.3em",
            backgroundColor := "white",
            height := "100%",

            c.foodList (
                gridColumn := "1 / 4",
                backgroundColor := "white",
                padding := "0 1.5em 0 1.5em",
                marginTop := "1em",
            ),

            c.firstRow (
                gridColumn := "1 / 4",
                display := "flex",
                flexDirection := "row",
                backgroundColor := "#265976",
                padding := "1.5em 4em",
                color := "white",

                e.span (
                    padding := "4px 6px 2px 6px",
                    marginBottom := "0.3em",
                ),

                c.balanceLabel (textAlign := "right"),
                c.balanceValue (padding := "10px 7px 7px 7px"),

                e.label (
                    display := "flex",
                    flexDirection := "column",
                    marginRight := "0.5em",
                ),
            ),
        )
    }
}
