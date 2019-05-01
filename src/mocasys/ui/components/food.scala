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

    def radioName(choice: DbRow) =
        s"${choice("kind")}_${choice("day").toString().substring(0, 10)}"

    def forAttrValue(choice: DbRow) =
        s"${choice("name")}_${choice("id_food")}_${choice("day").toString().substring(0, 10)}"

    def render: liwec.VNode = {
        scoped(
            div(cls := "food borderRadius" + (if (isToday) " today" else ""),
                div(cls := "info",
                    span(date.toDateString()),
                ),
                table(tbody(
                    choices.map(choice => tr(
                        td(label(forAttr := forAttrValue(choice),
                            choice("kind").asInstanceOf[String]
                        )),
                        td(label(forAttr := forAttrValue(choice),
                            choice("name").asInstanceOf[String]
                        )),
                        td(label(forAttr := forAttrValue(choice),
                            choice("option").asInstanceOf[String]
                        )),
                        td(
                            input(typeAttr := "radio", id := forAttrValue(choice),
                                    value := "", name := radioName(choice))
                        ),
                    ))
                )),
            )
        )
    }

    cssScoped { import liwec.cssDsl._
        c.food -> (
            minHeight := "10em",
            maxHeight := "15em",
            backgroundColor := "white",
            boxShadow := "4px 4px 8px 0px rgba(0, 0, 0, 0.60)",
            borderTop := "3px solid #3ea7b9",
            color := "white",

            c.info -> (
                display := "flex",
                flexDirection := "row",
                height := "31px",
                backgroundColor := "#265976",

                e.span -> (
                    marginLeft := "0.8em",
                    marginTop := "0.4em",
                ),
            ),

            e.table -> (
                maxWidth := "50%",
                maxHeight := "100%",
                marginTop := "0.3em",
                paddingLeft := "0.8em",

                e.td (paddingLeft := "1em"),

                RawSelector("td:nth-child(1)") -> (
                    fontWeight := "bold",
                    paddingLeft := "0",
                ),
            ),
        )

        c.food /+ c.food -> (
            marginTop := "1em",
        )
    }
}
