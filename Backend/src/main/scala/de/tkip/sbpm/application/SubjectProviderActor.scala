package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

class SubjectProviderActor(val userID: UserID, val processManagerRef: ProcessManagerRef) extends Actor {

  val processIDs = collection.mutable.Set[ProcessID]()
  
  def receive = {
    case gpr: ExecuteRequest =>
      processManagerRef ! gpr

    case as: AddState =>
      processManagerRef ! as
      
    case cp: CreateProcess =>
      processManagerRef ! cp
      
    case pc: ProcessCreated =>
      processIDs += pc.processID
      context.parent ! pc

    case _ => "not yet implemented"
  }
}