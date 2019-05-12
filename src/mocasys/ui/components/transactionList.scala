package mocasys.ui.components

import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import mocasys._
import mocasys.ApiClient._
import mocasys.ui.tables._
import mocasys.ui.forms._

class TransactionList(val diner: DbRow) extends Component {
    var form: Option[Form] = None

    def render() = scoped(
        div(
            form.map { f =>
                div(cls := "transactionForm",
                    f.errorText(),
                    label(span("Amount:"),
                        f.textMoney("amount")),
                    // TODO: Set form = None on successful save
                    f.save("Create", "diner_transactions", Seq("id"))
                )
            },
            button(cls := "createButton", "Create transaction",
                onClick := { _ =>
                    form = Some(new Form(this, Map(
                        "id_diner" -> diner("id"),
                        "amount" -> 0,
                    ), false))
                }),
            new InteractiveTable(
                s"""SELECT * FROM diner_transactions
                   WHERE id_diner = ${diner("id")}
                """),
        )
    )
}
