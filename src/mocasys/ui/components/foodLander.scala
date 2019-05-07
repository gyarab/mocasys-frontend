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
import mocasys.ui.main._
import mocasys.ui.tables._
import mocasys.ApiClient._

// Used on the food assignment page
class FoodLander(var kind: String = "",
                var option: String = "",
                var foodName: String = "") extends Component {
    var originalKind = kind
    var originalOption = option
    var originalFoodName = foodName

    def changed: Boolean =
        originalKind != kind || originalOption != option ||
        originalFoodName != foodName

    def render = scoped(div(cls := "foodLander " + (if (changed) "changed" else ""),
        textInput(kind, { s => kind = s }),
        textInput(option, { s => option = s }),
        div(cls := "lander",
            onDrop := { e: dom.DragEvent => {
                // TODO: Check that the dropped element is supposed to be here
                e.preventDefault()
                val self = e.target.asInstanceOf[dom.raw.HTMLElement]
                foodName = e.dataTransfer.getData("name")
            }},
            onDragover := { e: dom.DragEvent => e.preventDefault() },
            onDragleave := { e: dom.DragEvent => e.preventDefault() },
            (if (!foodName.isEmpty) div(new Food(foodName)) else None)
        ),
        button("X", onClick := { e => {
            foodName = originalFoodName
            kind = originalKind
            option = originalOption
        }})
    ))

    cssScoped { import liwec.cssDsl._
        c.foodLander (
            padding := "0.2em",
            display := "grid",
            gridTemplateColumns := "1fr 1fr 3fr auto",
            gridTemplateRows := "1fr",
            gridColumnGap := "0.5em",
            gridAutoFlow := "row",

            e.input (
                width := "8em",
                height := "2.5em",
            ),

            c.lander (
                gridColumn := "3",
                backgroundColor := "#ff9b20",

                e.span (
                    display := "block",
                ),
            ),

            e.button (
                width := "1.5em",
                textAlign := "center",
            ),
        )

        c.changed (
            backgroundColor := "#ff9b20",
        )
    }
}
