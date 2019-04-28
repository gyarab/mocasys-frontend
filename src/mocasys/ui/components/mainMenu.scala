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

class MainMenu() extends Component {

    def render() = scoped(
        div(cls := "mainMenu",
        )
    )

    cssScoped { import liwec.cssDsl._
        c.mainManu -> (

        )
    }
}
