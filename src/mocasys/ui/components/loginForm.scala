package mocasys.ui.components

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import scalajs.js
import scalajs.js.annotation._
import org.scalajs.dom
import org.scalajs.dom.ext._
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ui.functionComponents._
import mocasys.ApiClient._

class LoginForm(var username: String = "", var password: String = "",
                    val onLogin: Option[() => Unit] = None)
            extends Component {
    var loginError: String = ""

    def login(e: dom.Event) =
        // TODO: Set the button active
        if (password.length > 0 && username.length > 0)
            AppState.loginWithPassword(username, password)
            .onComplete {
                case Success(_) => loginError = "Success!"
                case Failure(e) => {
                    val ApiError(status, message) = e
                    loginError = s"Unknown error: ${message}"
                    if (status == 400)
                        loginError = "Invalid username or password"
                }
            }
        else
            loginError = "Please enter both username and password"

    def onEnter(e: dom.KeyboardEvent) = if (e.keyCode == 13) login(e)

    def render() = scoped(
        div(cls := "loginForm borderRadius",
            errorBox(loginError),
            div(cls := "form bgColor1 borderRadius",
                img(cls := "mocasysLogo",
                    src := "/assets/mocasys_logo_trans.svg"),
                label(cls := "username",
                    span(cls := "borderShadowColor3 bgColor2 borderRadius", "username"),
                    textInput(username, { username = _ }, onKeyupE = onEnter)
                ),
                label(cls := "password",
                    span(cls := "borderShadowColor3 bgColor2 borderRadius", "password"),
                    textInput(password, { password = _ }, "password", onKeyupE = onEnter),
                ),
                button("Google", cls := "googleButton shadowClick",
                        onClick := { e => dom.window.alert("Not Yet Implemented!") }),
                button("Log In", cls := "submitButton shadowClick",
                        onClick := { e => login(e) }),
            )
        )
    )

    //TODO: import this default colorTheme to css
    //val color1 : String = "#265976" //dark blue
    //val color2 : String = "#3685a2" //light blue
    //val color3 : String = "#3ea7b9" //turquoise
    //val color4 : String = "#f1ffff" //white
    //val color5 : String = "#ff9b20" //orange
    //val color6 : String = "#d23a3f" //red

    cssScoped { import liwec.cssDsl._
        c.loginForm (
            marginTop := "1em",
            marginLeft := "auto",
            marginRight := "auto",
            width := "90%",
            padding := "15px",
            maxWidth := "760px",
            minWidth := "200px",

            c.errorMessage (height := "2.4em"),

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
                    'logo logo'
                    'usernameLabel usernameLabel'
                    'usernameInput googleButton'
                    'passwordLabel passwordLabel'
                    'passwordInput loginButton'
                """,

                c.mocasysLogo (
                    gridArea := "logo",
                    marginBottom := "20px",
                    width := "100%",
                ),

                c.username (
                    e.span (
                        gridArea := "usernameLabel",
                    ),
                    e.input (
                        gridArea := "usernameInput",
                    ),
                ),

                c.password (
                    e.span (
                        gridArea := "passwordLabel",
                    ),
                    e.input (
                        gridArea := "passwordInput",
                    ),
                ),

                e.label (
                    display := "contents",

                    e.input (
                        width := "100%",
                    ),

                    e.span (
                        padding := "4px 6px 2px 6px",
                        justifySelf := "left",
                        // marginLeft := "12.5%",
                    ),
                ),

                (c.googleButton | c.submitButton) (
                    width := "100%",
                    maxWidth := "10em",
                    justifySelf := "right",
                    alignSelf := "end",
                    border := "0",
                    padding := "10px",
                ),

                c.googleButton (
                    gridArea := "googleButton",
                    backgroundImage := "url('/assets/google_logo.svg')",
                    backgroundRepeat :=  "no-repeat",
                    backgroundSize := "1.15em",
                    backgroundPosition := "1.1em center",
                    borderRadius := "3px",
                    textAlign := "center",
                ),

                c.submitButton (
                    gridArea := "loginButton",
                ),
            ),
        )
    }
}
