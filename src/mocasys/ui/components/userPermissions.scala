package mocasys.ui.components

import scala.util.{Success, Failure}
import scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.ext._
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ui.components._
import mocasys.ui.functionComponents._
import mocasys.ui.main._
import mocasys.ui.tables._
import mocasys.ApiClient._

class UserPermissions(val userId: Integer) extends Component {
    var usersPermissions: Option[Set[String]] = None
    var permissions: Option[Set[String]] = None
    var error: String = ""

    def diffPerms: Set[String] = if (usersPermissions != None && permissions != None)
            permissions.get diff usersPermissions.get
        else
            Set()

    override def onMount = {
        fetchUsersPermissions
        fetchAllPermissions
    }

    def fetchUsersPermissions =
        AppState.apiClient.queryDb(
            """SELECT permission FROM user_permissions WHERE id_user = $1""",
            Seq(userId)
        ).onComplete {
            case Success(res) => {
                usersPermissions = Some(
                    (for (perm <- res.rows) yield perm(0).toString).toSet
                )
                // usersPermissions.get.map { name =>
                //     println(name.toString)
                // }
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
                error = msg
            }
        }
    
    def fetchAllPermissions =
        AppState.apiClient.queryDb("""SELECT name FROM permissions""")
        .onComplete {
            case Success(res) => {
                permissions = Some(
                    (for (perm <- res.rows) yield perm(0).toString).toSet
                )
                println(permissions.get)
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
                error = msg
            }
        }
    
    def dropableLi(name: String) =
        li(name, id := name,
            draggable := "true",
            onDragstart := { e: dom.DragEvent => {
                e.dataTransfer.setData("name", name)
            }},
        )

    def dropableUl(perms: Set[String], onDropVal: dom.DragEvent => Unit) =
        ul(perms.map { name => dropableLi(name.toString) },
            onDrop := onDropVal,
            onDragover := { e: dom.DragEvent => e.preventDefault() },
            onDragleave := { e: dom.DragEvent => e.preventDefault() }
        )
    
    def onDropPerm(e: dom.DragEvent) = {
        e.preventDefault()
        val name = e.dataTransfer.getData("name")
        e.target.asInstanceOf[dom.raw.HTMLElement]
            .appendChild(dom.document.getElementById(name))
    }

    def render = scoped(div(cls := "userPermissions",
        div(cls := "owned",
            (if (usersPermissions != None)
                dropableUl(usersPermissions.get, onDropPerm)
            else None)
        ),
        div(cls := "notOwned",
            dropableUl(diffPerms, onDropPerm)
        ),
    ))

    cssScoped { import liwec.cssDsl._
        c.userPermissions (
            backgroundColor := "red",
        )
    }
}
