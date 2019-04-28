package mocasys.ui.components

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
import mocasys.ui.main._
import mocasys.ui.tables._
import mocasys.ApiClient._

class Food(
        val date: js.Date,
        val choices: Seq[DbRow]) extends Component {
    val isToday = {
        val today = new js.Date()
        date.getDate() == today.getDate() &&
        date.getMonth() == today.getMonth() &&
        date.getFullYear() == today.getFullYear()
    }

    def render() = scoped(
        div(cls := "food" + (if (isToday) " today" else ""),
            div(cls := "info",
                span(date.toString()),
                div(cls := "controls",
                    button("Z"),
                    button("O"),
                    button("X"),
                )
            ),
            table(tbody(
                choices.map(choice => tr(
                    td(choice("kind").asInstanceOf[String]),
                    td(choice("name").asInstanceOf[String]),
                ))
            )),
        )
    )

    cssScoped { import liwec.cssDsl._
        c.food -> (
            height := "10em",
            backgroundColor := "white",
            border := "1px solid #dadce0",
            borderLeft := "6px solid #56aeB5",
            borderRadius := "4px",

            c.info -> (
                display := "flex",
                flexDirection := "row",
                borderBottom := "1px solid #dadce0",
                height := "31px",

                e.span -> (
                    marginLeft := "0.8em",
                    marginTop := "0.5em",
                    marginBottom := "0.2em",
                ),

                c.controls -> (
                    marginLeft := "auto",

                    e.button -> (
                        marginLeft := "10px",
                        height := "100%",
                        lineHeight := "100%",
                        borderRadius := "0",
                        border := "0px",
                        fontWeight := "bold",
                        backgroundColor := "#e69857",
                    ),

                    RawSelector("button:hover") -> (
                        backgroundColor := "#e66740",
                        color := "white",
                    )
                ),
            ),

            e.table -> (
                maxWidth := "50%",
                paddingLeft := "0.8em",

                RawSelector("td:first-child") -> (
                    fontWeight := "bold",
                ),
            ),
        )

        c.today -> (
            borderLeftColor := "#4caf50",
        )

        c.food /+ c.food -> (
            marginTop := "1em",
        )

        e.button -> (
            height := "100%",
        )

        c.foodEmpty -> (
            borderLeft := "6px solid grey",
            color := "#eeeeee",
            backgroundColor := "#eeeeee",
        )
    }
}
