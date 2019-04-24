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
    var query = ""
    var error = ""
    var result: Option[QueryDbResp] = None

    def render = scoped(
        div(cls := "queryTest",
            h1("Query test"),
            label(span("SQL query:"),
                    textInput(query, { query = _ })),
            input(typeAttr:="submit", value:="Submit", onClick:={ _ =>
                AppState.apiClient.queryDb(query)
                .onComplete {
                    case Success(res) => {
                        result = Some(res)
                        error = ""
                    }
                    case Failure(e) => {
                        result = None
                        val response = e.asInstanceOf[AjaxException]
                        val json = js.JSON.parse(response.xhr.responseText)
                        error = json.message.toString()
                    }
                }
            }),
            if(error != "") Seq(div(s"error: $error")) else Seq(),
            div("Result:"),
            // This is an example table, showing most of their functionality
            result.map { res =>
                // TODO: Maybe some prettier way of formatting all this? By
                // convention or by creating a DSL
                val cols = colsFromQuery(res, col => v =>
                        a(href := "/some/url", rendererForColumn(col)(v))) :+
                    Column(
                        "Row as JSON",
                        (r: js.Array[js.Any]) => js.JSON.stringify(r))
                dataTable(cols, res.rows)
            },
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
