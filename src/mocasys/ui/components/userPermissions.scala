package mocasys.ui.components

import scala.util.{Success, Failure}
import scalajs.js
import scala.collection.SortedSet
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
    var usersPermissions: Option[SortedSet[String]] = None
    var permissions: Option[SortedSet[String]] = None

    def diffPerms: SortedSet[String] = if (usersPermissions != None && permissions != None)
            permissions.get -- usersPermissions.get
        else
            SortedSet()

    override def onMount = {
        fetchAllPermissions
        fetchUsersPermissions
    }

    def fetchUsersPermissions =
        AppState.queryDb(
            """SELECT permission FROM user_permissions WHERE id_user = $1 ORDER BY permission""",
            Seq(userId)
        ).onComplete {
            case Success(res) => {
                usersPermissions = Some(
                    (for (perm <- res.rows) yield perm(0).toString).to[SortedSet]
                )
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
            }
        }
    
    def fetchAllPermissions =
        AppState.queryDb("""SELECT name FROM permissions ORDER BY name""")
        .onComplete {
            case Success(res) => {
                permissions = Some(
                    (for (perm <- res.rows) yield perm(0).toString).to[SortedSet]
                )
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
            }
        }
    
    def removePermission(name: String, onSuccess: () => Unit) =
        AppState.queryDb(
            """DELETE FROM user_permissions WHERE id_user = $1 AND permission = $2""",
            Seq(userId, name)
        ).onComplete {
            case Success(res) => {
                onSuccess()
                println(s"$name deleted!")
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
                println(s"failed to delete $name")
                fetchUsersPermissions
            }
        }

    def addPermission(name: String, onSuccess: () => Unit) =
        AppState.queryDb(
            """INSERT INTO user_permissions (id_user, permission) VALUES ($1, $2)""",
            Seq(userId, name)
        ).onComplete {
            case Success(res) => {
                onSuccess()
                println(s"$name inserted!")
            }
            case Failure(e) => {
                val ApiError(_, msg) = e
                println(s"failed to insert $name")
                fetchUsersPermissions
            }
        }
    
    def dropableLi(name: String) =
        li(cls := "dropable", name, id := name,
            draggable := "true",
            onDragstart := { e: dom.DragEvent => {
                e.dataTransfer.setData("name", name)
            }},
        )

    def dropableUl(perms: SortedSet[String], onDropVal: dom.DragEvent => Unit) =
        ul(perms.map { name => dropableLi(name.toString) },
            onDrop := onDropVal,
            onDragover := { e: dom.DragEvent => e.preventDefault() },
            onDragleave := { e: dom.DragEvent => e.preventDefault() }
        )
    
    def onDropPerm(e: dom.DragEvent): String = {
        e.preventDefault()
        val name = e.dataTransfer.getData("name")
        return name
    }

    def render = scoped(div(cls := "userPermissions",
        div(cls := "owned",
            h2("Owned Permissions"),
            (if (usersPermissions != None)
                dropableUl(usersPermissions.get, { e => {
                    val name = onDropPerm(e)
                    addPermission(name, { () =>
                        e.target.asInstanceOf[dom.raw.HTMLElement]
                            .appendChild(dom.document.getElementById(name))
                    })
                }})
            else None)
        ),
        div(cls := "notOwned",
            h2("Available Permissions"),
            dropableUl(diffPerms, { e => {
                val name = onDropPerm(e)
                removePermission(name, { () =>
                    e.target.asInstanceOf[dom.raw.HTMLElement]
                        .appendChild(dom.document.getElementById(name))
                })
            }})
        ),
    ))

    cssScoped { import liwec.cssDsl._
        c.userPermissions (
            backgroundColor := "blue",
            display := "grid",
            gridTemplateColumns := "1fr 1fr",
            gridColumnGap := "1em",

            e.ul (
                listStyle := "none",
                paddingBottom := "3em",
                backgroundColor := "lightblue",
                height := "100%",
                paddingTop := "1em",
            ),

            c.owned (
                marginRight := "0.5em",
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
