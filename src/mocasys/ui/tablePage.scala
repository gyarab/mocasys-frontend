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

abstract class TablePage(val paging: Boolean = false) extends Component {
    var error: String = ""
    val name: String = "<name>"
    var page: Integer = 1
    val limit: Integer = 50

    final def offset = (page - 1) * limit

    def onChangePage(str: String) =
        scala.util.Try(str.toInt) match {
            case Success(p) => if (p > 0) page = p
                else page = 1
            case Failure(_) => page = 1
        }

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
                    (if (paging) textInput(page.toString, onChangePage, "number") else None),
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
                paddingTop := "1em",
                paddingBottom := "1em",
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
                    margin := "1em",
                ),
            ),

            c.form (
                gridColumn := "1 / 3",
                gridRow := "3",
                marginLeft := "1em",
                paddingBottom := "0.5em",

                e.input (
                    marginLeft := "0.5em",
                    marginRight := "0.5em",
                ),
                
                e.div (
                    paddingBottom := "0.5em",
                ),
                
                RawSelector("type=submit") (
                    padding := "0.3em 0.6em",
                    border := "0",
                    borderRadius := "3px",
                ),

                RawSelector("type=submit:hover") (
                    boxShadow := "5px 5px 10px 0px rgba(0, 0, 0, 0.6)",
                    transform := "translateY(-2px)",
                    backgroundColor := "#ff9b20",
                ),

                RawSelector("type=submit:active") (
                    transform := "translateY(0px)",
                    backgroundColor := "#d23a3f",
                ),
            ),

            c.table (
                gridRow := "4",
                gridColumn := "1 / 3",
            ),
        )

        (c.tablePage /> (e.div /+ e.div)) (
            marginTop := "1em",
            paddingTop := "1em",
        )
    }
}
