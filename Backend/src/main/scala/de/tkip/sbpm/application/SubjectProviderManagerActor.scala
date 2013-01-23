package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage

class SubjectProviderManagerActor(val processManagerRef: ProcessManagerRef)
    extends Actor {
  private var subjectCount = 0
  private val subjectProviderMap =
    collection.mutable.Map[UserID, SubjectProviderRef]()

  def receive = {
    // create a new subject provider and send the ID to the requester.
    // additionally send it to the subjectprovider who forwards 
    // the message to the processmanager so he can register the new subjectprovider
    case csp: CreateSubjectProvider =>
      createNewSubjectProvider(subjectCount)
      sender ! SubjectProviderCreated(csp, subjectCount)
      subjectCount += 1

    case gpr: ExecuteRequest =>
      forwardControlMessageToProvider(gpr.userID, gpr)

    case sra: ExecuteRequestAll =>
      forwardControlMessageToProvider(sra.userID, sra)

    case rp: ReadProcess =>
      forwardControlMessageToProvider(rp.userID, rp)

    case cp: CreateProcess =>
      forwardControlMessageToProvider(cp.userID, cp)

    case ra: RequestAnswer =>
      forwardControlMessageToProvider(ra.processID, ra)

    case cp: CreateProcessInstance =>
      cp.sender = sender
      forwardControlMessageToProvider(cp.userID, cp)

    case ea: ExecuteAction =>
      if (subjectProviderMap.contains(ea.userID)) {
        ea.sender = sender
        subjectProviderMap(ea.userID) ! ea
      }

    case kill: KillProcess =>
      processManagerRef ! kill

    // general matching:
    // first match the answers
    // then SubjectProviderMessages

    case answer: AnswerMessage[_] => {
      if (answer.sender != null)
        answer.sender ! answer
    }

    case message: SubjectProviderMessage[_] => {
      // TODO im moment doppelt drin
      val userID: UserID = message.subjectProviderID
      if (subjectProviderMap.contains(userID)) {
        subjectProviderMap(userID).forward(withSender(message))
      }
    }

    case s => {
      println("SubjectProviderManger not yet implemented: " + s)
    }
  }

  /**
   * Sets the sender of the message if the message is AnswerAble
   * and returns the message
   */
  private def withSender(message: Any) = {
    message match {
      case answerAble: AnswerAbleMessage => answerAble.sender = sender
      case _ =>
    }
    message
  }

  // forward control message to subjectProvider that is mapped to a specific userID
  private def forwardControlMessageToProvider(userID: UserID,
                                              controlMessage: ControlMessage) {
    if (subjectProviderMap.contains(userID)) {
      if (controlMessage.isInstanceOf[AnswerAbleMessage]) {
        controlMessage.asInstanceOf[AnswerAbleMessage].sender = sender
      }

      subjectProviderMap(userID).forward(controlMessage)
    }
  }

  // creates a new subject provider and registers it with the given userID 
  // (overrides the old entry)
  private def createNewSubjectProvider(userID: UserID) = {
    val subjectProvider =
      context.actorOf(Props(new SubjectProviderActor(userID, processManagerRef)))
    subjectProviderMap += userID -> subjectProvider
    subjectProvider
  }

  // kills the subject provider with the given userID and unregisters it
  private def killSubjectProvider(userID: UserID) = {
    if (subjectProviderMap.contains(userID)) {
      context.stop(subjectProviderMap(userID))
      subjectProviderMap -= userID
    }
  }

}
