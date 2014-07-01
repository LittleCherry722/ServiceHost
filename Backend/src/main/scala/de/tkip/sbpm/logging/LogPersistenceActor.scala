package de.tkip.sbpm.logging

import akka.actor.{Actor}
import de.tkip.sbpm.persistence.schema.{Log, Logs}
import scala.slick.jdbc.meta.MTable
import de.tkip.sbpm.instrumentation.InstrumentedActor

import de.tkip.sbpm.persistence.schema.Schema.driver
import driver.simple._
import Logs.logs


object LogPersistenceActor {
  case class Insert(log: Log)
  case class Get(n: Int)
}

class LogPersistenceActor extends InstrumentedActor {
  import LogPersistenceActor._
  val db = Database.forURL("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
  db withSession { implicit session =>
    if (!MTable.getTables.list.exists(_.name.name == logs.baseTableRow.tableName))
      logs.ddl.create
  }

  def wrappedReceive = {
    case Get(n) => db withSession { implicit session =>
      val log_list: List[Log] = logs.sortBy(_.timestamp.desc).take(n).list
      sender !! log_list
    }
    case Insert(log) => db withSession { implicit session =>
      logs.insert(log)
    }
  }

}
