package mocasys.ui.pages

import scala.collection._
import scala.util.{Success, Failure}
import scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.ext._
import org.scalajs.dom
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector
import mocasys._
import mocasys.ui.components._
import mocasys.ui.main._
import mocasys.ui.functionComponents._
import mocasys.ui.tables._
import mocasys.ApiClient._

class FoodAssignmentPage extends Component {
    var foodData: Option[Seq[DbRow]] = None
    var foodAssignmentData: Option[Seq[DbRow]] = None
    var date: js.Date = new js.Date()
    var prevDate: js.Date = null
    var foodLanders: Seq[FoodLander] = Seq()

    override def onMount() {
        fetchFood
        fetchCurrentAssignments
    }

    // TODO: Paging
    def fetchFood =
        AppState.apiClient.queryDb("SELECT id, name FROM food")
        .onComplete {
            case Success(res) => foodData = Some(res)
            case Failure(e) => val ApiError(_, msg) = e
        }

    def fetchCurrentAssignments =
        AppState.apiClient.queryDb(s"""
            SELECT fa.day, fa.kind, fa.option, f.name, f.id FROM food_assignments AS fa
            LEFT JOIN food AS f ON fa.id_food = f.id
            WHERE fa.day = '${isoDate(date)}'
            ORDER BY fa.day;
        """)
        .onComplete {
            case Success(res) => {
                foodAssignmentData = Some(res)
                foodLanders = for(row <- res)
                    yield new FoodLander(row("kind").toString,
                        row("option").toString,
                        row("name").toString,
                        row("id").toString.toInt
                )
            }
            case Failure(e) => val ApiError(_, msg) = e
        }

    def fetchFoodAssignmentsIfDateDiff =
        if (date != prevDate) {
            prevDate = date
            fetchCurrentAssignments
        }
    
    def faWhereFl(fl: FoodLander) = s"""
        WHERE day = '${isoDate(date)}'
            AND kind = '${fl.originalKind}'
            AND option = '${fl.originalOption}'
            AND id_food = ${fl.originalFoodId}
    """

    def faUpdateQuery(fl: FoodLander) = s"""
        UPDATE food_assignments
        SET option = '${fl.option}',
            kind = '${fl.kind}',
            id_food = ${fl.foodId}
        ${faWhereFl(fl)}
    """

    def faDeleteQuery(fl: FoodLander) = s"""
        DELETE FROM food_assignments
        ${faWhereFl(fl)}
    """

    def save(e: dom.Event) = {
        for (fl <- foodLanders if fl.changed || fl.delete) {
            if (fl.delete)
                AppState.apiClient.queryDb(faDeleteQuery(fl))
                .onComplete {
                    case Success(res) => println(s"Deleted from ${fl.originalFoodName} to ${fl.foodName}")
                    case Failure(e) => val ApiError(_, msg) = e
                }
            else
                AppState.apiClient.queryDb(faUpdateQuery(fl))
                .onComplete {
                    case Success(res) => println(s"Updated from ${fl.originalFoodName} to ${fl.foodName}")
                    case Failure(e) => val ApiError(_, msg) = e
                }

        }
        fetchCurrentAssignments
    }

    // TODO: Make functional
    def addAssignment(e: dom.DragEvent) = {
        e.preventDefault()
        e.target.asInstanceOf[dom.raw.HTMLElement].style.border = "2px solid #00000000"
        val self = e.target.asInstanceOf[dom.raw.HTMLElement]
        if (foodAssignmentData == None)
            foodAssignmentData = Some(Seq())
        val s: Seq[DbRow] = foodAssignmentData.get
        // TODO: If needed, add fields
        println(s)
        val dbRow = new DbRow(js.Array(
            isoDate(date),
            "<kind>",
            "<option>",
            e.dataTransfer.getData("name"),
            e.dataTransfer.getData("id").toInt
        ), js.Array())
        println(dbRow)
        s :+ dbRow
        foodAssignmentData.get.map {
            row => println(row("name").toString)
        }
        // change = !change
    }

    def renderControls =
        div(cls := "controls borderRadius",
            label(cls := "dateStart",
                span(cls := "borderShadowColor3 bgColor2 borderRadius",
                    "Date"),
                textInput(isoDate(date),
                    { str => date =
                        (if (str.isEmpty()) date else new js.Date(str))},
                    "date"
                ),
            ),
            button("Save", onClick := save)
        )

    def renderFoods =
        div(cls := "foods",
            (if (foodData == None)
                None
            else
                foodData.get.map { row =>
                    div(cls := "foodDraggable",
                        id := s"foodDraggable_$i",
                        draggable := "true",
                        onDragstart := { e: dom.DragEvent => {
                            e.dataTransfer.setData("name", row("name").toString)
                            e.dataTransfer.setData("id", row("id").toString)
                        }},
                        new Food(row("name").toString),
                    )
                }
            ),
        )

    def renderAssignments: VNodeFrag = {
        return div(cls := "assignment",
            foodLanders.map { lander => div(lander) },
            span(cls := "newAssignment", "DROP TO ADD NEW",
                onDragover := { e: dom.DragEvent => {
                    e.preventDefault()
                    e.target.asInstanceOf[dom.raw.HTMLElement].style.border = "2px solid black"
                }},
                onDragleave := { e: dom.DragEvent => {
                    e.preventDefault()
                    e.target.asInstanceOf[dom.raw.HTMLElement].style.border = "2px solid #00000000"
                }},
                onDrop := addAssignment),
        )
    }

    def render: VNode = {
        fetchFoodAssignmentsIfDateDiff
        return scoped(div(cls := "foodAssignment",
            renderControls,
            renderFoods,
            renderAssignments,
        ))
    }

    cssScoped { import liwec.cssDsl._
        c.foodAssignment (
            width := "80%",
            backgroundColor := "#dddddd",
            margin := "3em auto 0 auto",
            padding := "1em",
            display := "grid",
            gridTemplateColumns := "3fr 2fr",
            gridGap := "0.5em",

            (c.controls | c.foods | c.assignment) (
                backgroundColor := "#bbbbbb",
                padding := "0.7em",
            ),

            c.controls (
                gridRow := "1",
                gridColumn := "1 / 3",                
                display := "flex",
                flexDirection := "row",
                backgroundColor := "#265976",
                color := "white",

                e.span (
                    padding := "4px 6px 2px 6px",
                    marginBottom := "0.3em",
                ),

                e.label (
                    display := "flex",
                    flexDirection := "column",
                    marginRight := "0.5em",
                ),
            ),

            c.foods (
                gridRow := "2",
                gridColumn := "2",

                (c.foodDraggable /+ c.foodDraggable) (
                    marginTop := "0.3em",
                )
            ),

            c.assignment (
                gridRow := "2",
                gridColumn := "1",

                (e.div /+ e.div) (marginTop := "0.5em"),

                c.newAssignment (
                    border := "2px solid #00000000",
                    display := "block",
                    padding := "0.7em",
                    marginTop := "0.7em",
                    textAlign := "center",
                    backgroundColor := "grey",
                )
            )
        )
    }
}
