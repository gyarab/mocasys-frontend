package mocasys.ui

import scala.scalajs.js
import liwec._
import liwec.domvm.ElementVNode
import liwec.htmlDsl._
import liwec.htmlDslTypes.VNodeApplicable
import mocasys.ApiClient.{DbField, QueryDbResp}

package object tables {
    case class Column[R](
        val title: String,
        val render: R => VNodeApplicable[ElementVNode])

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

    def rendererForColumn(col: DbField): Any => VNodeApplicable[ElementVNode] =
        col.dataTypeName match {
            // Numbers and strings (and other unmentioned types) are simply
            // shown using toString
            case _ => { (v: Any) =>
                v match {
                       case null => ""
                       case _ => v.toString()
                } }
        }

    def colsFromQuery(
            queryRes: QueryDbResp,
            rendererGetter: DbField => (Any => VNodeApplicable[ElementVNode])
                = rendererForColumn) = {
        queryRes.fields.zipWithIndex.map { case (col, i) =>
            val renderer = rendererGetter(col)
            Column(col.columnName,
                   (row: js.Array[js.Any]) => renderer(row(i)))
        }
    }

    def dataTableFromQuery(queryRes: QueryDbResp) =
        dataTable(colsFromQuery(queryRes), queryRes.rows)
}
