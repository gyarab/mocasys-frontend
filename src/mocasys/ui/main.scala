package mocasys.ui.main

import scalajs.js
import scalajs.js.annotation._
import org.scalajs.dom
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._

class PageRoot extends Component {
    def render() =
        div("Hello, world!")
}

@JSExportTopLevel("MocasysWeb")
object MocasysWeb extends js.Object {
    def initApp() = {
        liwec.domvm.mountComponent(dom.document.querySelector("body"), new PageRoot())
    }
}
