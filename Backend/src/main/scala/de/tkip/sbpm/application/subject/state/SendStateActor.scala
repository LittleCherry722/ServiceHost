package de.tkip.sbpm.application.subject.state

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.Array.canBuildFrom
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.history.{
  Transition => HistoryTransition,
  Message => HistoryMessage,
  State => HistoryState
}
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.application.RequestUserID
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.event.Logging
import scala.collection.mutable.ArrayBuffer
import de.tkip.sbpm.application.subject.StateData
import de.tkip.sbpm.application.subject.ExecuteAction
import de.tkip.sbpm.application.subject.BehaviorStateActor
import de.tkip.sbpm.application.subject.ActionExecuted
import de.tkip.sbpm.application.subject.ActionData
import de.tkip.sbpm.application.subject.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.Stored
import de.tkip.sbpm.application.subject.TargetUser
import de.tkip.sbpm.application.subject.MessageIDProvider

protected case class SendStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  import scala.collection.mutable.{ Map => MutableMap }

  private var remainingStored = 0
  private var messageContent: Option[String] = None
  private val unsentMessageIDs: MutableMap[MessageID, Transition] =
    MutableMap[MessageID, Transition]()

  // TODO
  private val sendTransition: Transition =
    transitions.find(_.isExitCond).get
  private val sendExitCond = sendTransition.myType.asInstanceOf[ExitCond]
  private val sendTarget = sendExitCond.target.get

  //  override def preStart() {
  // TODO so ist das noch nicht, besser machen!
  // ask the ContextResolver for the targetIDs
  // store them in a val
  implicit val timeout = Timeout(2000)

  var targetUserIDs: Option[Array[UserID]] = None

  override def preStart() {
    blockingHandlerActor ! BlockUser(userID)

    ActorLocator.contextResolverActor ! (RequestUserID(
      SubjectInformation(sendTransition.subjectID),
      TargetUsers(_)))

    super.preStart()
  }
  private case class TargetUsers(users: Array[UserID])

  protected def stateReceive = {
    case TargetUsers(userIDs) if (!targetUserIDs.isDefined) => {
      targetUserIDs = Some(userIDs)
      blockingHandlerActor ! UnBlockUser(userID)
    }

    case action: ExecuteAction if ({
      // the message needs a content
      action.actionData.messageContent.isDefined
    }) => {
      if (!messageContent.isDefined) {
        val input = action.actionData
        // send subjectInternalMessage before sending executionAnswer to make sure that the executionAnswer 
        // can be blocked until a potentially new subject is created to ensure all available actions will 
        // be returned when asking
        messageContent = input.messageContent
        for (transition <- exitTransitions if transition.target.isDefined) yield {

          blockingHandlerActor ! BlockUser(userID) // TODO handle several targetusers

          val messageType = transition.messageType
          val toSubject = transition.subjectID
          val messageID = nextMessageID
          unsentMessageIDs(messageID) = transition

          logger.debug("Send@" + userID + "/" + subjectID + ": Message[" +
            messageID + "} \"" + messageType + "to " + transition.target +
            "\" with content \"" + messageContent.get + "\"")

          // This ArrayBuffer stores the user, which should be blocked
          // one user can blocked several times
          val blockUsers = ArrayBuffer[UserID]()

          val target = transition.target.get
          if (target.toVariable) {
            target.insertVariable(variables(target.variable.get))
            for ((_, userID) <- target.varSubjects) { blockUsers += userID }
          } else if (targetUserIDs.isDefined) {
            val userIDs = targetUserIDs.get

            if (userIDs.length == target.min == target.max) {
              target.insertTargetUsers(userIDs)
            } else if (input.targetUsersData.isDefined) {
              val targetUserData = input.targetUsersData.get
              // TODO validate?!
              target.insertTargetUsers(targetUserData.targetUsers)
            } else {
              // TODO error?
            }

            blockUsers ++= target.targetUsers
          }

          // block the target users for this message
          for (userID <- blockUsers) {
            blockingHandlerActor ! BlockUser(userID)
          }

          remainingStored += target.min

          // send the message over the process instance
          processInstanceActor !
            SubjectToSubjectMessage(
              messageID,
              userID,
              subjectID,
              target,
              messageType,
              messageContent.get,
              input.fileId)

          // send the ActionExecuted to the blocking actor, it will send it 
          // to the process instance, when this user is ready
          blockingHandlerActor ! ActionExecuted(action)
        }
      } else {
        logger.error("Second send-message action request received")
      }
    }

    case Stored(messageID) if ({
      messageContent.isDefined &&
        unsentMessageIDs.contains(messageID)
      // TODO might remove the message ID from unsentMessageIDs?
    }) => {
      val transition = unsentMessageIDs(messageID)
      // Create the history message
      val message =
        HistoryMessage(messageID, transition.messageType, subjectID, transition.subjectID, messageContent.get)
      // Change the state and enter the History entry
      remainingStored -= 1
      if (remainingStored == 0) {
        changeState(transition.successorID, message)
        blockingHandlerActor ! UnBlockUser(userID)
      }
    }
  }

  // TODO only send targetUserData when its not trivial
  override protected def getAvailableAction: Array[ActionData] =
    Array(
      ActionData(
        sendTransition.messageType,
        !messageContent.isDefined && targetUserIDs.isDefined && targetUserIDs.get.length >= sendTarget.min,
        exitCondLabel,
        relatedSubject = Some(sendTransition.subjectID),
        targetUsersData =
          Some(TargetUser(sendTarget.min, sendTarget.max, targetUserIDs.getOrElse(Array())))))

  /**
   * Generates a new message ID
   */
  private def nextMessageID: Int = MessageIDProvider.nextMessageID()
}