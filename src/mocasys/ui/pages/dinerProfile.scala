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

class DinerProfilePage extends Component {
    var userData: Option[DbRow] = None
    var nextMeals: Option[Seq[DbRow]] = None
    var form: Option[Form] = None
    var date = new js.Date("2019-05-01")

    override def onMount = {
        fetchUser
        fetchNextMeal
    }

    val queryUser = """
        SELECT p.name, u.username, p.birth_date, d.account_balance FROM people AS p                                  
            INNER JOIN users AS u ON u.id_person = p.id
            JOIN diners AS d ON d.id_person = p.id
        WHERE p.id = session_person_get();
        """

    val queryNextMeal = s"""
        SELECT fa.kind as meal, f.name, fc.option
        FROM food_assignments AS fa
        LEFT JOIN food_choice AS fc ON fa.day = fc.day
            AND fa.kind = fc.kind
            AND fa.option = fc.option
            AND fc.id_diner = session_person_get()
        JOIN food AS f ON f.id = fa.id_food
        WHERE fa.day = '${isoDate(date)}';
        """

    def fetchUser =
        AppState.apiClient.queryDb(queryUser)
        .onComplete {
            case Success(res) => userData = Some(res(0))
            case Failure(e) => val ApiError(_, msg) = e
        }
    
    def fetchNextMeal =
        AppState.apiClient.queryDb(queryNextMeal)
        .onComplete {
            case Success(res) => nextMeals = Some(res)
            case Failure(e) => val ApiError(_, msg) = e
        }
    
    def renderProfile =
        div(cls := "box profile bgColor1 borderRadius boxShadowBig",
            if (userData != None)
                userData.get.map { case (key, value) =>
                    div(cls := "dataRow",
                        p(key.toString),
                        p(cls := "value", value.toString))
                }
            else
                p("Loading...")
        )

    def renderNextMeals =
        div(cls := "box nextMeals bgColor2 borderRadius boxShadowBig",
            if (nextMeals != None)
                div(
                    h3(date.toDateString),
                    table(
                        thead(tr(
                            nextMeals.get.head.map { case (key, _) =>
                                td(key.toString.capitalize),
                            }
                        )),
                        tbody(nextMeals.get.map(meal =>
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
                    br(),
                    button("Select Future Meals",
                        cls := "shadowClick",
                        onClick := { _ => AppState.router.goToUrl("foods") }),
                    br(),
                )
            else
                p("Loading...")
        )

    def render: liwec.VNode = {
        return scoped(
            div(cls := "dinerProfile",
                h1(AppState.loggedInUser.getOrElse("").toString),
                renderProfile,
                renderNextMeals,
            )
        )
    }

    cssScoped { import liwec.cssDsl._
        c.dinerProfile (
            width := "80%",
            marginTop := "5em",
            marginLeft := "auto",
            marginRight := "auto",
            display := "grid",
            gridColumnGap := "2em",
            gridTemplateColumns := "repeat(3, 1fr)",

            e.h1 (
                fontSize := "20pt",
                textDecoration := "underline",
            ),

            c.box (
                padding := "0.5em 1em",
                color := "white",
                minHeight := "8em",
            ),

            e.table (
                color := "white",
                width := "100%",

                (e.thead / e.td) (
                    fontWeight := "bold",
                    paddingBottom := "0.3em",
                ),
            ),

            c.profile (gridColumn := "1"),
            c.nextMeals (
                gridColumn := "2",
                
                e.button (margin := "1em 0"),
            ),

            c.dataRow (
                display := "grid",
                gridTemplateColumns := "auto auto",

                c.value (
                    fontWeight := "bold",
                    justifySelf := "end"
                ),
            ),
        )
    }
}
