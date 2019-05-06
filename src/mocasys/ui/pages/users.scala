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

class UsersPage extends TablePage {
    var form: Option[Form] = None

    override def renderForm: VNodeFrag = 
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

    override def renderTable: VNodeFrag = 
        new InteractiveTable(
            "SELECT * FROM users ORDER BY id",
            onClickRendererForColumn({ row =>
                form = Some(new Form(this, row))
            }), Seq("sys_period"))

    override def renderControls: VNodeFrag = 
        button("New user", cls := "bgColor3", onClick := { _ =>
            form = Some(new Form(this, Map(
                "username" -> "", "id_person" -> 0)))
        })

}
