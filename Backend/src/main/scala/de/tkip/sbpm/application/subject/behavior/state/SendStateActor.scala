/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application.subject.behavior.state

import scala.collection.mutable.ArrayBuffer
import scala.Array.canBuildFrom
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.event.Logging

import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.history.{
  Transition => HistoryTransition,
  Message => HistoryMessage,
  State => HistoryState
}
import de.tkip.sbpm
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.{ SubjectInformation, RequestUserID }
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.rest.google.GDriveActor.{ GetUrl, PublishFile }
import de.tkip.sbpm.logging.DefaultLogging
import com.google.api.services.drive.model.{ Permission }

private class GoogleSendProxyActor(
  processInstanceActor: ActorRef,
  userId: String) extends Actor with DefaultLogging {

  lazy val driveActor = sbpm.ActorLocator.googleDriveActor
  implicit val ec = context.dispatcher
  implicit val timeout = Timeout(3000)

  def receive = {
    case message: SubjectToSubjectMessage =>
      if (message.fileID.isDefined) {

        val f_url = (driveActor ? GetUrl(userId, message.fileID.get))
        val f_pub = (driveActor ? PublishFile(userId, message.fileID.get))
        for {
          url <- f_url.mapTo[String]
          pub <- f_pub.mapTo[Permission]
        } {
          message.fileUrl = Some(url)
          processInstanceActor forward message
        }
      } else {
        processInstanceActor forward message
      }
  }
}

protected case class SendStateActor(data: StateData)
  extends BehaviorStateActor(data) with ActorLogging {

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
      SubjectInformation(processID, processInstanceID, sendTarget.subjectID),
      TargetUsers(_)))

    super.preStart()
  }

  private case class TargetUsers(users: Array[UserID])

  protected def stateReceive = {

    case TargetUsers(userIDs) if (!targetUserIDs.isDefined) =>
      targetUserIDs = Some(userIDs)
      blockingHandlerActor ! UnBlockUser(userID)

    case action: ExecuteAction if (action.actionData.messageContent.isDefined) => {
      if (!messageContent.isDefined) {
        // send subjectInternalMessage before sending executionAnswer to make sure that the executionAnswer 
        // can be blocked until a potentially new subject is created to ensure all available actions will 
        // be returned when asking
        messageContent = action.actionData.messageContent

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
            } else if (action.actionData.targetUsersData.isDefined) {
              val targetUserData = action.actionData.targetUsersData.get
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
          val sendProxy = context.actorOf(Props(
            new GoogleSendProxyActor(
              processInstanceActor,
              action.userID.toString)))

          sendProxy !
            SubjectToSubjectMessage(
              messageID,
              userID,
              subjectID,
              target,
              messageType,
              messageContent.get,
              action.actionData.fileId)

          // send the ActionExecuted to the blocking actor, it will send it 
          // to the process instance, when this user is ready
          blockingHandlerActor ! ActionExecuted(action)
        }
      } else {
        logger.error("Second send-message action request received")
      }
    }

    case Stored(messageID) if (messageContent.isDefined &&
      unsentMessageIDs.contains(messageID) // TODO might remove the message ID from unsentMessageIDs?
      ) => {
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

    case Rejected(messageID) if (
      messageContent.isDefined &&
      unsentMessageIDs.contains(messageID)) => {
      log.warning("message with id {} was rejected", messageID)

      //TODO how to handle the rejected message?

      blockingHandlerActor ! UnBlockUser(userID)
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