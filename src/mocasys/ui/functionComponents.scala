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

    def radioCheckboxInput(radio: Boolean, idAttrVal: String, nameAttrVal: String,
            onChange: String => Unit,
            checkedB: Boolean, disabledB: Boolean) =
        label(cls := "radioCheckboxInput " + (if (radio) "radioInput" else "checkboxInput"),
            forAttr := idAttrVal,
            input(typeAttr := (if (radio) "radio" else "checkbox"),
                id := idAttrVal,
                name := nameAttrVal,
                onInput := {
                    e => onChange(e.target.asInstanceOf[dom.raw.HTMLInputElement].value)
                },
                (if (checkedB) checked := "true" else None),
                (if (disabledB) disabled := "true" else None),
            ),
            span(cls := "checkmark"),
        )

    def radioInput(idAttrVal: String, nameAttrVal: String,
                    onChange: String => Unit,
                    checkedB: Boolean = false, disabledB: Boolean = false) =
        radioCheckboxInput(true, idAttrVal, nameAttrVal,
            onChange, checkedB, disabledB)

    def checkboxInput(idAttrVal: String, nameAttrVal: String,
                    onChange: String => Unit,
                    checkedB: Boolean = false, disabledB: Boolean = false) =
        radioCheckboxInput(false, idAttrVal, nameAttrVal,
            onChange, checkedB, disabledB)

    // Pass an empty string to be invisible
    def errorBox(message: String) =
        div(cls := "errorMessage", message match {
            case "" => message
            case _ => span(cls := "bgColor6 borderRadius", message)
        }),

}
