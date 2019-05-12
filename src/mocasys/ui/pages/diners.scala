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
    var selectedDiner: Option[DbRow] = None
    override val name: String = "Diners"
    var dinerId: Option[Int] = None
    var modifyFoodSelection: Boolean = false

    override def renderForm =
        div(
            form.map { form =>
                div(cls := "userForm",
                    form.errorText(),
                    (if (form.data.keySet.exists(_ == "id")) 
                        div(s"User selected: ${form.data("id")}")
                    else None),
                    label(span("ID Person:"),
                            form.textInt("id_person")),
                    button("Toggle Food Selection Tab",
                        cls := "ShadowClick bgColor4",
                        onClick := { e => modifyFoodSelection = !modifyFoodSelection }),
                    form.save("Save", "diners", Seq("id_person")),
                )
            },

            selectedDiner.map { diner =>
                div(new TransactionList(diner))
            },
        )

    def formForRow(row: DbRow) =
        new Form(this, Map(
            "id_person" -> row("id_person")
        ), true)

    override def renderTable = div(
        new InteractiveTable(
            s"""SELECT *, diner_balance(d.id_person) AS balance
            FROM diners AS d
            INNER JOIN people AS p ON p.id = d.id_person
            ORDER BY d.id_person LIMIT ${limit} OFFSET ${offset}""",
            onClickRendererForColumn({ row =>
                selectedDiner = Some(row)
                form = Some(formForRow(row))
                dinerId = Some(row("id").toString.toInt)
            }),
            Seq("sys_period")),
        (if (dinerId != None && modifyFoodSelection) new FoodSelection(dinerId) else None)
    )

    override def renderControls: VNodeFrag = 
        button("New diner", cls := "bgColor4 ShadowClick btnPadding", onClick := { _ =>
            form = Some(new Form(this, Map(
                "id_person" -> 0, "account_balance" -> 0)))
        })
}
