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
    class PasswordLoginResp(val sessionToken: String) extends js.Object

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
        def apply(fieldName: String) = {
            // TODO: Map for more efficiency? It'd need to be cached at the
            // QueryDbResp level
            val fieldIndex =
                fields.zipWithIndex
                .filter { case (c, i) => c.columnName == fieldName }
                .map { case (c, i) => i }
                .head
            data(fieldIndex)
        }

        def apply(i: Int) = data(i)
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

    def queryDbRaw(query: String, params: Seq[String] = Seq()) =
        Ajax.post(
            s"$apiUrl/qdb",
            data = js.JSON.stringify(
                new QueryDbRequest(query, params.toJSArray)),
            headers = Map(
                "Content-Type" -> "application/json",
                "Authorization" -> s"Token ${this.authToken.getOrElse("")}",
            ))

    def queryDb(query: String, params: Seq[String] = Seq()) =
        queryDbRaw(query, params)
        .transform {
            case Success(xhr: dom.XMLHttpRequest) =>
                Success(js.JSON.parse(xhr.responseText).asInstanceOf[QueryDbResp])
            case Failure(e) => { Failure(e) }
        }
    
    def multiQueryDb(queries: Seq[String], params: Seq[String] = Seq()) =
        queryDbRaw(queries.mkString(" "), params)
        .transform {
            case Success(xhr: dom.XMLHttpRequest) =>
                Success(js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[QueryDbResp]])
            case Failure(e) => { Failure(e) }
        }

}
