package de.tkip.sbpm.logging

import java.io.ByteArrayOutputStream

import ch.qos.logback.core.AppenderBase
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.encoder.PatternLayoutEncoder

class SlickAppender extends AppenderBase[ILoggingEvent] {

  val outStream = new ByteArrayOutputStream()
  val encoder = new PatternLayoutEncoder()

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
  }

  def getEncoder(): PatternLayoutEncoder = encoder
  def setEncoder(encoder: PatternLayoutEncoder) { this.encoder = encoder }

}