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
import mocasys.ui.main.textInput

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
                    // Compiler cries without the cast
                    val response = e.asInstanceOf[AjaxException]
                    // TODO: Change this once json parsing for errors is done
                    val json = js.JSON.parse(response.xhr.responseText)
                    var message = s"Unknown error: ${json.message}"
                    if (response.xhr.status == 400) {
                        message = "Invalid username or password"
                    }
                    loginError = message
                }
            }
        else
            loginError = "Please enter both username and password"

    def onEnter(e: dom.KeyboardEvent) = if (e.keyCode == 13) login(e)

    def render() = scoped(
        div(cls := "loginForm borderRadius",
            div(cls := "error", loginError match {
                case "" => loginError
                case _ => span(cls := "bgColor6 borderRadius", loginError)
            }),
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
                button("Google", cls := "googleButton shadowClick"),
                button("Log In", cls := "submitButton shadowClick", onClick := { e => login(e) }),
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
            color := "#f1ffff",
            maxWidth := "760px",
            minWidth := "200px",

            c.form -> (
                display := "grid",
                alignItems := "center",
                justifyItems := "center",
                backgroundColor := "#265976",
                padding := "20px",

                gridTemplateColumns := "repeat(2, 50fr)",
                gridGap := "5px 10px",
                gridTemplateAreas := """
                    'logo logo'
                    'usernameLabel usernameLabel'
                    'usernameInput googleButton'
                    'passwordLabel passwordLabel'
                    'passwordInput loginButton'
                """,

                c.mocasysLogo -> (
                    gridArea := "logo",
                    marginBottom := "20px",
                    width := "100%",
                ),

                c.username -> (
                    e.span -> (
                        gridArea := "usernameLabel",
                    ),
                    e.input -> (
                        gridArea := "usernameInput",
                        background := "#fff",
                    ),
                ),

                c.password -> (
                    e.span -> (
                        gridArea := "passwordLabel",
                    ),
                    e.input -> (
                        gridArea := "passwordInput",
                        backgroundColor := "#fff",
                    ),
                ),

                e.label (
                    display := "contents",

                    e.input -> (
                        width := "100%",
                    ),

                    e.span -> (
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
                    color := "#265976",
                ),

                c.googleButton -> (
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

            c.error -> (
                marginTop := "2em",
                marginBottom := "1em",
                height := "2.4em",
            ),

            c.error / e.span -> (
                display := "block",
                marginLeft := "4em",
                marginRight := "4em",
                padding := "10px 0px 8px",
                textAlign := "center",
            ),
        )
    }
}
