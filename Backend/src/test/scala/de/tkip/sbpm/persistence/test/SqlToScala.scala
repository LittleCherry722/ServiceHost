package de.tkip.sbpm.persistence.test
import scala.io._
import scala.util.matching.Regex
import scala.collection.mutable.ListBuffer

object SqlToScala extends App {
  val typeMapping = Map(
    "groups" -> ("Group" -> "Some(%s), %s, %b"),
    "group_x_roles" -> ("GroupRole" -> "%s, %s, %b"),
    "group_x_users" -> ("GroupUser" -> "%s, %s, %b"),
    "process" -> ("Process" -> "Some(%1$s), %2$s, %4$s, !%5$b, %3$s"),
    "process_graphs" -> ("Graph" -> "Some(%s), %s, java.sql.Timestamp.valueOf(%s), %s"),
    "relation" -> ("Relation" -> "%s, %s, %s, %s"),
    "roles" -> ("Role" -> "Some(%s), %s, %b"),
    "users" -> ("User" -> "Some(%s), %s, %b, %s"),
    "users_x_groups" -> ("GroupUser" -> "%2$s, %1$s, %3$b"))

  val src = Source.fromFile("export.sql")
  val lines = src.getLines.filter(l => l.startsWith("INSERT") || l.startsWith("("))
  val inserts = lines.foldLeft(List[(String, Array[String], ListBuffer[Array[String]])]()) {
    (list, str) =>
      if (str.startsWith("INSERT")) {
        val name = cleanName(str.drop(13).takeWhile(_ != '('))
        val cols = str.dropWhile(_ != '(').drop(1).takeWhile(_ != ')').split(',').map(cleanName)
        list :+ (name, cols, ListBuffer[Array[String]]())
      } else {
        list.last._3 += "(\\A[(])|([)][,;]?\\z)".r.replaceAllIn(str, "").split(", ")
        list
      }
  }.foreach { e =>
    val mapping = typeMapping(e._1)
    val name = mapping._1
    println("val " + e._1 + " = List(")
    println(e._3.map { cols =>
      val colStr = mapping._2.format(cols.map(_.trim).map(_.replaceAll("(\\A')|('\\z)", "\"\"\"")): _*)
      "    "+ name + "(" + colStr + ")"
    }.mkString(",\n"))
    println(")")
  }

  def cleanName(str: String) = {
   str.trim().replaceAll("`", "")
  }
}