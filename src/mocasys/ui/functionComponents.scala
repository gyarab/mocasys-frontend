package mocasys.ui

import scalajs.js
import org.scalajs.dom
import liwec._
import liwec.htmlDsl._
import liwec.htmlMacros._
import liwec.cssMacros._
import liwec.cssDslTypes.RawSelector

package object functionComponents {
    def textInput(strValue: String,
                  onChange: String => Unit,
                  typ: String = "text",
                  onKeyupE: dom.KeyboardEvent => Unit = {
                      e => Unit
                  }) =
        input(typeAttr := typ, onKeyup := onKeyupE, value := strValue, onInput := {
            e => onChange(e.target.asInstanceOf[dom.raw.HTMLInputElement].value)
        })
}
