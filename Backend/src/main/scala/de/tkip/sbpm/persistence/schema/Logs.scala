package de.tkip.sbpm.persistence.schema

import scala.slick.driver.SQLiteDriver.simple._

case class Log(timestamp: Long, msg: String)

object Logs extends Table[Log]("LOGS") {
  def timestamp = column[Long]("Timestamp", O.PrimaryKey)
  def msg = column[String]("MSG")
  def * = timestamp ~ msg <> (Log, Log.unapply _)
}