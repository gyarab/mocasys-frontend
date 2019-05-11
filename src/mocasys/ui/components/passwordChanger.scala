package mocasys.ui.components

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
import mocasys.ui.components._
import mocasys.ui.functionComponents._
import mocasys.ui.main._
import mocasys.ui.tables._
import mocasys.ApiClient._

class PasswordChanger extends Component {
    var username: String = AppState.loggedInUser.getOrElse("")
    var currentPassword: String = ""
    var newPassword: String = ""
    var newPasswordConfirm: String = ""
    var error: String = ""

    def changePassword(e: dom.Event): Unit = {
        if (newPassword != newPasswordConfirm) {
            error = "The new password must be entered twice"
            return
        } else if (newPassword.length < 8) {
            error = "The new password must be longer than 7 characters"
            return
        }
        AppState.changeLoginPassword(username, currentPassword, newPassword)
        .onComplete {
            case Success(_) => {
                error = "The password was successfully changed. You will be logged out now."
                js.timers.setTimeout(3000) {
                    dom.document.getElementById("passwordIDDD")
                        .asInstanceOf[dom.raw.HTMLElement].style.display = "none"
                    AppState.logout
                }
                error = ""
            }
            case Failure(e) => {
                val ApiError(status, message) = e
                error = message
            }
        }
    }

    def onEnter(e: dom.KeyboardEvent) =
        if (e.keyCode == 13) changePassword(e)

    def render = scoped(div(id := "passwordIDDD", cls := "passwordChanger borderRadius",
        div(cls := "form bgColor1 borderRadius",
            errorBox(error),
            label(cls := "username",
                span(cls := "borderShadowColor3 bgColor2 borderRadius", "username"),
                textInput(username, { username = _ }, onKeyupE = onEnter)
            ),
            label(cls := "currentPassword",
                span(cls := "borderShadowColor3 bgColor2 borderRadius", "current password"),
                textInput(currentPassword, { currentPassword = _ },
                    "password", onKeyupE = onEnter),
            ),
            label(cls := "newPassword",
                span(cls := "borderShadowColor3 bgColor2 borderRadius", "new password"),
                textInput(newPassword, { newPassword = _ },
                    "password", onKeyupE = onEnter),
            ),
            label(cls := "newPasswordConfirm",
                span(cls := "borderShadowColor3 bgColor2 borderRadius", "confirm new password"),
                textInput(newPasswordConfirm, { newPasswordConfirm = _ },
                    "password", onKeyupE = onEnter),
            ),
            button("Change", cls := "submitButton shadowClick",
                    onClick := { e => changePassword(e) }),
        ),
        span(cls := "cross", "x",
            onClick := { e => e.target.asInstanceOf[dom.raw.HTMLElement]
                .parentNode.asInstanceOf[dom.raw.HTMLElement].style.display = "none" }),
    ))

    cssScoped { import liwec.cssDsl._
        c.passwordChanger (
            display := "none",
            position := "absolute",
            top := "3em",
            left := "0",
            right := "0",
            margin := "4em auto 0 auto",
            width := "90%",
            color := "#f1ffff",
            maxWidth := "500px",
            minWidth := "200px",
            zIndex := "5",

            c.cross (
                position := "absolute",
                top := "0.3em",
                right := "0.5em",
                fontSize := "1.7em",
                fontWeight := "bold",
                backgroundColor := "#d23a3f",
                borderRadius := "10px",
                padding := "0.15em 0.3em",
            ),

            c.form (
                display := "grid",
                alignItems := "center",
                justifyItems := "center",
                backgroundColor := "#265976",
                padding := "20px",
                boxShadow := "0px 0px 10px 0px rgba(0, 0, 0, 0.60)",

                gridTemplateColumns := "repeat(2, 50fr)",
                gridGap := "5px 0",
                gridTemplateAreas := """
                    'usernameLabel errorMessage'
                    'usernameInput errorMessage'
                    'currentPasswordLabel errorMessage'
                    'currentPasswordInput errorMessage'
                    'newPasswordLabel newPasswordLabel'
                    'newPasswordInput none3'
                    'newPasswordConfirmLabel newPasswordConfirmLabel'
                    'newPasswordConfirmInput loginButton'
                """,

                c.errorMessage (
                    gridArea := "errorMessage",

                    e.span (
                        paddingLeft := "0.5em",
                        paddingRight := "0.5em",
                        marginLeft := "2em",
                        marginRight := "2em",
                    ),
                ),

                c.username (
                    e.span (gridArea := "usernameLabel"),
                    e.input (gridArea := "usernameInput"),
                ),

                c.currentPassword (
                    e.span (gridArea := "currentPasswordLabel"),
                    e.input (gridArea := "currentPasswordInput"),
                ),

                c.newPassword (
                    e.span (gridArea := "newPasswordLabel"),
                    e.input (gridArea := "newPasswordInput"),
                ),

                c.newPasswordConfirm (
                    e.span (gridArea := "newPasswordConfirmLabel"),
                    e.input (gridArea := "newPasswordConfirmInput"),
                ),

                e.label (
                    display := "contents",

                    e.input (width := "100%"),

                    e.span (
                        padding := "4px 6px 2px 6px",
                        justifySelf := "left",
                    ),
                ),
    
                c.submitButton (
                    gridArea := "loginButton",
                    width := "100%",
                    maxWidth := "12em",
                    justifySelf := "right",
                    alignSelf := "end",
                    border := "0",
                    padding := "10px",
                ),
            ),
        )
    }
}
