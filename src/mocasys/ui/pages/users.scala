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
import mocasys.ui.functionComponents._
import mocasys.ui.tables._
import mocasys.ui.forms._
import mocasys.ApiClient._

class UsersPage extends TablePage(true) {
    var form: Option[Form] = None
    var userId: Option[Int] = None
    override val name: String = "Users"
    var modifyPermissions: Boolean = false

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
                button("Toggle Permissions Tab",
                    cls := "ShadowClick bgColor4",
                    onClick := { e => modifyPermissions = !modifyPermissions }),
                form.save("Save", "users", Seq("id")),
            )
        }

    override def renderTable = div(
        new InteractiveTable(
            s"SELECT * FROM users ORDER BY id LIMIT ${limit} OFFSET ${offset}",
            onClickRendererForColumn({ row => {
                form = Some(new Form(this, row))
                userId = Some(row("id").toString.toInt)
            }}), Seq("sys_period")),
        (if (userId != None && modifyPermissions) new UserPermissions(userId.get) else None)
    )

    override def renderControls = div(
        button("New user", cls := "bgColor4 shadowClick btnPadding", onClick := { _ =>
            form = Some(new Form(this, Map(
                "username" -> "", "id_person" -> 0)))
        })
    )
}
