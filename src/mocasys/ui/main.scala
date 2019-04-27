package mocasys.ui

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
import mocasys.ui.components._
import mocasys.ui.pages._

package object main {
    def textInput(strValue: String,
                  onChange: String => Unit,
                  typ: String = "text",
                  onKeyupE: dom.KeyboardEvent => Unit = {
                      e => Unit
                  }) =
        input(typeAttr := typ, onKeyup := onKeyupE, value := strValue, onInput := {
            e => onChange(e.target.asInstanceOf[dom.raw.HTMLInputElement].value)
        })

    class PageRoot extends Component {
        def render() =
            div(
                div(cls := "tempStatus",
                    AppState.loggedInUser match {
                        case None => "Not Logged In"
                        case Some(s) => s"User: $s"
                    }
                ),
                AppState.router.currentComponent
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
            // Redraw when the URL changes
            dom.window.addEventListener("popstate", { (_: dom.Event) =>
                Component.queueRedraw(root.vm.get)
            })
        }
    }
}
