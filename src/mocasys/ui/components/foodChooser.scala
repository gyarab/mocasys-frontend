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
import mocasys.ui.pages.FoodSelection
import mocasys.ui.main._
import mocasys.ui.tables._
import mocasys.ApiClient._

class FoodChooser(
        val parent: FoodSelection,
        val date: js.Date,
        val choices: Seq[DbRow]) extends Component {
    val today = new js.Date()
    val deadlineDays: Integer = 1

    lazy val shouldBeDisabled: Boolean =
        deadlineDays * 3600 * 24 * 1000 >
            (date.getTime - today.getTime)
    lazy val isToday: Boolean = {
        date.getDate() == today.getDate() &&
        date.getMonth() == today.getMonth() &&
        date.getFullYear() == today.getFullYear()
    }

    var error: String = ""

    // TODO: Replace once the query builder is finished
    def choiceQuery(choice: DbRow) =
        if (choice("option2") == null)
            AppState.apiClient.queryDb(
                """INSERT INTO food_choice (id_diner, day, kind, option) VALUES
                (session_person_get(), $1, $2, $3)""",
                Seq(choice("day"), choice("kind"), choice("option"))
            )
        else
            AppState.apiClient.queryDb(
                """UPDATE food_choice
                SET option = $1
                WHERE id_diner = session_person_get()
                    AND day = $2
                    AND kind = $3""",
                Seq(choice("option"), choice("day"), choice("kind"))
            )

    def radioName(choice: DbRow) =
        s"${choice("kind")}_${choice("day").toString().substring(0, 10)}"

    def forAttrValue(choice: DbRow) =
        s"radio_${choice("kind")}_${choice("id_food")}_${choice("day").toString().substring(0, 10)}"
    
    def shouldBeChecked(choice: DbRow): Boolean =
        choice("option") == choice("option2") || shouldBeDisabled
    
    def shouldBeHidden(choice: DbRow): Boolean =
        choice("option").toString.isEmpty

    def onChange(choice: DbRow) =
        choiceQuery(choice)
        .onComplete {
            // TODO: Do more efficiently
            case Success(res) => parent.fetchFoodList
            case Failure(e) => {
                val ApiError(_, msg) = e
                error = msg
            }
        }

    //TODO: Actaully cancel the food
    def cancelFood = Unit

    def render = scoped(div(cls := "food borderRadius" + (if (isToday) " today" else ""),
        div(cls := "info",
            span(date.toDateString()),
            button("Cancel", cls := "cancelButton shadowClick",
                (if (shouldBeDisabled) disabled := "true" else None),
                onClick := { e => cancelFood }
            ),
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
                (if (shouldBeHidden(choice))
                    td()
                else
                    td(radioInput(
                        forAttrValue(choice),
                        radioName(choice),
                        { _ => onChange(choice) },
                        shouldBeChecked(choice),
                        shouldBeDisabled,
                    )
                )),
            ))
        )),
    ))

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
                c.cancelButton (
                    fontSize := "15",
                    margin := "2px 2em 2px auto",
                    borderRadius := "0",
                    color := "265976",
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