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
            div(cls := "error", loginError match {
                case "" => loginError
                case _ => span(loginError)
            }),
            div(cls := "form", 
                img(cls := "mocasysLogo", src := "/assets/mocasys_logo.svg"),
                label(cls := "username",
                    span("username"),
                    textInput(username, { username = _ })
                ),
                label(cls := "password",
                    span("password"),
                    textInput(password, { password = _ }, "password")
                ),
                button("Log in", cls := "submitButton", onClick := { e =>
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
            marginTop := "1em",
            marginLeft := "auto",
            marginRight := "auto",
            width := "50%",
            padding := "15px",
            fontFamily := "Helvetica",
            color := "#f1ffff",

            c.form -> (
                display := "grid",
                alignItems := "center",
                justifyItems := "center",
                gridTemplateColumns := "1fr 20px 1fr",
                gridTemplateRows := "auto 1fr 1fr 20px 1fr 1fr",
                backgroundColor := "#265976",
                padding := "20px",
                borderRadius := "3px",

                c.mocasysLogo -> (
                    gridColumn := "1/4",
                    gridRow := "1",
                    width := "100%",
                ),

                c.username -> (
                    e.span -> (
                        gridRow := "2",
                    ),
                    e.input -> (
                        gridRow := "3"
                    ),
                ),

                c.password -> (
                    e.span -> (
                        gridRow := "5",
                    ),
                    e.input -> (
                        gridRow := "6"
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
                    gridRow := "2/6",
                    gridColumn := "3",
                    width := "75%",
                    height :=  "50%",
                    padding := "0",
                    border := "0",
                    borderRadius := "3px",
                    alignSelf := "end",
                    background := "#f1ffff",
                    color := "#265976",
                    fontSize := "2em",
                    fontWeight := "400",
                ),

                RawSelector(".submitButton:hover") -> (
                    backgroundColor := "#ff9b20",
                    boxShadow := "7px 7px 50px 0px rgba(0,0,0,0.75)",
                    transform := "translateY(-8px)",
                ),

                RawSelector(".submitButton:clicked") -> (
                    backgroundColor := "#d23a3f",
                    transform := "translateY(2px)",
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
                backgroundColor := "#d23a3f",
                padding := "10px 0px 8px",
                textAlign := "center",
                borderRadius := "3px",
            ),
        )
    }
}
