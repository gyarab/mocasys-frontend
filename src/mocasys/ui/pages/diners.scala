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

class DinersPage extends TablePage(true) {
    var form: Option[Form] = None
    override val name: String = "Diners"

    override def renderForm =
        form.map { form =>
            div(cls := "userForm",
                form.errorText(),
                (if (form.data.keySet.exists(_ == "id")) 
                    div(s"User selected: ${form.data("id")}")
                else None),
                label(span("ID Person:"),
                        form.textInt("id_person")),
                form.save("Save", "diners", Seq("id_person")),
            )
        }

    def formForRow(row: DbRow) =
        new Form(this, Map(
            "id_person" -> row("id_person")
        ), true)

    override def renderTable =
        new InteractiveTable(
            s"""SELECT * FROM diners AS d
            INNER JOIN people AS p ON p.id = d.id_person
            ORDER BY d.id_person LIMIT ${limit} OFFSET ${offset}""",
            onClickRendererForColumn({ row =>
                form = Some(formForRow(row))
            }),
            Seq("sys_period")
        )

    override def renderControls: VNodeFrag = 
        button("New diner", cls := "bgColor3", onClick := { _ =>
            form = Some(new Form(this, Map(
                "id_person" -> 0, "account_balance" -> 0)))
        })

}
