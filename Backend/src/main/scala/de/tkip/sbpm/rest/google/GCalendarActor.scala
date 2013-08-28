package de.tkip.sbpm.rest.google

import scala.concurrent.Future

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.pipe

import GCalendarCtrl._

object GCalendarActor {
  case class CreateEvent(userId: String, summary: String, location: String,
                         year: Int, month: Int, day: Int)
}

class GCalendarActor extends Actor {
  import GCalendarActor._

  implicit val ec = context.dispatcher
  val gCalCtrl = new GCalendarCtrl()

  def receive = {
    case CreateEvent(u,s,l,y,m,d) =>
      println("contacting Google API...")
      Future { gCalCtrl.createEvent(u, s, l, y, m, d) } pipeTo sender
  }

}