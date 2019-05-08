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
import mocasys.ui.tables._
import mocasys.ApiClient._

class NotFoundPage extends Component {

    def render = scoped(
        div(cls := "notFoundPage bgColor1 borderRadius",
            h2("404: Not Found"),
        )
    )

    cssScoped { import liwec.cssDsl._
        c.notFoundPage (
            color := "white",
            textAlign := "center",
            padding := "5em",
            paddingTop := "0.4em",
            height := "6em",
            width := "40%",
            margin := "4em auto 0 auto",
            boxShadow := "5px 5px 10px 0px rgba(0, 0, 0, 0.60)",
            borderTop := "3px solid #3685a2",
        ),
    }
}
