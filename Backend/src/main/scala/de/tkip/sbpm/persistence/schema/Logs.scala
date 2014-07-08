package de.tkip.sbpm.persistence.schema

import scala.slick.driver.SQLiteDriver.simple._

object Logs {
  val logs = TableQuery[Logs]
}
case class Log(timestamp: Long, msg: String)

class Logs(tag: Tag) extends Table[Log](tag, "LOGS") {
  def timestamp = column[Long]("Timestamp", O.PrimaryKey)
  def msg = column[String]("MSG")
  def * = (timestamp, msg) <> (Log.tupled, Log.unapply)
}
