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
import mocasys.ui._
import mocasys.ui.components._
import mocasys.ui.tables._
import mocasys.ui.forms._
import mocasys.ApiClient._

class PeoplePage extends TablePage(true) {
    var form: Option[Form] = None
    override val name: String = "People"

    override def renderForm =
        form.map { form =>
            div(cls := "peopleForm",
                form.errorText(),
                (if (form.data.keySet.exists(_ == "id")) 
                    div(s"Person selected: ${form.data("id")}")
                else None),
                label(span("Name:"),
                    form.text("name")),
                label(span("Birth Date:"),
                    form.textDate("birth_date")),
                form.save("Save", "people", Seq("id")),
            )
        }

    override def renderTable =
        new InteractiveTable(
            s"SELECT * FROM people ORDER BY id LIMIT ${limit} OFFSET ${offset}",
            onClickRendererForColumn({ row =>
                form = Some(new Form(this, row))
            }), Seq("sys_period"))

    override def renderControls = 
        button("New Food", cls := "bgColor4 shadowClick btnPadding", onClick := { _ =>
            form = Some(new Form(this, Map(
                "name" -> "", "birth_date" -> "2000-01-01")))
        })

}
