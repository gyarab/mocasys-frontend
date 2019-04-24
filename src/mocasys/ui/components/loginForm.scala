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
import mocasys._
import mocasys.ui.main.textInput

class LoginForm(var username: String = "", var password: String = "",
                    val onLogin: Option[() => Unit] = None)
            extends Component {
    var loginError: String = ""

    def render() = scoped(
        div(cls:="loginForm",
            h2("Login"),
            div(cls := "error", loginError match {
                case "" => loginError
                case _ => p(loginError)
            }),
            label(span("Username: "),
                textInput(username, { username = _ })),
            label(span("Password: "),
                textInput(password, { password = _ }, "password")),
            button("Login", cls := "submitButton", onClick := { e =>
                if (password.length > 0 && username.length > 0) {
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
                } else {
                    loginError = "Please enter both username and password."
                }
            }),
        )
    )

    cssScoped { import liwec.cssDsl._
        c.loginForm (
            display := "grid",
            gridTemplateColumns := "auto auto",
            maxWidth := "20em",
            marginTop := "3em",
            marginLeft := "auto",
            marginRight := "auto",
            backgroundColor := "#eeeeee",
            padding := "15px",
            borderRadius := "10px",

            (c.error | e.h2) -> (
                gridColumn := "1 / 3",
            ),

            c.error -> (
                maxHeight := "3em",
                minHeight := "3em",
            ),

            c.error / e.p -> (
                padding := "4px",
                backgroundColor := "#ff9966",
            ),

            e.label (
                display := "contents",

                e.span (
                    alignSelf := "center",
                ),
            ),

            c.submitButton (
                gridColumn := "1 / 3",
                justifySelf := "right",
                width := "10em",
                marginTop := "1em",
            ),
        )
    }
}