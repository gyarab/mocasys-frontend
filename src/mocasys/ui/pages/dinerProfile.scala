package mocasys.ui.pages

import scala.util.{Success, Failure}
import scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.ext._
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ui.main._
import mocasys.ui.components._
import mocasys.ui.functionComponents._
import mocasys.ui.tables._
import mocasys.ui.forms._
import mocasys.ApiClient._

class DinerProfilePage extends Component {
    var userData: Option[DbRow] = None
    var nextMeals: Option[Seq[DbRow]] = None
    var form: Option[Form] = None
    var error: String = ""

    val passwordChanger = new PasswordChanger()

    override def onMount = {
        fetchUser
    }

    def fetchUser =
        AppState.apiClient.queryDb(
            """SELECT p.name, u.username, p.birth_date,
                      diner_balance(d.id_person) AS balance
               FROM people AS p
               INNER JOIN users AS u ON u.id_person = p.id
               JOIN diners AS d ON d.id_person = p.id
               WHERE p.id = session_person_get()"""
        ).onComplete {
            case Success(res) => userData = Some(res(0))
            case Failure(e) => {
                val ApiError(_, msg) = e
                error = msg
            }
        }
    
    def renderProfile =
        div(cls := "box profile borderRadius boxShadowBalanced",
            (if (userData != None)
                userData.get.map { case (key, value) =>
                    if (key.toString == "name")
                        div(cls := "dataRow firstRow bgColor1",
                            p(key.toString),
                            p(cls := "value", value.toString))
                    else
                        div(cls := "dataRow bgWhite",
                            p(key.toString),
                            p(cls := "value", value.toString))
                }
            else
                p("Loading...")),
            (if (userData != None)
                button("Change Password", cls := "changePassBtn shadowClick",
                    onClick := { e =>
                        e.target.asInstanceOf[dom.raw.HTMLElement]
                            .nextSibling.asInstanceOf[dom.raw.HTMLElement]
                                .style.display = "block"
                    }
                )
            else None),
            passwordChanger,
        )

    def render: liwec.VNode = {
        return scoped(div(cls := "dinerProfile",
            errorBox(error),
            h1(AppState.loggedInUser.getOrElse("").toString,
                cls := "borderRadius boxShadowBalanced bgColor1"),
            div(cls := "grid",
                renderProfile,
                div(cls := "box_1_3",
                    new FoodDayDisplay(new js.Date())
                ),
                div(cls := "box_2_3",
                    new FoodDayDisplay(new js.Date(new js.Date().getTime + 86400000)),
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
                borderTop := "3px solid #3ea7b9",
                width := "max-content",
                padding := "8px 16px",
            ),

            c.grid (
                display := "grid",
                gridGap := "2em",
                gridTemplateColumns := "2fr 2fr 3fr",
                gridTemplateRows := "repeat(3, 1fr)",

                c.box (
                    color := "white",
                    minHeight := "8em",
                    backgroundColor := "#265976",
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
                    padding := "0 1em",

                    c.value (
                        fontWeight := "bold",
                        justifySelf := "end"
                    ),
                ),
                
                c.firstRow (
                    borderTop := "3px solid #3ea7b9",
                    borderTopLeftRadius := "3px",
                    borderTopRightRadius := "3px",
                ),

                c.changePassBtn (
                    margin := "0.5em 1em",
                    padding := "6px 10px",
                ),
            ),
        )
    }
}
