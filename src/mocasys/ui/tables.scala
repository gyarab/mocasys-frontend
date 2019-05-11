package mocasys.ui

import scala.util.{Success, Failure}
import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.ext._
import liwec._
import liwec.domvm.ElementVNode
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssDsl._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ApiClient._
import mocasys.ui.functionComponents._

package object tables {
    case class Column[R](
        val title: String,
        val render: R => VNodeFrag)

    def dataTable[R](cols: Seq[Column[R]], data: Seq[R], ignored: Seq[String] = Seq()) =
        table(cls := "dataTable",
            thead(
                for(col <- cols if !ignored.contains(col.title))
                    yield th(col.title)
            ),
            tbody(
                for(row <- data) yield
                    tr(for(col <- cols if !ignored.contains(col.title))
                        yield td(col.render(row)))
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

    def linkedRendererForColumn(linkGetter: DbRow => String)(col: DbField)
            : (DbRow, Any) => VNodeFrag = {
        val renderer = rendererForColumn(col)
        (row, value) =>
            a(href := linkGetter(row), renderer(row, value))
    }

    def onClickRendererForColumn(clickHandler: DbRow => Unit)(col: DbField)
            : (DbRow, Any) => VNodeFrag = {
        val renderer = rendererForColumn(col)
        (row, value) =>
            Seq(renderer(row, value), onClick := { _ => clickHandler(row) })
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
                = rendererForColumn _,
            val ignoredCols: Seq[String] = Seq())
            extends Component {
        var result: Option[Either[String, QueryDbResp]] = None

        override def onMount() =
            if(query != "") executeQuery()

        def table(res: QueryDbResp) = {
            val cols = colsFromQuery(res, rendererGetter)
            dataTable(cols, res, ignoredCols)
        }

        def executeQuery() =
            AppState.queryDb(query)
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
            div(cls := "bgColor4",
                div (cls := "inputBox bgColor1",
                    label(span("Query:"),
                        textInput(query, { query = _ })),
                    button("Execute", cls := "execBtn shadowClick btnPadding", onClick := { _ => executeQuery() }),
                ),
                result.map {
                    case Right(res) => table(res)
                    case Left(err) => span(cls := "error", err)
                }
            )
        )

        cssScoped { import liwec.cssDsl._

            e.div (
                c.inputBox (
                    paddingBottom := "0.5em",

                    e.label (
                        paddingLeft := "1em",
                        paddingRight := "0.5em",
                    ),
                ),

                c.dataTable (
                    paddingLeft := "1em",
                    paddingBottom := "0.2em",
                    marginTop := "0.2em",
                ),

                c.execBtn (
                    margin := "0.5em",
                ),
                
                e.tr (
                    lineHeight := "1.5em",
                    color := "#265976",
                ),

                e.td (
                    padding := "2px 8px",
                ),

                e.th (
                    backgroundColor := "#265976",
                    color := "#f1ffff",
                    padding := "7px 10px",
                ),

                RawSelector("tr:nth-child(even)") (
                    backgroundColor := "#3685a2",
                    color := "#f1ffff",
                ),
            )
        }
    }
}
