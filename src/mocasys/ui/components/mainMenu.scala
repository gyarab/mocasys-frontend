package mocasys.ui.components

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import scalajs.js
import scalajs.js.annotation._
import org.scalajs.dom
import org.scalajs.dom.ext._
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ui.main.textInput

class MenuNode
case class MenuItem(val value: String,
                    val action: dom.Event => Unit = {_ => Unit}
    ) extends MenuNode
case class SubMenu(val item: MenuItem, val children: Seq[MenuNode]) extends MenuNode

class MainMenu() extends Component {
    var visible: Boolean = false

    lazy val rootNode: SubMenu =
        SubMenu(MenuItem("Menu"),
            Seq(
                MenuItem("/foods", _ => dom.window.location.href = "/foods"),
                MenuItem("/users", _ => dom.window.location.href = "/users"),
                SubMenu(MenuItem("Submenu"),
                    Seq(
                        MenuItem("1", _ => dom.window.alert("Nada!")),
                        MenuItem("2", _ => dom.window.alert("Nada!"))
                    )
                ),
            )
        )

    def renderMenu(node: SubMenu, root: Boolean = false): liwec.htmlDsl.VNodeFrag =
        if (root)
            node match {
                case s: SubMenu => div(cls := "menuContainer",
                    h2(cls := "menuHeader", node.item.value),
                    ul(cls := "rootMenu", renderMenu(node))
                )
            }
        else
            for (child <- node.children) yield child match {
                case s: SubMenu => li(cls := "menuItem",
                    h4(cls := "menuHeader", s.item.value,
                        onClick := s.item.action),
                    ul(cls := "menu",
                        renderMenu(s)
                    )
                )
                case i: MenuItem => li(cls := "menuItem", span(i.value),
                                     onClick := i.action)
            }

    def render: liwec.VNode = {
        val username = AppState.loggedInUser
        if (username == None) return scoped(div())
        return scoped(
            nav(cls := "mainMenu bgColor1 borderRadius "
                        + (if (visible) "visible" else "invisible"),
                img(src := "/assets/mocasys_logo_trans.svg"),
                div(cls := "hider",
                    div(cls := "bar1"), 
                    div(cls := "bar2"),
                    div(cls := "bar3"),
                    onClick := { e => visible = !visible }
                ),
                nav(cls := "userMenu",
                    span("Logged in as "), b(username.get),
                    button("Profile", cls := "profile bgColor4 shadowClick",
                    onClick := { e =>
                        dom.window.alert("Not Yet Implemented!")
                    }),
                    button("Log Out", cls := "logout bgColor4 shadowClick",
                    onClick := { e =>
                        dom.window.alert("Not Yet Implemented!")
                    }),
                ),
                renderMenu(rootNode, root = true)
            )
        )
    }

    cssScoped { import liwec.cssDsl._
        // TODO: Fix - on small screen buttons popout
        (c.mainMenu & c.visible) -> (
            left := "0", // Changed by .hider
        )

        (c.mainMenu & c.invisible) -> (
            left := "-16em", // Changed by .hider
        )

        c.mainMenu -> (
            position := "absolute",
            top := "0",
            marginTop := "3%",
            width := "16em",
            height := "90%",
            boxShadow := "5px 5px 10px 0px rgba(0, 0, 0, 0.60)",
            borderBottomLeftRadius := "0",
            borderTopLeftRadius := "0",
            color := "#f1ffff",
            fontFamily := "Helvetica",
            transition := "left 0.3s ease-in-out",

            e.img (
                width := "100%",
            ),

            // TODO: Position better
            c.hider (
                position := "absolute",
                left := "102%",
                top := "0.8%",
                display := "grid",
                padding := "3px",
                width := "2.1em",
                height := "2.1em",
                gridTemplateRows := "repeat(5, 1fr)",

                e.div (
                    border := "2px solid #ffb820",
                    borderRightWidth := "0",
                    borderBottomWidth := "0",
                    gridColumn := "1",
                    backgroundColor := "#ff9b20",
                    boxShadow := "2px 2px 4px 0px rgba(0, 0, 0, 0.60)",
                ),

                c.bar1 (gridRow := "1"),
                c.bar2 (gridRow := "3"),
                c.bar3 (gridRow := "5"),
            ),

            RawSelector(".hider:hover") (
                e.div (
                    transform := "translateY(-2px)",
                ),
            ),

            RawSelector(".hider:active") (
                e.div (
                    backgroundColor := "#d23a3f",
                    border := "0",
                    transform := "translateY(0px)",
                ),
            ),

            c.userMenu (
                display := "grid",
                gridRowGap := "0.1em",
                padding := "0.2em 0.8em 0.4em 0.8em",

                e.b (
                    gridRow := "1",
                    gridColumn := "3",
                    justifySelf := "right",
                ),

                e.button (
                    padding := "4px 6px 4px 6px",
                ),

                c.logout (
                    gridRow := "3/4",
                    gridColumn := "3/4",
                ),

                c.profile (
                    gridRow := "3/4",
                    gridColumn := "1/2",
                ),
            ),

            c.menuContainer (
                listStyle := "none",
                margin := "0",
                padding := "1em 0.8em 0 0.8em",

                e.ul (listStyle := "none"),

                RawSelector("*") (
                    margin := "0",
                    padding := "0",
                ),

                c.menu (
                    margin := "0.2em 0 0.3em 1.6em",
                ),

                c.menuHeader (
                    margin := "4px 0 4px 0px",
                    backgroundColor := "blue", // Debug
                ),

                c.menuItem (
                    backgroundColor := "red", // Debug
                    margin := "0.1em 0 0.2em 0em",
                    padding := "5px",

                    RawSelector("span:hover") (
                        textDecoration := "underline",
                    ),
                )
            ),
        )
    }
}
