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
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.Array.canBuildFrom

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
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.application.RequestUserID
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessageReceived

import de.tkip.sbpm.rest.google.GDriveControl.GDriveFileInfo

protected case class ReceiveStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  // convert the transitions into a map of extended transitions, to work with
  // this map in the whole actor
  private val exitTransitionsMap: Map[(SubjectID, MessageType), ExtendedExitTransition] =
    exitTransitions.map((t: Transition) =>
      ((t.subjectID, t.messageType), new ExtendedExitTransition(t)))
      .toMap[(SubjectID, MessageType), ExtendedExitTransition]

  log.debug("exitTransitionsMap: "+exitTransitionsMap.mkString(","))

  // register to subscribe the messages at the inputpool
  inputPoolActor ! {
    // convert the transition array into the request array
    for (transition <- exitTransitions if (transition.target.isDefined)) yield {
      // maximum number of messages the state is able to process
      val count = transition.target.get.max
      // the register-message for the inputpool
      SubscribeIncomingMessages(id, transition.subjectID, transition.messageType, count)
    }
  }

  protected def stateReceive = {
    // execute an action
    case action: ExecuteAction if ({
      // check if the related subject exists
      val input = action.actionData
      input.relatedSubject.isDefined && {
        val from = input.relatedSubject.get
        val messageType = input.text
        // check if the related transition exists
        exitTransitionsMap.contains((from, messageType)) &&
          // only execute transitions, which are ready to execute
          exitTransitionsMap((from, messageType)).ready
      }
    }) => {
      val input = action.actionData
      // get the transition from the map
      val transition = exitTransitionsMap((input.relatedSubject.get, input.text))
      // create the Historymessage
      val message =
        HistoryMessage(transition.messageID, transition.messageType, transition.from, subjectID, transition.messageContent.get)
      // change the state and enter the history entry
      changeState(transition.successorID, data, message)

      // inform the processinstance, that this action is executed
      blockingHandlerActor ! ActionExecuted(action)
    }

    case sm: SubjectToSubjectMessage if (exitTransitionsMap.contains((sm.from, sm.messageType))) => {
      logger.debug("Receive@" + userID + "/" + subjectID + ": Message \"" +
        sm.messageType + "\" from \"" + sm.from +
        "\" with content \"" + sm.messageContent + "\"")

      exitTransitionsMap(sm.from, sm.messageType).addMessage(sm)

      val t = exitTransitionsMap(sm.from, sm.messageType).transition
      val varID = t.storeVar
      if (t.storeToVar && varID.isDefined) {
        variables.getOrElseUpdate(varID.get, Variable(varID.get)).addMessage(sm)
        System.err.println(variables.mkString("VARIABLES: {\n", "\n", "}")) //TODO
      }

      sender ! SubjectToSubjectMessageReceived(sm)
    }

    case InputPoolSubscriptionPerformed => {
      // This state has all inputpool information -> unblock the user
      blockingHandlerActor ! UnBlockUser(userID)
    }
  }

  override protected def delayUnblockAtStart = true

  // only for startstate creation, check if subjectready should be sent
  var sendSubjectReady = startState
  private def trySendSubjectStarted() {
    if (sendSubjectReady) {
      // TODO so richtig?F
      blockingHandlerActor ! UnBlockUser(userID)
      sendSubjectReady = false
    }
  }

  override protected def executeTimeout() {
    val exitTransition =
      exitTransitionsMap.map(_._2).filter(_.ready).map(_.transition)
        .reduceOption((t1, t2) => if (t1.priority < t2.priority) t1 else t2)

    if (exitTransition.isDefined) {
      // TODO richtige historymessage
      changeState(exitTransition.get.successorID, data, null)
    } else {
      super.executeTimeout()
    }
  }

  override protected def getAvailableAction: Array[ActionData] =
    (for ((k, t) <- exitTransitionsMap) yield {
      ActionData(
        t.messageType,
        t.ready,
        exitCondLabel,
        relatedSubject = Some(t.from),
        messageContent = t.messageContent, // TODO delete
        messages = Some(t.messages))
    }).toArray

  override protected def changeState(successorID: StateID, prevStateData: StateData, historyMessage: HistoryMessage) {
    // inform the inputpool, that this state is not waiting for messages anymore
    inputPoolActor ! UnSubscribeIncomingMessages(id)

    // change the state
    super.changeState(successorID, data, historyMessage)
  }

  /**
   * This case class extends an transition with information about the related message
   */
  private class ExtendedExitTransition(val transition: Transition) {
    val from: SubjectID = transition.subjectID
    val messageType: MessageType = transition.messageType
    val successorID: StateID = transition.successorID

    var ready = false
    var messageID: MessageID = -1
    var messageContent: Option[MessageContent] = None

    val messageData: ArrayBuffer[MessageData] = ArrayBuffer[MessageData]()

    def messages = messageData.toArray

    private var remaining = transition.target.get.min

    def addMessage(message: SubjectToSubjectMessage) {
      // validate
      if (!(message.messageType == messageType && message.from == from)) {
        logger.error("Transportmessage is invalid to transition: " + message +
          ", " + this)
        return
      }

      remaining -= 1
      ready = remaining <= 0

      // TODO auf mehrere messages umbauen, anstatt immer nur die letzte
      messageID = message.messageID
      messageContent = Some(message.messageContent)
      val (title,url,iconLink) = message.fileInfo match {
        case Some(GDriveFileInfo(title,url,iconLink)) => (Some(title),Some(url),Some(iconLink))
        case None => (None,None,None)
      }
      messageData += MessageData(message.userID, message.messageContent, title, url, iconLink)
    }
  }
}