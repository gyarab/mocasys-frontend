package mocasys

import scala.util.{Success, Failure}
import scala.scalajs.js
import js.JSConverters._
import scalajs.js.annotation._
import org.scalajs.dom.ext._
import scala.concurrent.ExecutionContext.Implicits.global

object ApiClient {
    class PasswordLoginRequest(
            val username: String,
            val password: String,
        ) extends js.Object
    class PasswordLoginResp(val sessionToken: String) extends js.Object

    class QueryDbRequest(
            val query_str: String,
            val data: js.Array[js.Any],
        ) extends js.Object
    class DbField(
            val columnName: String,
            val tableName: String,
            val dataTypeName: String,
            val dataTypeSize: Int,
            val dataTypeModifier: Int,
            val format: String,
        ) extends js.Object
    class QueryDbResp(
            val rows: js.Array[js.Array[js.Any]],
            val rowCount: Int,
            val fields: js.Array[DbField],
        ) extends js.Object
    //class QueryDbRespSc(
    //        val rows: Seq[Seq[Any]],
    //        val rowCount: Int,
    //        val fields: Seq[DbField],
    //    )
    //
    //def parseQdbRows(rows: js.Array[js.Array[js.Any]]): Seq[Seq[Any]] =
    //    for (row <- rows) yield Seq(for (dp <- row) yield dp)
    
    //def toQdbResp(respJs: QueryDbRespJs): QueryDbResp =
    //    QueryDbResp(parseQdbRows(respJs.rows), respJs.rowCount, respJs.asInstanceOf[Seq[DbField]])
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

    def queryQdbRaw(query: String, params: Seq[js.Any] = Seq()) =
        Ajax.post(
            s"$apiUrl/qdb",
            data = js.JSON.stringify(
                new QueryDbRequest(query, params.toJSArray)),
            headers = Map(
                "Content-Type" -> "application/json",
                "Authorization" -> s"Token ${this.authToken.getOrElse("")}",
            ))

    def queryDb(query: String, params: Seq[js.Any] = Seq()) =
        queryQdbRaw(query, params)
        .transform {
            case Success(xhr) =>
                Success(js.JSON.parse(xhr.responseText).asInstanceOf[QueryDbResp])
            case Failure(e) => { Failure(e) }
        }
    
    def multiQueryDb(queries: Seq[String], params: Seq[js.Any] = Seq()) =
        queryQdbRaw(queries.mkString(" "), params)
        .transform {
            case Success(xhr) => Success(js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[QueryDbResp]])
            case Failure(e) => { Failure(e) }
        }

}
