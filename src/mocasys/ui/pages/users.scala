package mocasys.ui.pages

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

class UsersPage extends Component {

    def render = scoped(
        div(cls := "queryTest",
            new InteractiveTable("SELECT * FROM users"),
        )
    )

    cssScoped { import liwec.cssDsl._
        c.queryTest -> (
            (e.input & RawSelector("[type=text]")) -> (
                width := "60%",
            ),
        ),
    }
}
