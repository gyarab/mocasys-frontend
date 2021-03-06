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

trait MenuNode
case class MenuItem(val value: String,
                    val action: dom.Event => Unit = { _ => Unit }
    ) extends MenuNode
case class SubMenu(val item: MenuItem,
                   val children: Seq[MenuNode]
    ) extends MenuNode

object MainMenu {
    lazy val rootNode: SubMenu =
        SubMenu(MenuItem("Menu", _ => AppState.router.goToUrl("")),
            Seq(
                MenuItem("Food Selection", _ => AppState.router.goToUrl("food-selection")),
                MenuItem("Food Assignment", _ => AppState.router.goToUrl("food-assignment")),
                MenuItem("Users", _ => AppState.router.goToUrl("users")),
                MenuItem("Diners", _ => AppState.router.goToUrl("diners")),
                MenuItem("People", _ => AppState.router.goToUrl("people")),
                MenuItem("Food", _ => AppState.router.goToUrl("food")),
            )
        )
}

class MainMenu extends Component {
    var visible: Boolean = false

    def renderMenuRoot(node: SubMenu, showHeader: Boolean = true): liwec.htmlDsl.VNodeFrag =
        div(cls := "menuContainer",
            (if (showHeader) h2(cls := "menuHeader borderRadius borderShadowColor3",
                                node.item.value,
                                onClick := node.item.action) 
                else None),
            ul(cls := "rootMenu", renderMenu(node)
        ))

    def renderMenu(node: SubMenu, root: Boolean = false): liwec.htmlDsl.VNodeFrag =
        for (child <- node.children) yield child match {
            case s: SubMenu => li(cls := "menuItem",
                h4(cls := "menuHeader borderRadius borderShadowColor3",
                    s.item.value,
                    onClick := s.item.action),
                    ul(cls := "menu", renderMenu(s))
                )
            case i: MenuItem => 
                li(cls := "menuItem",
                    span(i.value),
                    onClick := i.action
                )
        }

    def render: liwec.VNode = {
        val username = AppState.loggedInUser
        if (username == None) return scoped(div())
        return scoped(
            div(
                div(cls := "hider",
                    div(cls := "bar1"),
                    div(cls := "bar2"),
                    div(cls := "bar3"),
                    onClick := { e => visible = !visible },
                ),
                nav(cls := "mainMenu bgColor1 borderRadius boxShadowBig"
                            + (if (visible) " visible" else " invisible"),
                    div(cls := "container",
                        img(src := "/assets/mocasys_logo_trans.svg"),
                        renderUserMenu(username),
                        renderMenuRoot(MainMenu.rootNode),
                    )
                )
            )
        )
    }

    def renderUserMenu(username: Option[String]) =
        nav(cls := "userMenu",
            span("Logged in as "), b(username.get),
            button("Profile",
                    cls := "profile bgColor4 shadowClick",
                    onClick := { e => AppState.router.goToUrl("profile") }),
            button("Log Out",
                    cls := "logout bgColor4 shadowClick",
                    onClick := { e => {
                        // visible = false
                        AppState.logout
                    }}),
        )

    cssScoped { import liwec.cssDsl._
        c.hider (
            position := "absolute",
            left := "0",
            top := "0",
            display := "grid",
            padding := "3px",
            width := "2.1em",
            height := "2.1em",
            padding := "6px 8px 8px 3px",
            gridTemplateRows := "repeat(5, 1fr)",
            boxShadow := "2px 2px 4px 0px rgba(0, 0, 0, 0.60)",
            borderBottomRightRadius := "10px",
            borderTopRightRadius := "10px",
            backgroundColor := "#265976",

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
        )

        (c.mainMenu & c.invisible) (
            width := "0",
        )

        c.mainMenu (
            position := "absolute",
            overflowX := "hidden",
            top := "0",
            width := "80%",
            maxWidth := "20em",
            height := "calc(100% - 3em)",
            marginTop := "3em",
            borderBottomLeftRadius := "0",
            borderTopLeftRadius := "0",
            transition := "width 0.3s ease-in-out",
            zIndex := "10",

            e.img (
                width := "100%",
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

            c.container(
                width := "80vw",
                maxWidth := "20em",

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
                        paddingTop := "0.2em",
                    ),

                    c.profile (
                        gridRow := "3/4",
                        gridColumn := "1/2",
                        paddingTop := "0.2em",
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
                        padding := "5px",
                        backgroundColor := "#3685a2",
                        boxShadow := "1px 1px 3px 0px rgba(0, 0, 0, 0.60)",
                    ),

                    RawSelector(".menuHeader:hover") (
                        transform := "translateX(2px)",
                    ),

                    c.menuItem (
                        margin := "0.1em 0 0.2em 1em",
                        padding := "5px",
                        paddingLeft := "0",

                        c.menuHeader(
                            color := "#f1ffff",
                        ),

                        RawSelector("span:hover") (
                            textDecoration := "underline",
                        ),
                    )
                ),
            ),
        )
    }
}
