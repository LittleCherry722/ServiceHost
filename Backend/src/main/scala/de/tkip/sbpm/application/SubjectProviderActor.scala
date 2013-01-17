package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

class SubjectProviderActor(val userID: UserID, val processManagerRef: ProcessManagerRef) extends Actor {

  val processIDs = collection.mutable.Set[ProcessID]()

  def receive = {
    case spc: SubjectProviderCreated =>
      processManagerRef.forward(spc)

    case pc: ProcessInstanceCreated =>
      processIDs += pc.processInstanceID
      context.parent.forward(pc)

    case message: AnswerAbleMessage =>
      // just forward all messages from the frontend which are not
      // required in this Actor
      processManagerRef.forward(message)

    case message: AnswerMessage[_] =>
      // send the Answermessages to the SubjectProviderManager

      // TODO forward oder tell?
      context.parent ! message

    case s =>
      println("SubjectProvider not yet implemented: " + s)
  }
}
