package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

class SubjectProviderActor(val processManagerRef: ProcessManagerRef) extends Actor {

  def receive = {
    case gpr: StatusRequest =>
      processManagerRef ! gpr

    case as: AddState =>
      processManagerRef ! as

    case _ => "not yet implemented"
  }
}