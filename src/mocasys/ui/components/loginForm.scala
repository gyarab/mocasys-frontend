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

    def render() = scoped(
        div(cls := "loginForm",
            img(cls := "mocasysLogo", src := "/assets/mocasys_logo.svg"),
            div(cls := "error", loginError match {
                case "" => loginError
                case _ => span(loginError)
            }),
            div(cls := "form", 
                img(src := "/assets/google_logo.svg"),
                label(cls := "username",
                    span("username"),
                    textInput(username, { username = _ })
                ),
                label(cls := "password",
                    span("password"),
                    textInput(password, { password = _ }, "password")
                ),
                button("> login", cls := "submitButton", onClick := { e =>
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
                        loginError = "Please enter both username and password"
                    }
                }),
            )
        )
    )

    cssScoped { import liwec.cssDsl._
        c.loginForm (
            marginTop := "4em",
            marginLeft := "auto",
            marginRight := "auto",
            width := "50%",
            padding := "15px",
            fontFamily := "Helvetica",
            color := "#f1ffff",

            e.img -> (
                borderRadius := "3px",
            ),

            c.mocasysLogo -> (
                width := "100%",
            ),

            c.form -> (
                display := "grid",
                alignItems := "center",
                justifyItems := "center",
                gridTemplateColumns := "1fr 1fr",
                gridTemplateRows := "1fr 1fr 20px 1fr 1fr",
                backgroundColor := "#265976",
                padding := "20px",
                borderRadius := "3px",

                e.img -> (
                    gridColumn := "2",
                    gridRowStart := "1",
                    gridRowEnd := "3",
                    width := "3.5em",
                    padding := "0.5em",
                    backgroundColor := "#f1ffff",
                    justifySelf := "right",
                    marginRight := "17.5%",
                ),

                RawSelector("img:hover") -> (
                    backgroundColor := "#ff9b20",
                ),

                c.username -> (
                    e.span -> (
                        gridRow := "1",
                    ),
                    e.input -> (
                        gridRow := "2"
                    ),
                ),

                c.password -> (
                    e.span -> (
                        gridRow := "4",
                    ),
                    e.input -> (
                        gridRow := "5"
                    ),
                ),

                e.label (
                    display := "contents",

                    e.input -> (
                        width := "70%",
                        height := "3em",
                        gridColumn := "1",
                    ),

                    e.span -> (
                        gridColumn := "1",
                        backgroundColor := "#3685a2",
                        padding := "4px 6px 2px 6px",
                        borderRadius := "2px",
                    ),
                ),

                c.submitButton (
                    gridRowStart := "4",
                    gridRowEnd := "6",
                    gridColumn := "2",
                    width := "65%",
                    height := "4em",
                    padding := "0",
                    border := "0",
                    borderRadius := "3px",
                ),

                RawSelector(".submitButton:hover") -> (
                    backgroundColor := "#ff9b20",
                ),
            ),

            c.error -> (
                marginTop := "4em",
                marginBottom := "4em",
                height := "2.4em",
            ),

            c.error / e.span -> (
                display := "block",
                marginLeft := "4em",
                marginRight := "4em",
                backgroundColor := "#d23a3f",
                padding := "10px 0px 8px",
                textAlign := "center",
                borderRadius := "3px",
            ),
        )
    }
}
