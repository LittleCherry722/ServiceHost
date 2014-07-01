package de.tkip.sbpm.persistence.schema

import scala.slick.driver.SQLiteDriver.simple._
import Logs.Log

object Logs {
  case class Log(timestamp: Long, msg: String)
  val logs = TableQuery[Logs]
}

class Logs(tag: Tag) extends Table[Log](tag, "LOGS") {
  def timestamp = column[Long]("Timestamp", O.PrimaryKey)
  def msg = column[String]("MSG")
  def * = (timestamp, msg) <> (Log.tupled, Log.unapply)
}
