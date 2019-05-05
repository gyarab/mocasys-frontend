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
import mocasys.ui.forms._
import mocasys.ApiClient._

class DinersPage extends Component {
    var form: Option[Form] = None

    def render = scoped(
        div(cls := "queryTest",
            div(cls := "controls",
                button("New diner", cls := "bgColor3", onClick := { _ =>
                    form = Some(new Form(this, Map(
                        "id_person" -> 0, "account_balance" -> 0)))
                }),
                hr(),
            ),
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
            },
            div(cls := "dataTable",
                new InteractiveTable(
                    "SELECT * FROM diners AS d INNER JOIN people AS p ON p.id = d.id_person ORDER BY d.id_person",
                    onClickRendererForColumn({ row => {
                        form = Some(new Form(this, Map(
                            "account_balance" -> row("account_balance")
                                .toString.replaceFirst("\\$", ""),
                            "id_person" -> row("id_person")
                        ), true))
                    }}), Seq("sys_period")),
            ),
        )
    )

    cssScoped { import liwec.cssDsl._
        c.queryTest (
            width := "80%",
            marginLeft := "auto",
            marginRight := "auto",
            padding := "1.5em",
            display := "grid",
            gridTemplateColumns := "1fr 1fr",

            c.error (
                color := "white",
                padding := "1em",
            ),

            c.controls (
                gridColumn := "1 / 3",
                gridRow := "1",

                e.button (
                    margin := "1em",
                ),
            ),

            c.userform (
                gridColumn := "1",
                gridRow := "2",
            ),

            c.dataTable (
                gridRow := "2",
                gridColumn := "2",
            ),
        )
    }
}
