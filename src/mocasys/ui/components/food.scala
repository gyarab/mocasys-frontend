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
    var error: String = ""

    // TODO: Replace once the query builder is finished
    def insertChoiceQuery(choice: DbRow): String =
        if (choice("option2") == null)
            s"""
            INSERT INTO food_choice (id_diner, day, kind, option) VALUES
            (session_person_get(), '${choice("day")}', '${choice("kind")}', '${choice("option")}');
            """
        else
            s"""
            UPDATE food_choice
            SET option = ${choice("option")}
            WHERE id_diner = session_person_get()
                AND day = '${choice("day")}'
                AND kind = '${choice("kind")}';
            """

    def radioName(choice: DbRow) =
        s"${choice("kind")}_${choice("day").toString().substring(0, 10)}"

    def forAttrValue(choice: DbRow) =
        s"radio_${choice("kind")}_${choice("id_food")}_${choice("day").toString().substring(0, 10)}"
    
    def shouldBeChecked(choice: DbRow): Boolean =
        choice("option") == choice("option2") || shouldBeDisabled(choice)

    def shouldBeDisabled(choice: DbRow): Boolean =
        choice("option").toString().isEmpty()

    def onChange(choice: DbRow) =
        AppState.apiClient.queryDb(insertChoiceQuery(choice))
        .onComplete {
            case Success(res) => Unit
            case Failure(e) => {
                val ApiError(_, msg) = e
                error = msg
            }
        }

    def render: liwec.VNode = {
        scoped(
            div(cls := "food borderRadius" + (if (isToday) " today" else ""),
                div(cls := "info",
                    span(date.toDateString()),
                ),
                table(tbody(
                    choices.map(choice => tr(
                        (if (shouldBeChecked(choice)) cls := "chosenRow" else None),
                        td(label(forAttr := forAttrValue(choice),
                            p(choice("kind").asInstanceOf[String])
                        )),
                        td(label(forAttr := forAttrValue(choice),
                            p(choice("option").asInstanceOf[String])
                        )),
                        td(cls := "foodName",label(forAttr := forAttrValue(choice),
                            p(choice("name").asInstanceOf[String])
                        )),
                        td(input(typeAttr := "radio", id := forAttrValue(choice),
                            name := radioName(choice),
                            onInput := { e => onChange(choice) },
                            (if (shouldBeChecked(choice)) checked := "1" else None),
                            (if (shouldBeDisabled(choice)) disabled := "1" else None)
                        )),
                    ))
                )),
            )
        )
    }

    cssScoped { import liwec.cssDsl._
        c.food (
            minHeight := "10em",
            maxHeight := "15em",
            backgroundColor := "white",
            boxShadow := "4px 4px 8px 0px rgba(0, 0, 0, 0.60)",
            borderTop := "3px solid #3ea7b9",
            color := "white",

            c.info (
                display := "flex",
                flexDirection := "row",
                height := "31px",
                backgroundColor := "#265976",

                e.span (
                    marginLeft := "0.8em",
                    marginTop := "0.4em",
                ),
            ),

            e.table (
                maxWidth := "70%",
                maxHeight := "100%",
                marginTop := "0.3em",
                paddingLeft := "0.8em",

                (e.tr / e.p) (
                    paddingLeft := "1em",
                    margin := "0",
                ),

                e.input (marginBottom := "0.2em"),

                RawSelector("td:nth-child(1) p") (
                    fontWeight := "bold",
                    paddingLeft := "0",
                ),

                c.foodName (width := "20em"),
            ),
        )

        (c.food /+ c.food) (
            marginTop := "1em",
        )
    }
}
