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
    var newFoodLanders: mutable.ArrayBuffer[FoodLander] = mutable.ArrayBuffer()
    var changed: Boolean = false
    var foodSearch: String = ""

    override def onMount() {
        fetchFood
        fetchCurrentAssignments
    }

    def foodQuery = if (foodSearch.isEmpty)
            AppState.apiClient.queryDb("SELECT id, name FROM food")
        else
            AppState.apiClient.queryDb("SELECT id, name FROM food WHERE name LIKE $1",
                Seq(s"%${foodSearch}%"))

    // TODO: Paging
    def fetchFood =
        foodQuery.onComplete {
            case Success(res) => foodData = Some(res)
            case Failure(e) => val ApiError(_, msg) = e
        }

    def fetchCurrentAssignments =
        AppState.apiClient.queryDb(
            """SELECT fa.day, fa.kind, fa.option, f.name, f.id FROM food_assignments AS fa
            LEFT JOIN food AS f ON fa.id_food = f.id
            WHERE fa.day = $1
            ORDER BY fa.kind, fa.option""",
            Seq(isoDate(date))
        ).onComplete {
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

    def faCreateQuery(fl: FoodLander) = s"""
        INSERT INTO food_assignments (day, kind, option, id_food)
        VALUES ('${isoDate(date)}', '${fl.kind}', '${fl.option}', ${fl.foodId})
    """

    def save(e: dom.Event) = {
        for (fl <- foodLanders if (fl.changed && !fl.kind.isEmpty) || fl.delete) {
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
        for (fl <- newFoodLanders if fl.changed && !fl.kind.isEmpty)
            AppState.apiClient.queryDb(faCreateQuery(fl))
            .onComplete {
                case Success(res) => println(s"Created ${fl.foodName}")
                case Failure(e) => val ApiError(_, msg) = e
            }
        newFoodLanders.clear
        fetchCurrentAssignments
    }

    def addDropAreaDefaultStyle(elem: dom.raw.HTMLElement) = {
        elem.style.transform = "translateY(0px)"
        elem.style.backgroundColor = "#f1ffff"
    }

    // TODO: Fix deletion
    def addAssignment(e: dom.DragEvent) = {
        e.preventDefault()
        addDropAreaDefaultStyle(e.target.asInstanceOf[dom.raw.HTMLElement])
        val lander: FoodLander = new FoodLander("",
            "",
            e.dataTransfer.getData("name"),
            e.dataTransfer.getData("id").toInt,
            false,
            { fl => {
                println(newFoodLanders.contains(fl))
                newFoodLanders -= fl
                println(newFoodLanders.contains(fl))
                println(newFoodLanders.length)
                changed = !changed
            }}
        )
        changed = !changed
        println(newFoodLanders.length)
    }

    def renderControls =
        div(cls := "controls borderRadius boxShadowBalanced",
            label(cls := "dateStart",
                span(cls := "borderShadowColor3 bgColor2 borderRadius",
                    "Date"),
                textInput(isoDate(date),
                    { str => if (str.isEmpty)
                        date = date
                    else {
                        date = new js.Date(str)
                        fetchCurrentAssignments
                    }},
                    "date",
                    onKeyupE = onEnter
                ),
            ),
            button("Save", onClick := save, cls := "shadowClick saveButton")
        )

    def onEnter(e: dom.KeyboardEvent) = if (e.keyCode == 13) fetchFood

    def renderFoods =
        div(cls := "foods boxShadowBalanced",
            div(cls := "foodControls",
                textInput(foodSearch,
                    { str => foodSearch = str },
                    "text",
                    placeholderVal = "Search Foods",
                    onKeyupE = onEnter),
                button("Search", onClick := { e => fetchFood })
            ),
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
        return div(cls := "assignment boxShadowBalanced",
            foodLanders.map { lander => div(lander) },
            span(cls := "newAssignment", "DROP TO ADD NEW",
                onDragover := { e: dom.DragEvent => {
                    e.preventDefault()
                    e.target.asInstanceOf[dom.raw.HTMLElement].style.transform = "translateY(-2px)"
                    e.target.asInstanceOf[dom.raw.HTMLElement].style.backgroundColor = "#ff9b20"
                }},
                onDragleave := { e: dom.DragEvent => {
                    e.preventDefault()
                    addDropAreaDefaultStyle(e.target.asInstanceOf[dom.raw.HTMLElement])
                }},
                onDrop := addAssignment),
            div(cls := "newAssignments",
                newFoodLanders.toList.map { lander => div(lander) },
            ),
        )
    }

    def render = scoped(div(cls := "foodAssignment",
            renderControls,
            renderFoods,
            renderAssignments,
        ))

    cssScoped { import liwec.cssDsl._
        c.foodAssignment (
            width := "90%",
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
                borderTop := "3px solid #3ea7b9",
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
                c.saveButton (
                    padding := "20px",
                    fontSize := "20",
                ),
            ),

            c.foods (
                gridRow := "2",
                gridColumn := "2",
                backgroundColor := "#265976",
                borderTop := "3px solid #3685a2",
                borderRadius := "3px",

                (c.foodDraggable /+ c.foodDraggable) (
                    marginTop := "0.3em",
                ),

                c.foodControls (marginBottom := "0.4em"),
            ),

            c.assignment (
                gridRow := "2",
                gridColumn := "1",
                width := "40em",
                backgroundColor := "#265976",
                borderTop := "3px solid #3ea7b9",
                borderRadius := "3px",

                (e.div /+ e.div) (marginTop := "0.5em"),

                c.newAssignment (
                    display := "block",
                    padding := "0.7em",
                    margin := "0.7em 0.3em",
                    textAlign := "center",
                    backgroundColor := "#f1ffff",
                    color := "#265976",
                    fontWeight := "550",
                )
            )
        )
    }
}
