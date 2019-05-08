package mocasys

import scala.util.{Success, Failure}
import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.ext._
import js.JSConverters._
import scalajs.js.annotation._
import scala.concurrent.ExecutionContext.Implicits.global

object ApiClient {
    class PasswordLoginRequest(
            val username: String,
            val password: String,
        ) extends js.Object
    class PasswordLoginResp(val sessionToken: String,
                            val expiresIn: Long) extends js.Object
    
    class PasswordChangeRequest(
            val username: String,
            val currentPassword: String,
            val newPassword: String
        ) extends js.Object

    class QueryDbRequest(
            val query_str: String,
            val data: js.Array[String],
        ) extends js.Object
    class DbField(
            val columnName: String,
            val tableName: String,
            val dataTypeName: String,
            val dataTypeSize: Int,
            val dataTypeModifier: Int,
            val format: String,
        ) extends js.Object
    case class DbRow(
            val data: js.Array[js.Any],
            val fields: js.Array[DbField]) {
        def getOption(fieldName: String) = {
            // TODO: Map for more efficiency? It'd need to be cached at the
            // QueryDbResp level
            fields.zipWithIndex
            .filter { case (c, i) => c.columnName == fieldName }
            .map { case (c, i) => i }
            .headOption
            .map(fi => data(fi))
        }
        def apply(fieldName: String) = {
            getOption(fieldName).head
        }

        def apply(i: Int) = data(i)
    }
    implicit class DbRowAsMap(val row: DbRow) extends Map[String, Any] {
        def get(key: String) = row.getOption(key)
        def + [V1 >: Any](kv: (String, V1)): Map[String, V1] = this.toMap + kv
        def - (key: String) = this.toMap - key
        def iterator = new Iterator[(String, Any)] {
            var i = 0
            def hasNext = i < row.data.length
            def next() = {
                val col = (row.fields(i).columnName, row.data(i))
                i += 1
                col
            }
        }
    }
    class QueryDbResp(
            val rows: js.Array[js.Array[js.Any]],
            val rowCount: Int,
            val fields: js.Array[DbField],
        ) extends js.Object
    // An implicit wrapper which makes it possible to treat QueryDbResp
    // as a Seq without almost any performance penalty
    implicit class QueryDbRespAsSeq(val resp: QueryDbResp) extends Seq[DbRow] {
        def apply(i: Int) = DbRow(resp.rows(i), resp.fields)
        def length = resp.rowCount
        def iterator = new Iterator[DbRow] {
            var i = 0
            def hasNext = i < resp.rowCount
            def next() = {
                val row = resp(i)
                i += 1
                row
            }
        }
    }

    case class ApiError(val httpCode: Int, val message: String)
        extends Throwable
}

class ApiClient(val apiUrl: String) {
    import ApiClient._
    var authToken: Option[String] = None

    def loginWithPassword(username: String, password: String) =
        Ajax.post(
            s"$apiUrl/auth/password",
            data = js.JSON.stringify(
                new PasswordLoginRequest(username, password)),
            headers = Map("Content-Type" -> "application/json"))
        .map { xhr =>
            val resp =
                js.JSON.parse(xhr.responseText)
                .asInstanceOf[PasswordLoginResp]
            this.authToken = Some(resp.sessionToken)
            resp
        }

    def changeLoginPassword(username: String,
                            currentPassword: String,
                            newPassword: String) =
        Ajax.put(s"$apiUrl/auth/password",
            data = js.JSON.stringify(new PasswordChangeRequest(username, currentPassword, newPassword)),
            headers = Map("Content-Type" -> "application/json")
        )

    // TODO: Trim!
    def queryDbRaw(query: String, params: Seq[Any] = Seq()) =
        Ajax.post(
            s"$apiUrl/qdb",
            data = js.JSON.stringify(
                new QueryDbRequest(query, params.map(_.toString).toJSArray)),
            headers = Map(
                "Content-Type" -> "application/json",
                "Authorization" -> s"Token ${this.authToken.getOrElse("")}",
            ))
        .transform {
            case Failure(e) => {
                val resp = e.asInstanceOf[AjaxException]
                val json = js.JSON.parse(resp.xhr.responseText)
                Failure(ApiError(resp.xhr.status, json.message.toString()))
            }
            case s => s
        }

    def queryDb(query: String, params: Seq[Any] = Seq()) =
        queryDbRaw(query, params)
        .map { xhr =>
            js.JSON.parse(xhr.responseText).asInstanceOf[QueryDbResp]
        }

    def multiQueryDb(query: String, params: Seq[String] = Seq()) =
        queryDbRaw(query, params)
        .map { xhr =>
            js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[QueryDbResp]]
        }
}
