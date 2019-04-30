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
import mocasys.ui.forms._
import mocasys.ApiClient._

class UsersPage extends Component {
    var form: Option[Form] = None

    def render = scoped(
        div(cls := "queryTest",
            form.map { form =>
                div(cls := "userForm",
                    form.errorText(),
                    div(s"User selected: ${form.data("id")}"),
                    label(span("Username:"),
                          form.text("username")),
                    label(span("Person:"),
                          form.textInt("id_person")),
                    form.save("Save", "users", Seq("id")),
                )
            },
            button("New user", onClick := { _ =>
                form = Some(new Form(this, Map(
                    "id" -> 0, "username" -> "", "id_person" -> 0)))
            }),
            new InteractiveTable(
                "SELECT * FROM users",
                onClickRendererForColumn({ row =>
                    form = Some(new Form(this, row))
                })),
        )
    )
}
