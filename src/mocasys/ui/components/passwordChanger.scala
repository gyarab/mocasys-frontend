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

// TODO: Hide by default
// TODO: Show when called upon
// TODO: Close button
class PasswordChanger extends Component {
    var show: Boolean = false
    var username: String = ""
    var currentPassword: String = ""
    var newPassword: String = ""
    var newPasswordConfirm: String = ""
    var error: String = ""

    def visible(v: Boolean) = {
        println(v)
        show = v
    }

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
            case Success(_) => error = "Success"
            case Failure(e) => {
                val ApiError(status, message) = e
                error = s"Unknown error: ${message}"
                if (status == 400)
                    error = "Invalid username or password"
            }
        }
    }

    def onEnter(e: dom.KeyboardEvent) =
        if (e.keyCode == 13) changePassword(e)

    def render = scoped(div(cls := "passwordChanger borderRadius" + (if (show) " show" else ""),
        div(cls := "form bgColor1 borderRadius",
            errorBox(error),
            label(cls := "username",
                span(cls := "borderShadowColor3 bgColor2 borderRadius", "username"),
                textInput(username, { username = _ }, onKeyupE = onEnter)
            ),
            label(cls := "currentPassword",
                span(cls := "borderShadowColor3 bgColor2 borderRadius", "current password"),
                textInput(currentPassword, { currentPassword = _ },
                    "currentPassword", onKeyupE = onEnter),
            ),
            label(cls := "newPassword",
                span(cls := "borderShadowColor3 bgColor2 borderRadius", "new password"),
                textInput(newPassword, { newPassword = _ },
                    "newPassword", onKeyupE = onEnter),
            ),
            label(cls := "newPasswordConfirm",
                span(cls := "borderShadowColor3 bgColor2 borderRadius", "confirm new password"),
                textInput(newPasswordConfirm, { newPasswordConfirm = _ },
                    "newPasswordConfirm", onKeyupE = onEnter),
            ),
            button("Change", cls := "submitButton shadowClick",
                    onClick := { e => changePassword(e) }),
        )
    ))

    cssScoped { import liwec.cssDsl._
        c.passwordChanger (
            // display := "none",
            position := "absolute",
            top := "10em",
            left := "0",
            right := "0",
            margin := "4em auto 0 auto",
            width := "90%",
            color := "#f1ffff",
            maxWidth := "760px",
            minWidth := "200px",
            zIndex := "5",

            c.form (
                display := "grid",
                alignItems := "center",
                justifyItems := "center",
                backgroundColor := "#265976",
                padding := "20px",
                boxShadow := "0px 0px 10px 0px rgba(0, 0, 0, 0.60)",

                gridTemplateColumns := "repeat(2, 50fr)",
                gridGap := "5px 10px",
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
                    maxWidth := "10em",
                    justifySelf := "right",
                    alignSelf := "end",
                    border := "0",
                    padding := "10px",
                ),
            ),
        )

        c.show (
            display := "block",
        )
    }
}
