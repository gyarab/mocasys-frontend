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

class MainMenu() extends Component {

    def render: liwec.VNode = {
        val username = AppState.loggedInUser
        println(username)
        if (username == None) return scoped(div())
        return scoped(
            div(cls := "mainMenu bgColor1 borderRadius",
                img(src := "/assets/mocasys_logo_trans.svg"),
                div(cls := "hider",
                    div(cls := "bar1"), 
                    div(cls := "bar2"),
                    div(cls := "bar3"),
                    onClick := { e => 
                        val menu = dom.document.querySelector(".mainMenu")
                            .asInstanceOf[dom.raw.HTMLElement]
                        if (menu.style.left == "-16%")
                            menu.style.left = "0"
                        else menu.style.left = "-16%"
                    }
                ),
                div(cls := "userMenu",
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
                div(cls := "menu",
                
                ),
            )
        )
    }

    cssScoped { import liwec.cssDsl._
        // TODO: Fix - on small screen buttons popout
        c.mainMenu -> (
            position := "absolute",
            left := "0",
            top := "0",
            marginTop := "3%",
            width := "16%",
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
        )
    }
}
