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
    }

    val queryUser = """
        SELECT p.name, u.username, p.birth_date, d.account_balance FROM people AS p                                  
            INNER JOIN users AS u ON u.id_person = p.id
            JOIN diners AS d ON d.id_person = p.id
        WHERE p.id = session_person_get();
        """

    def fetchUser =
        AppState.apiClient.queryDb(queryUser)
        .onComplete {
            case Success(res) => userData = Some(res(0))
            case Failure(e) => val ApiError(_, msg) = e
        }
    
    def renderProfile =
        div(cls := "box profile bgColor1 borderRadius boxShadowBalanced",
            if (userData != None)
                userData.get.map { case (key, value) =>
                    div(cls := "dataRow",
                        p(key.toString),
                        p(cls := "value", value.toString))
                }
            else
                p("Loading...")
        )

    def render: liwec.VNode = {
        return scoped(div(cls := "dinerProfile",
            h1(AppState.loggedInUser.getOrElse("").toString),
            div(cls := "grid",
                renderProfile,
                div(cls := "box_1_3",
                    new FoodDayDisplay(new js.Date("2019-05-01T03:00:00"))
                ),
                div(cls := "box_2_3",
                    new FoodDayDisplay(new js.Date("2019-05-09T03:00:00")),
                ),
            )
        ))
    }

    cssScoped { import liwec.cssDsl._
        c.dinerProfile (
            width := "80%",
            marginTop := "5em",
            marginLeft := "auto",
            marginRight := "auto",

            e.h1 (
                fontSize := "20pt",
                textDecoration := "underline",
            ),

            c.grid (
                display := "grid",
                gridGap := "2em",
                gridTemplateColumns := "2fr 2fr 3fr",
                gridTemplateRows := "repeat(3, 1fr)",

                c.box (
                    padding := "0.5em 1em",
                    color := "white",
                    minHeight := "8em",
                ),

                c.box_1_3 (
                    gridRow := "1",
                    gridColumn := "3",
                ),

                c.box_2_3 (
                    gridRow := "2",
                    gridColumn := "3",
                ),

                c.profile (
                    gridRow := "1 / 3",
                    gridColumn := "1 / 3"
                ),

                c.dataRow (
                    display := "grid",
                    gridTemplateColumns := "auto auto",

                    c.value (
                        fontWeight := "bold",
                        justifySelf := "end"
                    ),
                ),
            ),
        )
    }
}