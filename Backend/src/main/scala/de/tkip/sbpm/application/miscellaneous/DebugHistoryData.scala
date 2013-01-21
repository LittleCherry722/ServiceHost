package de.tkip.sbpm.application.miscellaneous

import java.util.Date
import scala.util.Random
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.application._
import akka.actor.ActorRef

object HistoryTestData {
  val random = new Random()
  def start = new Date()

  def generate(processName: String, instanceId: Int)(implicit payloadProvider: ActorRef) = {
    val h = History(processName, instanceId, start)
    addEntries(h)
    h
  }

  val sub1 = "Customer"
  val sub2 = "Supplier"

  def nextTime(time: Long) = time + random.nextInt(3600 * 1000)

  def addEntries(h: History)(implicit payloadProvider: ActorRef) {
    implicit var time = h.processStarted.getTime()
    h.entries += send(sub1, sub2, "Order request", 1, "152876(2),4547984(3),546847(1),541754(1)", null)
    time = nextTime(time)
    h.entries += receive(sub1, sub2, "Order request", 1, "152876(2),4547984(3),546847(1),541754(1)", null)
    time = nextTime(time)
    h.entries += action(sub2, "Check availability", h.entries.last.toState)
    time = nextTime(time)
    h.entries += send(sub2, sub1, "Order availability", 2, "152876(1),4547984(3),546847(0),541754(1)", null, h.entries.last.toState)
    time = nextTime(time)
    h.entries += receive(sub2, sub1, "Order availability", 2, "152876(1),4547984(3),546847(0),541754(1)", null, h.entries.head.toState)
    time = nextTime(time)
    h.entries += action(sub1, "Review order", h.entries.last.toState)
    time = nextTime(time)
    h.entries += send(sub1, sub2, "Order", 3, "152876(1),4547984(3),541754(1)", null, h.entries.last.toState)
    time = nextTime(time)
    h.entries += receive(sub1, sub2, "Order", 3, "152876(1),4547984(3),541754(1)", null, h.entries(3).toState)
    time = nextTime(time)
    h.entries += action(sub2, "Prepare delivery", h.entries.last.toState)
    time = nextTime(time)
    h.entries += send(sub2, sub1, "Goods", 4, null, List("invoice"), h.entries.last.toState)
    h.entries += end(sub2, h.entries.last.toState)
    time = nextTime(time)
    h.entries += receive(sub2, sub1, "Order availability", 4, null, List("invoice"), h.entries(4).toState)
    h.entries += end(sub1, h.entries.last.toState)
    h.processEnded = new Date(time)
  }

  def send(from: String, to: String, msgType: String, msgId: Int, payload: String, files: Seq[String], fromState: State = null)(implicit payloadProvider: ActorRef, time: Long) =
    Entry(new Date(time),
      from,
      fromState,
      State("Send " + msgType, "send"),
      createMessage(msgId, msgType, from, to, payload, files))

  def receive(from: String, to: String, msgType: String, msgId: Int, payload: String, files: Seq[String], fromState: State = null)(implicit payloadProvider: ActorRef, time: Long) =
    Entry(new Date(time),
	      to,
	      fromState,
	      State("Receive " + msgType, "receive"),
	      createMessage(msgId, msgType, from, to, payload, files))

  def createMessage(msgId: Int, msgType: String, from: String, to: String, payload: String, files: Seq[String])(implicit payloadProvider: ActorRef) =
    Message(msgId,
		      msgType,
		      from,
		      to,
		      payload,
		      if (files != null) files.map(MessagePayloadLink(payloadProvider, _)) else null)

  def action(sub: String, name: String, fromState: State = null)(implicit time: Long) =
    Entry(new Date(time),
      sub,
      fromState,
      State(name, "action"))

  def end(sub: String, fromState: State)(implicit time: Long) =
    Entry(new Date(time),
      sub,
      fromState,
      State("End", "end"))
}