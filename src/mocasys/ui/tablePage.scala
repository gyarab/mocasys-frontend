package mocasys.ui

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
import mocasys.ui.functionComponents._
import mocasys.ui.tables._
import mocasys.ui.forms._
import mocasys.ApiClient._

abstract class TablePage extends Component {
    var error: String = ""
    val name: String = "<name>"

    def setError(error: String) = this.error = error

    // UI Stuff

    def renderForm: VNodeFrag =  None

    def renderTable: VNodeFrag = None

    def renderControls: VNodeFrag = None

    def render = scoped(
        div(cls := "tablePage",
            h1(name, cls := "boxShadowBalanced bgColor1"),
            errorBox(error),
            div(cls := "pageBox bgColor1 boxShadowBalanced borderTopColor2 borderRadius",
                div(cls := "controls",
                    renderControls,
                ),
                div(cls := "form",
                    renderForm,
                ),
                div(cls := "table",
                    renderTable,
                ),
            )
        )
    )

    cssScoped { import liwec.cssDsl._
        c.tablePage (
            width := "80%",
            marginLeft := "auto",
            marginRight := "auto",
            padding := "1.5em",
            // display := "grid",
            gridTemplateColumns := "1fr 1fr",
            gridRowGap := "1em",

            e.h1 (
                fontSize := "1.5em",
                color := "f1ffff",
                borderTop := "3px solid #3685a2",
                borderRadius := "3px",
                width := "max-content",
                padding := "0.3em 0.5em",
                textAlign := "center",
            ),

            c.pageBox (
                padding := "1em",
            ),

            c.errorMessage (
                gridRow := "1",
                gridColumn := "1 / 3",
                color := "white",
            ),

            c.controls (
                gridColumn := "1 / 3",
                gridRow := "2",

                e.button (
                    //margin := "1em",
                ),
            ),

            c.form (
                gridColumn := "1 / 3",
                gridRow := "3",
            ),

            c.table (
                gridRow := "4",
                gridColumn := "1 / 3",
                paddingTop := "1em",
            ),
        )

        (c.tablePage /> (e.div /+ e.div)) (
            marginTop := "1em",
            paddingTop := "1em",
        )
    }
}
