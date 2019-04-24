package mocasys.ui.pages

import liwec._
import liwec.htmlDsl._
import mocasys.ui.components._

class LoginPage extends Component {
    def render() = div(new LoginForm())
}
