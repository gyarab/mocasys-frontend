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

class UsersPage extends TablePage(true) {
    var form: Option[Form] = None
    override val name: String = "Users"

    override def renderForm =
        form.map { form =>
            div(cls := "userForm",
                form.errorText(),
                (if (form.data.keySet.exists(_ == "id")) 
                    div(s"User selected: ${form.data("id")}")
                else None),
                label(span("Username:"),
                        form.text("username")),
                label(span("Person:"),
                        form.textInt("id_person")),
                form.save("Save", "users", Seq("id")),
            )
        }

    override def renderTable =
        new InteractiveTable(
            s"SELECT * FROM users ORDER BY id LIMIT ${limit} OFFSET ${offset}",
            onClickRendererForColumn({ row =>
                form = Some(new Form(this, row))
            }), Seq("sys_period"))

    override def renderControls = 
        button("New user", cls := "newBtn bgColor4 shadowClick btnPadding", onClick := { _ =>
            form = Some(new Form(this, Map(
                "username" -> "", "id_person" -> 0)))
        })

    cssScoped { import liwec.cssDsl._
        c.newBtn (
            marginRight := "1em",
        ),
    }
}
