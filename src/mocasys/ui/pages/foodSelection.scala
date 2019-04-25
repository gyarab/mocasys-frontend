package mocasys.ui.pages

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

class FoodSelection extends Component {
    def render = scoped(
        div(cls := "foodSelection",
            // button("Save", cls := "saveButton", onClick := { e =>
            //     println("save!");
            // }),

            new FoodList()
        )
    )

    cssScoped { import liwec.cssDsl._
        c.foodSelection -> (
            marginLeft := "14em", // Placeholder
            backgroundColor := "white",
            height := "100%",
            padding := "1.5em",

            c.saveButton -> (
                backgroundColor := "#2196F3",
                padding := "0.5em 1em",
                border := "#d3d3d3",
                borderRadius := "5px",
            ),
        )
    }
}
