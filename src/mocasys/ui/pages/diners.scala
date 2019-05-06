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

class DinersPage extends TablePage {
    var form: Option[Form] = None

    override def renderForm: VNodeFrag =
        form.map { form =>
            div(cls := "userForm",
                form.errorText(),
                (if (form.data.keySet.exists(_ == "id")) 
                    div(s"User selected: ${form.data("id")}")
                else None),
                label(span("ID Person:"),
                        form.textInt("id_person")),
                label(span("Account Balance:"),
                        form.textMoney("account_balance")),
                form.save("Save", "diners", Seq("id_person")),
            )
        }

    override def renderTable: VNodeFrag =
        new InteractiveTable(
            "SELECT * FROM diners AS d INNER JOIN people AS p ON p.id = d.id_person ORDER BY d.id_person",
            onClickRendererForColumn({ row => {
                form = Some(new Form(this, Map(
                    "account_balance" -> row("account_balance")
                        .toString.replaceFirst("\\$", ""),
                    "id_person" -> row("id_person")
                ), true))
            }}), Seq("sys_period")
        )

    override def renderControls: VNodeFrag = 
        button("New diner", cls := "bgColor3", onClick := { _ =>
            form = Some(new Form(this, Map(
                "id_person" -> 0, "account_balance" -> 0)))
        })

}
