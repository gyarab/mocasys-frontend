package mocasys.ui

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import scalajs.js
import scalajs.js.annotation._
import org.scalajs.dom
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import mocasys._

package object main {
    def textInput(strValue: String,
                  onChange: String => Unit,
                  typ: String = "text") =
        input(typeAttr:=typ, value:=strValue, onInput:={
            e => onChange(e.target.asInstanceOf[dom.raw.HTMLInputElement].value)
        })

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
                            case Failure(e) => loginError = s"Failed logging in: $e"
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

                (c.error | e.h2) -> (
                    gridColumn := "1 / 3",
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
                ),
            )
        }
    }

    class LoginPage extends Component {
        def render() = div(new LoginForm())
    }

    class PageRoot extends Component {
        def render() =
            div("Hello, world",
                div("Currently logged in: " +
                    AppState.loggedInUser.getOrElse("")),
                AppState.router.currentComponent,
            )
    }

    @JSExportTopLevel("MocasysWeb")
    object MocasysWeb extends js.Object {
        def initApp() = {
            val root = new PageRoot()
            liwec.domvm.mountComponent(
                dom.document.querySelector("body"),
                root)
            // Redraw the whole app when the global state changes
            AppState.onChange { _ => Component.queueRedraw(root.vm.get) }
        }
    }
}
