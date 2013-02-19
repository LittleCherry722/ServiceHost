package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.ActorLocator
import akka.event.Logging

class SubjectProviderManagerActor extends Actor {

  val logger = Logging(context.system, this)

  private lazy val processManagerActor = ActorLocator.processManagerActor
  private val subjectProviderMap =
    collection.mutable.Map[UserID, SubjectProviderRef]()

  def receive = {
    // create a new subject provider and send the ID to the requester.
    // additionally send it to the subjectprovider who forwards 
    // the message to the processmanager so he can register the new subjectprovider
    case csp @ CreateSubjectProvider(userID) =>
      createNewSubjectProvider(userID)
      if (subjectProviderMap.contains(userID)) {
        sender ! SubjectProviderCreated(csp, userID)
      }

    // general matching:
    // first match the answers
    // then SubjectProviderMessages
    case answer: AnswerMessage => {
      if (answer.sender != null) {
        answer.sender ! answer
      }
    }

    // TODO werden noch zu forwards aber zum routing testen erstmal tells
    case message: SubjectProviderMessage => {
      if (subjectProviderMap.contains(message.userID)) {
        subjectProviderMap(message.userID) ! withSender(message)
      } else {
        // TODO dynamisch erstellen?
        createNewSubjectProvider(message.userID)
        subjectProviderMap(message.userID).forward(withSender(message))
      }
    }

    // TODO muss man zusammenfassen koennen
    case message: PersistenceMessage => {
      processManagerActor.forward(message)
    }

    case message: AnswerAbleMessage => {
      processManagerActor ! message.withSender(sender)
    }

    case message: ControlMessage => {
      processManagerActor ! message
    }

    case message: SubjectMessage => {
      processManagerActor ! message
    }

    case message: GetAvailableActions => {
      if (subjectProviderMap.contains(message.userID)) {
        subjectProviderMap(message.userID).forward(message)
      } else {
        logger.info("Actions for subject " + message.userID + " but does not exist");
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
      context.actorOf(Props(new SubjectProviderActor(userID)))
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
