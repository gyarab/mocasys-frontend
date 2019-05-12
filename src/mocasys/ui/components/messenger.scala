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
    def setCancel(cancel: () => Unit): Unit = Unit
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

class OfflineMessage extends Message {
    var cancel: Option[() => Unit] = None

    dom.window.addEventListener("online", { (e: dom.UIEvent) =>
        cancel.map(func => func()) })

    def render() = Seq(cls := "offlineMessage", "It seems you are offline :(": VNodeFrag)
    def duration = None
    override def setCancel(cancel: () => Unit) = this.cancel = Some(cancel)
}

class Messenger extends Component {
    var messages: Map[Int, Message] = Map()

    def _removeMessage(key: Int) = {
        messages -= key
        this.changeCallbacks.foreach(_(this))
    }

    def removeMessage(key: Int, handle: Option[js.timers.SetTimeoutHandle]) = {
        handle.map(js.timers.clearTimeout(_))
        _removeMessage(key)
    }

    def addMessage(msg: Message) = {
        val key = Random.nextInt
        messages += (key -> msg)
        // TODO: Address the whole problem of "prior references"
        // (i.e. references to the underlying object instead of the proxy
        this.changeCallbacks.foreach(_(this))
        var cancel: Option[js.timers.SetTimeoutHandle] = None
        msg.duration.foreach(dur => {
            cancel = Some(js.timers.setTimeout(dur) { _removeMessage(key) })
        })
        msg.setCancel(() => removeMessage(key, cancel))
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
            color := "#f1ffff",
            fontWeight := "700",
            borderRadius := "3px",
            paddingRight := "0.2em",
            paddingLeft := "0.2em",

            e.p (
                marginTop := "0.2em",
                padding := "0.5em 2em 0.5em 2em",
                borderTop := "3px solid #ff9b20",
                backgroundColor := "#d23a3f",
            ),
        )
    }
}
