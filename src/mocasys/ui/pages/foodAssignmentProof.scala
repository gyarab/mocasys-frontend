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

class FoodAssignmentPageProofOfConcept extends Component {

    override def onMount() {
        val e: Option[dom.DragEvent] = None
    }

    def imgOnDragstart(e: dom.DragEvent) =
        e.dataTransfer.setData("text", e.target.asInstanceOf[dom.raw.HTMLElement].id) 

    def render: VNode = scoped(div(cls := "foodAssignment",
        div(cls := "draggables",
            img(id := "id1", cls := "draggable", src := "/assets/got.jpg", draggable := "true",
                onDragstart := imgOnDragstart,
            ),
        ),
        div(cls := "dropAreas",
            (1 to 3).map { _ =>
                div(cls := "dropArea",
                    onDrop := { e: dom.DragEvent => {
                        e.preventDefault()
                        val self = e.target.asInstanceOf[dom.raw.HTMLElement]
                        val data = e.dataTransfer.getData("text")
                        val nodeCopy = dom.document
                            .getElementById(data).cloneNode(true).asInstanceOf[dom.raw.HTMLElement];
                        nodeCopy.id = "newId"
                        nodeCopy.addEventListener("ondragstart", imgOnDragstart)
                        self.innerHTML = ""
                        self.appendChild(nodeCopy)
                    }},
                    onDragover := { e: dom.DragEvent => e.preventDefault() },
                )
            }
        ),
    ))

    cssScoped { import liwec.cssDsl._
        c.foodAssignment (
            width := "70%",
            backgroundColor := "lightgrey",
            margin := "3em auto 0 auto",
            padding := "1em",

            e.img (
                width := "100px",
                height := "100px",
            ),

            c.dropAreas (
                marginTop := "1em",
                height := "15em",
                backgroundColor := "salmon",

                c.dropArea (
                    display := "inline-block",
                    margin := "1em",
                    padding := "1em",
                    width := "100px",
                    height := "100px",
                    backgroundColor := "red",
                ),
            )
        )
    }
}
