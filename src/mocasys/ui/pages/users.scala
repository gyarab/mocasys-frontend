package mocasys.ui.pages

import scala.util.{Success, Failure}
import scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ui.components._
import mocasys.ui.main.textInput

class UsersPage extends Component {
    var query = ""
    var result = ""

    def render = scoped(
        div(cls := "queryTest",
            h1("Query test"),
            label(span("SQL query:"),
                    textInput(query, { query = _ })),
            input(typeAttr:="submit", value:="Submit", onClick:={ _ =>
                AppState.apiClient.queryDb(query)
                .onComplete {
                    case Success(res) => result = js.JSON.stringify(res)
                    case Failure(e) => result = s"Error: $e"
                }
            }),
            label(span("Result:"), pre(result)),
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
