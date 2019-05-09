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

//There is a bug where permissions can be visually put into other permissions.
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
            """SELECT permission FROM user_permissions WHERE id_user = $1 ORDER BY permission""",
            Seq(userId)
        ).onComplete {
            case Success(res) => {
                usersPermissions = Some(
                    (for (perm <- res.rows) yield perm(0).toString).toSet
                )
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
                error = msg
            }
        }
    
    def fetchAllPermissions =
        AppState.apiClient.queryDb("""SELECT name FROM permissions ORDER BY name""")
        .onComplete {
            case Success(res) => {
                permissions = Some(
                    (for (perm <- res.rows) yield perm(0).toString).toSet
                )
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
                error = msg
            }
        }
    
    def removePermission(name: String) =
        AppState.apiClient.queryDb(
            """DELETE FROM user_permissions WHERE id_user = $1 AND permission = $2""",
            Seq(userId, name)
        ).onComplete {
            case Success(res) => println(s"$name deleted!")
            case Failure(e) => println(s"failed to delete $name")
        }

    def addPermission(name: String) =
        AppState.apiClient.queryDb(
            """INSERT INTO user_permissions (id_user, permission) VALUES ($1, $2)""",
            Seq(userId, name)
        ).onComplete {
            case Success(res) => println(s"$name inserted!")
            case Failure(e) => println(s"failed to insert $name")
        }
    
    def dropableLi(name: String) =
        li(cls := "dropable", name, id := name,
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
    
    def onDropPerm(e: dom.DragEvent): String = {
        e.preventDefault()
        val name = e.dataTransfer.getData("name")
        e.target.asInstanceOf[dom.raw.HTMLElement]
            .appendChild(dom.document.getElementById(name))
        return name
    }

    def render = scoped(div(cls := "userPermissions",
        div(cls := "owned",
            h2("Owned Permissions"),
            (if (usersPermissions != None)
                dropableUl(usersPermissions.get, { e => {
                    val name = onDropPerm(e)
                    addPermission(name)
                }})
            else None)
        ),
        div(cls := "notOwned",
            h2("Available Permissions"),
            dropableUl(diffPerms, { e => {
                val name = onDropPerm(e)
                removePermission(name)
            }})
        ),
    ))

    cssScoped { import liwec.cssDsl._
        c.userPermissions (
            backgroundColor := "red",
            display := "grid",

            e.ul (
                listStyle := "none",
            ),

            c.owned (
                gridColumn := "1",
            ),

            c.notOwned (
                gridColumn := "2",
            ),

            (e.li & c.dropable) (
                marginBottom := "0.5em",
                padding := "0.3em 0.2em",
                backgroundColor := "grey",
            ),

            ((e.li & c.dropable) /+ (e.li & c.dropable)) (
                marginTop := "0.5em",
            ),
        )
    }
}
