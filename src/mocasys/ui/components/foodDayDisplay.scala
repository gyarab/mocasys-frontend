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
import mocasys.ui.main._
import mocasys.ui.components._
import mocasys.ui.tables._
import mocasys.ui.forms._
import mocasys.ApiClient._

class FoodDayDisplay(val date: js.Date = new js.Date()) extends Component {
    var meals: Option[Seq[DbRow]] = None

    override def onMount = {
        fetchMeals
    }

    def queryMeal(date: js.Date) = s"""
        SELECT fa.kind,
            COALESCE(
                (
                    SELECT fc.option FROM food_choice AS fc
                    WHERE id_diner = session_person_get()
                        AND fc.kind = fa.kind
                        AND day = '${isoDate(date)}'
                ),
                (
                    SELECT fa2.option FROM food_assignments AS fa2
                    -- TODO: Some kind of ordering for defaults
                    WHERE day = '${isoDate(date)}' AND fa2.kind = fa.kind
                    ORDER BY fa2.option
                    LIMIT 1
                )
            ) AS option
        FROM food_assignments AS fa
        WHERE fa.day = '${isoDate(date)}'
        GROUP BY fa.kind;
        """

    def fetchMeals =
        AppState.apiClient.queryDb(queryMeal(this.date))
        .onComplete {
            case Success(res) => meals = {
                println(isoDate(this.date))
                println(this.date)
                println(res(0))
                Some(res)
            }
            case Failure(e) => val ApiError(_, msg) = e
        }

    def render = scoped(
        div(cls := "box meals borderRadius boxShadowBalanced",
            if (meals != None)
                div(
                    div(cls := "boxHeader bgColor1",
                        h3(date.toDateString),
                        button("Select Future Meals",
                            cls := "shadowClick",
                            onClick := { _ => AppState.router.goToUrl("foods") }),
                    ),
                    div(cls := "boxBody",
                        table(
                            thead(tr(
                                meals.get.head.map { case (key, _) =>
                                    td(key.toString.capitalize),
                                }
                            )),
                            tbody(meals.get.map(meal =>
                                tr(meal.map { case (_, value) =>
                                    td(
                                        if (value == null)
                                            None
                                        else
                                            value.toString
                                    )
                                })
                            ))
                        ),
                    ),
                )
            else
                div(
                    div(cls := "boxHeader bgColor1",
                        h3(),
                    ),
                    div(cls := "boxBody"),
                )
        )
    )

    cssScoped { import liwec.cssDsl._
        c.meals (
            color := "white",
            minWidth := "26em",
            borderTop := "3px solid #3ea7b9",

            c.boxHeader (
                padding := "0.3em 1em 0 1em",
                position := "relative",

                e.h3 (
                    display := "inline-block",
                ),

                e.button (
                    position := "absolute",
                    top := "0.55em",
                    right := "0.5em",
                ),
            ),

            c.boxBody (
                padding := "0.5em",
            ),

            e.table (
                color := "black",
                width := "100%",

                (e.thead / e.td) (
                    fontWeight := "bold",
                    paddingBottom := "0.3em",
                ),
            ),
        ),
    }
}
