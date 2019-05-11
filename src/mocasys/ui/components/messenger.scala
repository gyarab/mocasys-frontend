package mocasys.ui.components

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.Map
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

case class Message(val message: String, val duration: Int)

class Messenger extends Component {
    var messages: Map[Int, Message] = Map()

    def addMessage(msg: Message) = {
        val key = Random.nextInt
        messages += (key -> msg)
        // TODO: Address the whole problem of "prior references"
        // (i.e. references to the underlying object instead of the proxy
        this.changeCallbacks.foreach(_(this))
        js.timers.setTimeout(msg.duration) {
            println(s"remove ${key}")
            messages -= key
            this.changeCallbacks.foreach(_(this))
        }
        println(messages)
    }

    def render = scoped(div(cls := "messages",
        messages.map { case (key, msg) =>
            p(msg.message)
        }
    ))

    cssScoped { import liwec.cssDsl._
        c.messages (
            position := "absolute",
            right := "1em",
            top := "1em",
            backgroundColor := "red",
            width := "5em",
        )
    }
}
