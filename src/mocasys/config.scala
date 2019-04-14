package mocasys

import scalajs.js
import scalajs.js.annotation._

/** This object keeps global configuration options, like API URLs. It should be
 *  defined in index.html or in another JS file. The Scala code expects this
 *  object to exist before running.
 */
@js.native
@JSGlobal("mocasysConfig")
object Config extends js.Object {
    val middleendApiUrl: String = js.native
}
