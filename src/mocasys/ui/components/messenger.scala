package mocasys.ui.components

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import scalajs.js
import scalajs.js.annotation._
import org.scalajs.dom
import org.scalajs.dom.ext._
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import mocasys._

trait Message {
    def render(): VNodeFrag
    def duration: Option[Int] // In milliseconds, None -- infinite
}

class InfoMessage(val msg: String, val dur: Option[Int] = Some(4000))
        extends Message {
    def render() = Seq(cls := "infoMessage", msg: VNodeFrag)
    def duration = dur
}

class ErrorMessage(val msg: String, val dur: Option[Int] = None)
        extends Message {
    def render() = Seq(cls := "errorMessage", msg: VNodeFrag)
    def duration = dur
}

class Messenger extends Component {
    var messages: Map[Int, Message] = Map()

    def addMessage(msg: Message) = {
        val key = Random.nextInt
        messages += (key -> msg)
        // TODO: Address the whole problem of "prior references"
        // (i.e. references to the underlying object instead of the proxy
        this.changeCallbacks.foreach(_(this))
        msg.duration.foreach(dur =>
            js.timers.setTimeout(dur) {
                messages -= key
                this.changeCallbacks.foreach(_(this))
            }
        )
    }

    def render = scoped(div(cls := "messages",
        messages.map { case (key, msg) =>
            p(msg.render())
        }
    ))

    cssScoped { import liwec.cssDsl._
        c.messages (
            position := "absolute",
            right := "1em",
            top := "1em",
            backgroundColor := "#d23a3f",
            color := "#f1ffff",
            fontWeight := "700",
            borderRadius := "3px",
            paddingRight := "2em",
            paddingLeft := "2em",
        )
    }
}
