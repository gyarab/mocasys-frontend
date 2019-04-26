package mocasys.ui

import scala.util.{Success, Failure}
import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.ext._
import liwec._
import liwec.domvm.ElementVNode
import liwec.htmlDsl._
import liwec.htmlMacros._
import mocasys._
import mocasys.ApiClient._
import mocasys.ui.main.textInput

package object tables {
    case class Column[R](
        val title: String,
        val render: R => VNodeFrag)

    def dataTable[R](cols: Seq[Column[R]], data: Seq[R]) =
        table(cls := "dataTable",
            thead(
                for(col <- cols) yield th(col.title)
            ),
            tbody(
                for(row <- data) yield
                    tr(for(col <- cols) yield td(col.render(row)))
            ),
        )

    def rendererForColumn(col: DbField): (DbRow, Any) => VNodeFrag =
        col.dataTypeName match {
            // Numbers and strings (and other unmentioned types) are simply
            // shown using toString
            case _ => { (_, v: Any) =>
                v match {
                       case null => ""
                       case _ => v.toString()
                } }
        }

    def linkedRendererForGroup(linkGetter: DbRow => String)(col: DbField):
            (DbRow, Any) => VNodeApplicable[ElementVNode] = {
        val renderer = rendererForColumn(col)
        (row, value) =>
            a(href := linkGetter(row), renderer(row, value))
    }

    def colsFromQuery(
            queryRes: QueryDbResp,
            rendererGetter: DbField => ((DbRow, Any) => VNodeFrag)
                = rendererForColumn) = {
        queryRes.fields.zipWithIndex.map { case (col, i) =>
            val renderer = rendererGetter(col)
            Column(col.columnName,
                   (row: DbRow) => renderer(row, row(i)))
        }
    }

    def dataTableFromQuery(queryRes: QueryDbResp) =
        dataTable(colsFromQuery(queryRes), queryRes)

    class InteractiveTable(
            var query: String,
            val rendererGetter: DbField => (DbRow, Any) => VNodeFrag
                = rendererForColumn _)
            extends Component {
        var result: Option[Either[String, QueryDbResp]] = None

        override def onMount() =
            if(query != "") executeQuery()

        def table(res: QueryDbResp) = {
            val cols = colsFromQuery(res, rendererGetter)
            dataTable(cols, res)
        }

        def executeQuery() =
            AppState.apiClient.queryDb(query)
            .onComplete {
                case Success(res) => {
                    result = Some(Right(res))
                }
                case Failure(e) => {
                    val response = e.asInstanceOf[AjaxException]
                    val json = js.JSON.parse(response.xhr.responseText)
                    result = Some(Left(json.message.toString()))
                }
            }

        def render() = scoped(
            div(
                label(span("Query:"),
                    textInput(query, { query = _ })),
                button("Execute", onClick := { _ => executeQuery() }),
                result.map {
                    case Right(res) => table(res)
                    case Left(err) => span(cls := "error", err)
                }
            )
        )
    }
}
