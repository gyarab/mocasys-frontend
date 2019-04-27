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
                case _ => span(cls := "bgRed borderRadius", loginError)
            }),
            div(cls := "form bgBlueDarker borderRadius", 
                img(cls := "mocasysLogo",
                    src := "/assets/mocasys_logo.svg"),
                label(cls := "username",
                    span(cls := "borderShadowBlue bgBlue borderRadius", "username"),
                    textInput(username, { username = _ }, onKeyupE = onEnter)
                ),
                label(cls := "password",
                    span(cls := "borderShadowBlue bgBlue borderRadius", "password"),
                    textInput(password, { password = _ }, "password", onKeyupE = onEnter),
                ),
                button("Google", cls := "googleButton shadowClick"),
                button("Log In", cls := "submitButton shadowClick", onClick := { e => login(e) }),
            )
        )
    )

    cssScoped { import liwec.cssDsl._
        c.loginForm (
            marginTop := "1em",
            marginLeft := "auto",
            marginRight := "auto",
            width := "50%",
            padding := "15px",
            fontFamily := "Helvetica",
            color := "#f1ffff",
            maxWidth := "760px",
            minWidth := "480px",

            c.form -> (
                display := "grid",
                alignItems := "center",
                justifyItems := "center",
                gridTemplateColumns := "repeat(2, 0.2fr 1fr) 0.2fr",
                gridTemplateRows := "auto 1fr 1fr 20px 1fr 1fr",
                backgroundColor := "#265976",
                padding := "20px",

                c.mocasysLogo -> (
                    gridColumn := "1/6",
                    gridRow := "1",
                    width := "100%",
                ),

                c.username -> (
                    e.span -> (
                        gridRow := "2",
                    ),
                    e.input -> (
                        gridRow := "3",
                        background := "#fff",
                    ),
                ),

                c.password -> (
                    e.span -> (
                        gridRow := "5",
                    ),
                    e.input -> (
                        gridRow := "6",
                        backgroundColor := "#fff",
                    ),
                ),

                e.label (
                    display := "contents",

                    e.input -> (
                        width := "100%",
                        height := "3em",
                        gridColumn := "2",
                    ),

                    e.span -> (
                        gridColumn := "2",
                        padding := "4px 6px 2px 6px",
                        justifySelf := "left",
                        // marginLeft := "12.5%",
                    ),
                ),

                (c.googleButton | c.submitButton) (
                    width := "100%",
                    height := "85%",
                    padding := "0",
                    gridColumn := "4",
                    justifySelf := "center",
                    alignSelf := "end",
                    border := "0",
                    color := "#265976",
                ),

                c.googleButton -> (
                    backgroundImage := "url('/assets/google_logo.svg')",
                    backgroundRepeat :=  "no-repeat",
                    backgroundSize := "1.15em",
                    backgroundPosition := "1.1em center",
                    gridRow := "2 / 4",
                    padding := "0 16%",
                    borderRadius := "3px",
                    fontWeight := "450",
                    fontSize := "1.4em",
                    textAlign := "right",
                    fontWeight := "450",
                ),

                c.submitButton (
                    gridRow := "5 / 7",
                    fontSize := "2em",
                    fontWeight := "400",
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
