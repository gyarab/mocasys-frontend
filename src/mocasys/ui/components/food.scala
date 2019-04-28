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
        val choices: Seq[Seq[Any]]
    ) extends Component {

    def render: liwec.VNode = {
        val today: Boolean = false
        println("null: " + (choices == null).toString())
        if (choices != null) {
            println("length: " + choices.length)
        }
        return scoped(
            // if (choices != null && choices.length > 0)
                div(cls := "food" + (if (today) " today" else ""),
                    div(cls := "info",
                        span("Date String"),
                        div(cls := "controls",
                            button("Z"),
                            button("O"),
                            button("X"),
                        )
                    ),
                    table(tbody(
                        tr(
                            td("Soup"),
                            td("Gulasch"),
                        ),
                        tr(
                            td("Main"),
                            td("Option 1"),
                        ),
                        tr(
                            td(""),
                            td("Krůtí nudličky po provensálsku, bramborové špecle"),
                        ),
                        tr(
                            td("Dessert"),
                            td("Ice Cream"),
                        )
                    )),
                )
            // else
            //     div(cls := "food foodEmpty",
            //         div(cls := "info",
            //             //span("Sat Apr 27 2019"),
            //             div(cls := "controls",
            //                 //button("Z"),
            //                 //button("O"),
            //                 //button("X"),
            //             )
            //         ),
            //         table(tbody(
            //             //for (_ <- 0 until 4) yield tr(td("."), td("."))
            //         )),
            //     )
        )
    }

    cssScoped { import liwec.cssDsl._
        c.food -> (
            fontFamily := "Helvetica",
            height := "10em",
            backgroundColor := "white",
            border := "1px solid #dadce0",
            borderLeft := "6px solid #56aeB5",
            borderRadius := "4px",

            c.info -> (
                display := "grid",
                borderBottom := "1px solid #dadce0",
                height := "31px",

                e.span -> (
                    gridColumn := "1 / 3",
                    marginLeft := "0.8em",
                    marginTop := "0.5em",
                    marginBottom := "0.2em",
                ),

                c.controls -> (
                    gridColumn := "3 / 3",
                    gridColumnGap := "4px",
                    display := "grid",
                    gridTemplateColumns := "repeat(3, 1fr)",
                    gridTemplateRows := "1fr",

                    e.button -> (
                        height := "100%",
                        borderRadius := "0",
                        border := "0px",
                        fontWeight := "bold",
                        font := "Tahoma",
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
                fontSize := "0.9em",
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
