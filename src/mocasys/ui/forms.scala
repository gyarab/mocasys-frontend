package mocasys.ui

import scala.util.{Success, Failure}
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import liwec._
import liwec.htmlDsl._
import mocasys.ui.functionComponents._
import mocasys.ApiClient._
import mocasys.AppState

package object forms {
    class Form(
            val parent: Component,
            var data: mutable.Map[String, Any],
            var inDb: Boolean = false) {
        def this(parent: Component, data: Map[String, Any], inDb: Boolean) =
            this(parent, mutable.Map() ++ data, inDb)
        def this(parent: Component, data: Map[String, Any]) =
            this(parent, mutable.Map() ++ data, false)
        def this(parent: Component, data: DbRow) =
            this(parent, data, true)

        var error: Option[String] = None

        def setData(key: String, value: Any) = {
            data(key) = value
            parent.vm.foreach(Component.queueRedraw)
        }

        def text(key: String) =
            textInput(data(key).asInstanceOf[String], { data(key) = _ })

        def textInt(key: String) =
            textInput(data(key).asInstanceOf[Integer].toString,
                      { v => data(key) = v.toInt })

        def errorText() =
            error.map(e => div(cls := "formError", e))

        def save(label: String, tableName: String, pkeys: Seq[String]) =
            input(typeAttr := "submit", value := label,
                  onClick := { _ => sqlSave(tableName, pkeys) })

        def sqlSave(tableName: String, pkeys: Seq[String]) = {
            // TODO: Proper query builder
            // TODO: Get primary key columns from DB
            val (query, params) =
                if(inDb) {
                    val colVals =
                        data
                        .toSeq
                        .filter { case (k, _) => !pkeys.contains(k) }
                    val setExprs =
                        colVals
                        .map(_._1)
                        .zipWithIndex
                        .map { case (col, i) => s"$col = ${"$"}${i + 1}" }
                        .mkString(",\n")
                    val conds =
                        pkeys
                        .zipWithIndex
                        .map { case (col, i) =>
                            s"$col = ${"$"}${i + 1 + colVals.length}" }
                        .mkString("\nAND ")
                    val params =
                        (colVals.map(_._2) ++ pkeys.map(data(_)))
                        .map(_.toString())

                    (s"""
                    UPDATE $tableName
                    SET $setExprs
                    WHERE $conds
                    """, params)
                } else {
                    val colVals = data.toSeq
                    val cols = colVals.map(_._1)
                    val valParams =
                        cols
                        .zipWithIndex
                        .map { case (_, i) => "$" + s"${i + 1}" }
                    val params = colVals.map(_._2.toString())
                    (s"""
                    INSERT INTO $tableName (${cols.mkString(", ")})
                    VALUES (${valParams.mkString(", ")})
                    """, params)
                }
            AppState.apiClient.queryDb(query, params)
            .onComplete { res =>
                res match {
                    case Success(_) => {
                        inDb = true
                        error = None
                    }
                    case Failure(e) => {
                        val ApiError(_, msg) = e
                        error = Some(msg)
                    }
                }
                parent.vm.foreach(Component.queueRedraw)
            }
        }
    }
}
