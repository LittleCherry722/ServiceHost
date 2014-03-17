package de.tkip.sbpm.rest.google

import scala.concurrent.Future

import akka.actor.{ActorSystem, Props}
import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.pattern._
import akka.pattern.pipe

import GCalendarCtrl._

object GCalendarActor {
  case class CreateEvent(userId: String, summary: String, location: String,
                         year: Int, month: Int, day: Int)
}

class GCalendarActor extends InstrumentedActor {
  import GCalendarActor._

  implicit val ec = context.dispatcher
  val gCalCtrl = new GCalendarCtrl()

  def wrappedReceive = {
    case CreateEvent(u,s,l,y,m,d) =>
      println("contacting Google API...")
      Future { gCalCtrl.createEvent(u, s, l, y, m, d) } pipeTo sender
  }

}
