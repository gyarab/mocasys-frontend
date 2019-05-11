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
    var error: String = ""

    override def onMount() {
        fetchFood
        fetchCurrentAssignments
        onSuccess
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
    
    def faWhereFl = 
        """WHERE day = $1
        AND kind = $2
        AND option = $3
        AND id_food = $4"""
    
    def onSuccess = {
        error = ""
        AppState.messenger.addMessage(new Message("Hello!", 5000))
    }

    def onFailure(e: Throwable) = {
        val ApiError(_, msg) = e
        error = msg
    }

    def save(e: dom.Event) = {
        for (fl <- foodLanders if (fl.changed && !fl.kind.isEmpty) || fl.delete) {
            if (fl.delete)
                AppState.apiClient.queryDb("""DELETE FROM food_assignments """ + faWhereFl,
                    Seq(isoDate(date), fl.originalKind, fl.originalOption, fl.originalFoodId)
                ).onComplete {
                    case Success(res) => onSuccess
                    case Failure(e) => onFailure(e)
                }
            else
                AppState.apiClient.queryDb(
                    """UPDATE food_assignments
                    SET option = $5,
                        kind = $6,
                        id_food = $7 """ + faWhereFl,
                    Seq(isoDate(date), fl.originalKind, fl.originalOption, fl.originalFoodId,
                        fl.option, fl.kind, fl.foodId)
                ).onComplete {
                    case Success(res) => onSuccess
                    case Failure(e) => onFailure(e)
                }
        }
        for (fl <- newFoodLanders if fl.changed && !fl.kind.isEmpty)
            AppState.apiClient.queryDb("""INSERT INTO food_assignments (day, kind, option, id_food)
                VALUES ($1, $2, $3, $4)""",
                Seq(isoDate(date), fl.kind, fl.option, fl.foodId)
            ).onComplete {
                case Success(res) => onSuccess
                case Failure(e) => onFailure(e)
            }
        newFoodLanders.clear
        fetchCurrentAssignments
    }

    def addDropAreaDefaultStyle(elem: dom.raw.HTMLElement) = {
        elem.style.transform = "translateY(0px)"
        elem.style.backgroundColor = "#f1ffff"
    }

    def addAssignment(e: dom.DragEvent) = {
        e.preventDefault()
        addDropAreaDefaultStyle(e.target.asInstanceOf[dom.raw.HTMLElement])
        val lander = new FoodLander("",
            "",
            e.dataTransfer.getData("name"),
            e.dataTransfer.getData("id").toInt,
            false,
            { fl => {
                newFoodLanders -= fl
                changed = !changed
            }}
        )
        // What a hack
        newFoodLanders += lander
        lander.setNoProxySelf(lander)
        changed = !changed
    }

    def renderControls =
        div(cls := "controls borderRadius boxShadowBalanced bgColor1 borderTopColor2",
            errorBox(error),
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
        div(cls := "foods boxShadowBalanced bgColor1 borderTopColor2",
            div(cls := "foodControls",
                textInput(foodSearch,
                    { str => foodSearch = str },
                    "text",
                    placeholderVal = "Search Foods",
                    onKeyupE = onEnter),
                button("Search", cls := "btnPadding searchBtn shadowClick", onClick := { e => fetchFood })
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
        return div(cls := "assignment boxShadowBalanced bgColor1 borderTopColor2",
            foodLanders.map { lander => div(lander) },
            span(cls := "newAssignment bgColor4", "DROP TO ADD NEW",
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
                padding := "0.7em",
            ),

            c.controls (
                gridRow := "1",
                gridColumn := "1 / 3",                
                display := "flex",
                flexDirection := "row",
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
                width := "max-content",

                (c.foodDraggable /+ c.foodDraggable) (
                    marginTop := "0.3em",
                ),

                c.foodControls (marginBottom := "0.4em"),

                c.searchBtn (
                    margin := "0.5em",
                ),
            ),

            c.assignment (
                gridRow := "2",
                gridColumn := "1",
                width := "40em",

                (e.div /+ e.div) (marginTop := "0.5em"),

                c.newAssignment (
                    display := "block",
                    padding := "0.7em",
                    margin := "0.7em 0.3em",
                    textAlign := "center",
                    fontWeight := "550",
                    color := "#265976",
                )
            )
        )
    }
}
