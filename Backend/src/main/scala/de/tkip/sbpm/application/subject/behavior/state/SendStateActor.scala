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

import de.tkip.sbpm.application.subject.GetCurrentCorrelation

import scala.collection.mutable.ArrayBuffer
import scala.Array.canBuildFrom
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.event.Logging

import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.history.{
Message => HistoryMessage
}
import de.tkip.sbpm
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.{SubjectInformation, RequestUserID}
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.rest.google.GDriveActor.{GetFileInfo, PublishFile}
import de.tkip.sbpm.rest.google.GDriveControl.GDriveFileInfo
import de.tkip.sbpm.logging.DefaultLogging
import com.google.api.services.drive.model.Permission
import de.tkip.sbpm.model.ChangeDataMode._
import java.util.UUID
import de.tkip.sbpm.application.subject

//import de.tkip.sbpm.application.ProcessInstanceActor.MessageContent

import de.tkip.sbpm.application.ProcessInstanceActor._

private class GoogleSendProxyActor(
                                    processInstanceActor: ActorRef,
                                    userId: String) extends InstrumentedActor with DefaultLogging {

  lazy val driveActor = sbpm.ActorLocator.googleDriveActor
  implicit val ec = context.dispatcher
  implicit val timeout = Timeout(3000)

  def wrappedReceive = {
    case message: SubjectToSubjectMessage if message.fileID.isDefined =>
      val f_info = (driveActor ?? GetFileInfo(userId, message.fileID.get))
      val f_pub = (driveActor ?? PublishFile(userId, message.fileID.get))
      val origin = context.sender
      for {
        info <- f_info.mapTo[GDriveFileInfo]
        pub <- f_pub.mapTo[Permission]
      } {
        message.fileInfo = Some(info)
        processInstanceActor.tell(message, origin)
      }
    case message => {
      processInstanceActor forward message
    }
  }
}

case class SendStateActor(data: StateData)
  extends BehaviorStateActor(data) with ActorLogging {

  import scala.collection.mutable.{Map => MutableMap}
  private var remainingStored = 0
  //private var messageContent: Option[String] = None
  private var messageContent: Option[MessageContent] = None
  private val unsentMessageIDs: MutableMap[MessageID, Transition] = MutableMap[MessageID, Transition]()
  private var executeAction: ExecuteAction = null

  // TODO
  private val sendTransition: Transition =
    transitions.find(_.isExitCond).get
  private val sendExitCond = sendTransition.myType.asInstanceOf[ExitCond]
  private val sendTarget = sendExitCond.target.get
  private var block_gui_message_in_overflow = false;

  //  override def preStart() {
  // TODO so ist das noch nicht, besser machen!
  // ask the ContextResolver for the targetIDs
  // store them in a val
  implicit val timeout = Timeout(2000)

  var targetUserIDs: Option[Array[UserID]] = None
  var crr = "0"

  override def preStart() {
    if (!sendTarget.toExternal) {
      blockingHandlerActor ! BlockUser(userID)
      val msg = RequestUserID(
        SubjectInformation(processID, processInstanceID, sendTarget.subjectID),
        TargetUsers(_))
      ActorLocator.contextResolverActor !! msg
    } else {
      targetUserIDs = Some(Array())
    }

    super.preStart()
  }

  private case class TargetUsers(users: Array[UserID])

  protected def stateReceive = {

    case TargetUsers(userIDs) if (!targetUserIDs.isDefined) =>
      targetUserIDs = Some(userIDs)
      // send information about changed actions to actionchangeactor
      actionChanged(Updated)
      blockingHandlerActor ! UnBlockUser(userID)

    /**
     * send reservation to receiver
     */
    case action: ExecuteAction if (action.actionData.messageContent.isDefined) => {
      if (!messageContent.isDefined) {
        // send subjectInternalMessage before sending executionAnswer to make sure that the executionAnswer
        // can be blocked until a potentially new subject is created to ensure all available actions will
        // be returned when asking
        var msgContent = TextContent(action.actionData.messageContent.get)
        messageContent = Some(msgContent)
        //messageContent = action.actionData.messageContent
        executeAction = action

        for (transition <- exitTransitions if transition.target.isDefined) {
          blockingHandlerActor ! BlockUser(userID) // TODO handle several targetusers

          val messageType = transition.messageType
          val toSubject = transition.subjectID
          val messageID = nextMessageID
          val isEnabled = false
          unsentMessageIDs(messageID) = transition
          // TEST CORRELATION
          val future = subjectActor ?? GetCurrentCorrelation()
          crr = Await.result(future, (5 seconds)).asInstanceOf[String]

          log.debug("SendR@" + userID + "/" + subjectID + ": Reservation[" +
            messageID + "] \"" + messageType + " to " + transition.target + "\"")

          // This ArrayBuffer stores the user, which should be blocked
          // one user can blocked several times
          val blockUsers = ArrayBuffer[UserID]()

          val target = transition.target.get
          if (target.toVariable) {
            target.insertVariable(variables(target.variable.get))
            for ((_, userID) <- target.varSubjects) {
              blockUsers += userID
            }
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
              action.userID.toString)), "GoogleSendProxyActor____" + UUID.randomUUID().toString())
          val msg =
            SubjectToSubjectMessage(
              messageID,
              processID,
              userID,
              subjectID,
              target,
              messageType,
              messageContent.get,
              action.actionData.fileId,
              isEnabled,
              crr // TEST CORRELATION
            )
          sendProxy ! msg
          // send the ActionExecuted to the blocking actor, it will send it
          // to the process instance, when this user is ready
          blockingHandlerActor ! ActionExecuted(action)
        }
      } else {
        log.error("Second send-message action request received")
      }
    }

    case Stored(messageID) if (messageContent.isDefined &&
      unsentMessageIDs.contains(messageID) // TODO might remove the message ID from unsentMessageIDs?
      ) => {
      val transition = unsentMessageIDs(messageID)
      val messageType = transition.messageType
      val toSubject = transition.subjectID
      val isEnabled = true
      val target = transition.target.get

      log.debug("SendR@" + userID + "/" + subjectID + ": Enable[" +
        messageID + "] \"" + messageType + " to " + transition.target + "\"")
      log.debug("message with id {} stored. remaining:", messageID)
      // send the message over the process instance
      val sendProxy = context.actorOf(Props(
        new GoogleSendProxyActor(
          processInstanceActor,
          executeAction.userID.toString)), "GoogleSendProxyActor____" + UUID.randomUUID().toString())
      val msg =
        SubjectToSubjectMessage(
          messageID,
          processID,
          userID,
          subjectID,
          target,
          messageType,
          TextContent(""),
          executeAction.actionData.fileId,
          isEnabled,
          crr)
      sendProxy ! msg

    }

    case Enabled(messageID) if (messageContent.isDefined &&
      unsentMessageIDs.contains(messageID) // TODO might remove the message ID from unsentMessageIDs?
      ) => {
      val transition = unsentMessageIDs(messageID)
      // Create the history message
      val message =
        HistoryMessage(messageID, transition.messageType, subjectID, transition.subjectID, messageContent.toString)
      // Change the state and enter the History entry
      remainingStored -= 1

      log.debug("message with id {} enabled. remaining: {}", messageID, remainingStored)

      block_gui_message_in_overflow = false

      if (remainingStored <= 0) {
        changeState(transition.successorID, data, message)
        blockingHandlerActor ! UnBlockUser(userID)
      }
    }


    case Overflow(messageID) => {
      //Overflow message was received, do something. But not necessary, we at least have to whait

      block_gui_message_in_overflow = true

      //
      actionChanged(Updated)
    }

    case Stored(messageID) => {
      log.warning("unknown message with id {}", messageID)
    }

    case Rejected(messageID) if (
      messageContent.isDefined &&
        unsentMessageIDs.contains(messageID)) => {
      log.warning("message with id {} was rejected", messageID)

      //TODO how to handle the rejected message?
      blockingHandlerActor ! UnBlockUser(userID)
    }

    case Reopen => {
      messageContent = None
      remainingStored = 0
      actionChanged(Updated)
    }

    case ReopenFromServiceHost(channelID) => {
      messageContent = None
      remainingStored = 0
      actionChanged(Updated)
    }
  }

  // TODO only send targetUserData when its not trivial
  override protected def getAvailableAction: Array[ActionData] =
    Array(
      ActionData(
        sendTransition.messageType,
        !block_gui_message_in_overflow && !messageContent.isDefined && targetUserIDs.isDefined && targetUserIDs.get.length >= sendTarget.min,
        exitCondLabel,
        relatedSubject = Some(sendTransition.subjectID),
        targetUsersData =
          Some(TargetUser(sendTarget.min, sendTarget.max, sendTarget.toExternal, targetUserIDs.getOrElse(Array())))))

  /**
   * Generates a new message ID
   */
  private def nextMessageID: Int = MessageIDProvider.nextMessageID()
}
