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
                // div(cls := "tempStatus",
                //     AppState.loggedInUser match {
                //         case None => "Not Logged In"
                //         case Some(s) => s"User: $s"
                //     }
                // ),
                // Do not cite me the old magic, Witch.
                // I was there when it was written.
                new MainMenu(),
                AppState.router.currentComponent,
                button(cls := "scrollToTop showOnScroll", "Up", onClick := { e => {
                    dom.window.scrollTo(0, 0);
                }})
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
            AppState.router.onUrlChangeListeners.append({ () =>
                Component.queueRedraw(root.vm.get)
            })
            // Elements that should be hidden/shown on scroll
            dom.window.addEventListener("scroll", { (e: dom.UIEvent) => 
                val elems = dom.document.querySelectorAll(".showOnScroll")
                if (dom.document.body.scrollTop > 100) {
                    // Show
                    for (elem <- elems) {
                        val e = elem.asInstanceOf[dom.raw.HTMLElement]
                        e.style.opacity = "1"
                    }
                } else {
                    // Hide
                    for (elem <- elems) {
                        val e = elem.asInstanceOf[dom.raw.HTMLElement]
                        e.style.opacity = "0"
                    }
                }
            })
        }
    }

    def incrDate(date: js.Date, offsetDays: Integer = 1): js.Date = {
        val newDate = new js.Date(date.getTime())
        newDate.setDate(newDate.getDate() + offsetDays)
        return newDate
    }
}
