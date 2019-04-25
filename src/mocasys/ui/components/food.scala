package mocasys.ui.components

import scala.util.{Success, Failure}
import scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.ext._
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ui.components._
import mocasys.ui.main.textInput
import mocasys.ui.tables._
import mocasys.ApiClient._

class Food(val selected: Boolean = false) extends Component {
    def render = scoped(
        div(cls := "food" + (if (selected) " selected" else ""),
            p("~data~")
        )
    )

    cssScoped { import liwec.cssDsl._
        c.food -> (
            height := "10em",
            padding := "1em",
            backgroundColor := "white",
            border := "1px solid #d3d3d3",
            borderLeft := "6px solid #2196f3",
            borderRadius := "3px",
        )

        c.selected -> (
            borderLeftColor := "#4caf50",
        )

        c.food /+ c.food -> (
            marginTop := "1em",
        )
    }
}
