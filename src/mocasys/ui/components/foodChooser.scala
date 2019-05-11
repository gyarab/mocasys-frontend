package mocasys.ui.components

import scala.util.{Success, Failure}
import scalajs.js
import scala.collection.mutable
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

object FoodChooser {
    val today = new js.Date()
    val deadlineDays: Integer = 1
}

class FoodChooser(
        val parent: FoodSelection,
        val date: js.Date,
        val choices: Seq[DbRow]) extends Component {
    lazy val shouldBeDisabled: Boolean =
        FoodChooser.deadlineDays * 3600 * 24 * 1000 >
            (date.getTime - FoodChooser.today.getTime) && parent.dinerId == None
    lazy val isToday: Boolean = {
        date.getDate() == FoodChooser.today.getDate() &&
        date.getMonth() == FoodChooser.today.getMonth() &&
        date.getFullYear() == FoodChooser.today.getFullYear()
    }
    lazy val kindSet: mutable.Set[String] = mutable.Set()

    // TODO: Replace once the query builder is finished
    def choiceQuery(choice: DbRow) =
        if (choice("ordered") == null)
            AppState.queryDb(
                s"""INSERT INTO food_choice (id_diner, day, kind, option) VALUES
                (${parent.id}, $$1, $$2, $$3)""",
                Seq(choice("day"), choice("kind"), choice("option"))
            )
        else
            AppState.queryDb(
                s"""UPDATE food_choice
                SET option = $$1, ordered = $$2
                WHERE id_diner = ${parent.id}
                    AND day = $$3
                    AND kind = $$4""",
                Seq(choice("option"), true, choice("day"), choice("kind"))
            )

    def radioName(choice: DbRow) =
        s"${choice("kind")}_${choice("day").toString().substring(0, 10)}"

    def forAttrValue(choice: DbRow) =
        s"radio_${choice("kind")}_${choice("id_food")}_${choice("day").toString().substring(0, 10)}"

    def shouldBeChecked(choice: DbRow): Boolean = {
        val ord = choice("ordered")
        return (ord == null && isFirstOfItsKind(choice)) ||
               (ord.asInstanceOf[Boolean] && choice("option") == choice("option2"))
    }

    def shouldBeHidden(choice: DbRow): Boolean =
        choice("option").toString.isEmpty

    def isFirstOfItsKind(choice: DbRow): Boolean = {
        val kind = choice("kind").toString
        if (kindSet.contains(kind)) false
        else {
            kindSet += kind
            true
        }
    }

    def onChange(choice: DbRow) =
        choiceQuery(choice)
        .foreach { res =>
            parent.fetchFoodList
        }

    def cancelUsingUpdate =
        AppState.queryDb(
            s"""UPDATE food_choice
            SET option = NULL, ordered = $$1
            WHERE day = $$2
            AND id_diner = ${parent.id}""",
            Seq(false, isoDate(date))
        ).foreach { res =>
            parent.fetchFoodList
        }

    def cancelUsingInsert(kindsChosen: Set[String]) = {
        val valParams = kindsChosen
            .zipWithIndex
            .map { case (kind, i) =>
                // $1 is date, $2 is ordered = false
                s"(${parent.id}, $$1, $$2, NULL, $$${i + 3})"
            }
            .mkString(",\n")
        val params = Seq(isoDate(date), false) ++ kindsChosen.toSeq
        AppState.queryDb(
                """INSERT INTO food_choice (id_diner, day, ordered, option, kind) VALUES """
                + valParams, params
            ).foreach { res =>
                parent.fetchFoodList
            }
    }

    def cancelFood = {
        val kindsCount = choices
            .map(r => r("kind").toString)
            .toSet
            .size
        val kindsNotChosen = choices
            .filter(r => r("ordered") == null)
            .map(r => r("kind").toString)
            .toSet
        if (kindsNotChosen.size == 0) {
            // Only update
            cancelUsingUpdate
        } else if (kindsNotChosen.size == kindsCount) {
            // Only insert
            cancelUsingInsert(kindsNotChosen)
        } else {
            // Both insert and update
            cancelUsingUpdate
            cancelUsingInsert(kindsNotChosen)
        }
    }

    def renderRow(choice: DbRow): liwec.VNode = {
        // Calling it the second time would produce a diff value
        val check = shouldBeChecked(choice)
        return tr((if (check) cls := "chosenRow" else None),
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
                    check,
                    shouldBeDisabled,
                )
            )),
        )   
    }

    def render: liwec.VNode = {
        kindSet.clear()
        println(kindSet)
        return scoped(div(cls := "food borderRadius" + (if (isToday) " today" else ""),
            div(cls := "info",
                span(date.toDateString()),
                button("Cancel", cls := "cancelButton shadowClick",
                    (if (shouldBeDisabled) disabled := "true" else None),
                    onClick := { e => cancelFood }
                ),
            ),
            table(tbody(choices.map(renderRow))),
        ))
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
