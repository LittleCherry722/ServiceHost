package de.tkip.sbpm.logging

import java.io.ByteArrayOutputStream

import ch.qos.logback.core.AppenderBase
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import de.tkip.sbpm.persistence.schema.{Logs, Log}
import scala.slick.jdbc.meta.MTable

import de.tkip.sbpm.persistence.schema.Schema.driver
import driver.simple._
import Logs.logs

import de.tkip.sbpm.persistence.schema.{Log, Logs}

class SlickAppender extends AppenderBase[ILoggingEvent] {

  val outStream = new ByteArrayOutputStream()
  var encoder = new PatternLayoutEncoder()
  val db = Database.forURL("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
  db withSession { implicit session =>
    if (!MTable.getTables.list.exists(_.name.name == logs.baseTableRow.tableName))
      logs.ddl.create
  }

  override def start() {
    if (this.encoder == null) {
      addError(s"No encoder set for the appender named [$name].")
      return
    }
    encoder.init(outStream)
    super.start()
  }

  def append(event: ILoggingEvent) {
    this.encoder.doEncode(event)
    val encodedMsg = new String(outStream.toByteArray())
    outStream.reset()
    db withSession { implicit session =>
      logs.insert(Log(event.getTimeStamp, encodedMsg))
    }
  }

  def getEncoder(): PatternLayoutEncoder = encoder
  def setEncoder(encoder: PatternLayoutEncoder) { this.encoder = encoder }

}