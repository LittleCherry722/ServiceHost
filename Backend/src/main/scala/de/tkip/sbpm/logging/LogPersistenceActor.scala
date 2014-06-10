package de.tkip.sbpm.logging

import akka.actor.{Actor}
import de.tkip.sbpm.persistence.schema.{Log, Logs}

import scala.slick.jdbc.meta.MTable
import scala.slick.driver.SQLiteDriver.simple._
import Database.threadLocalSession

object LogPersistenceActor {
  case class Insert(log: Log)
  case class Get(n: Int)
}

class LogPersistenceActor extends Actor with DefaultLogging {
  import LogPersistenceActor._
  val db = Database.forURL("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
  db withSession {
    if (!MTable.getTables.list.exists(_.name.name == Logs.tableName))
      Logs.ddl.create
  }

  def receive = {
    case Get(n) => db withSession {
      val log_list: List[Log] = Query(Logs).sortBy(_.timestamp.desc).take(n).list
      log.debug("TRACE: from " + this.self + " to " + sender + " " + log_list)
      sender ! log_list
    }
    case Insert(log) => db withSession {
      Logs.insert(log)
    }
  }

}
