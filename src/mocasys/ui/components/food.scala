package mocasys.ui.components

import scala.util.{Success, Failure}
import scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.ext._
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ui.components._
import mocasys.ui.functionComponents._
import mocasys.ui.main._
import mocasys.ui.tables._
import mocasys.ApiClient._

class Food(val name: String) extends Component {

    def render = scoped(div(cls := "food borderShadowColor3",
        span(name),
    ))

    cssScoped { import liwec.cssDsl._
        c.food (
            padding := "0.3em 0.2em",
            backgroundColor := "#3685a2",
            color := "#f1ffff",
            minHeight := "1.5em",
        )
    }
}
