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

import scala.Array.canBuildFrom
import scala.collection.mutable.ArrayBuffer
import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.history.{ Message => HistoryMessage }
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.exitCondLabel
//import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.MessageContent
import de.tkip.sbpm.application.ProcessInstanceActor.MessageContent
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.MessageID
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.MessageType
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.StateID
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.SubjectID
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import de.tkip.sbpm.application.subject.behavior.InputPoolSubscriptionPerformed
import de.tkip.sbpm.application.subject.behavior.SubscribeIncomingMessages
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.behavior.UnSubscribeIncomingMessages
import de.tkip.sbpm.application.subject.behavior.Variable
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.MessageData
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.rest.google.GDriveControl.GDriveFileInfo
import de.tkip.sbpm.application.subject.misc.DisableNonObserverStates
import de.tkip.sbpm.application.subject.misc.KillNonObserverStates
import de.tkip.sbpm.application.subject.behavior.InputPoolMessagesChanged
import de.tkip.sbpm.application.subject.behavior.DeleteInputPoolMessages
import de.tkip.sbpm.model.ChangeDataMode._

case class ReceiveStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  // convert the transitions into a map of extended transitions, to work with
  // this map in the whole actor
  private val exitTransitionsMap: Map[(SubjectID, MessageType), ExtendedExitTransition] =
    exitTransitions.map((t: Transition) =>
      ((t.subjectID, t.messageType), new ExtendedExitTransition(t)))
      .toMap[(SubjectID, MessageType), ExtendedExitTransition]

  log.debug("exitTransitionsMap: " + exitTransitionsMap.mkString(","))

  // register to subscribe the messages at the inputpool

  val msg = {
    // convert the transition array into the request array
    for (transition <- exitTransitions if (transition.target.isDefined)) yield {
      // the register-message for the inputpool
      SubscribeIncomingMessages(id, transition.subjectID, transition.messageType)
    }
  }
  inputPoolActor ! msg

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
        HistoryMessage(transition.messageID, transition.messageType, transition.from, subjectID, messageMatch(transition.messageContent))

      // TODO check if possible
      val msg = DeleteInputPoolMessages(transition.from, transition.messageType, transition.receiveMessages)
      inputPoolActor ! msg

      // change the state and enter the history entry
      changeState(transition.successorID, data, message)

      // inform the processinstance, that this action is executed
      blockingHandlerActor ! ActionExecuted(action)
    }

    case InputPoolMessagesChanged(fromSubject, messageType, messages) if (exitTransitionsMap.contains((fromSubject, messageType))) => {

      log.debug("Receive@" + userID + "/" + subjectID + ": " +
        messages.size + ". Messages \"" +
        messageType + "\" from \"" + fromSubject +
        "\" with content \"" + messages.map(_.messageContent).mkString("[", ", ", "]") + "\"")

      exitTransitionsMap(fromSubject, messageType).setMessages(messages)

      val t = exitTransitionsMap(fromSubject, messageType).transition
      val varID = t.storeVar
      if (t.storeToVar && varID.isDefined) {
        // FIXME variablen in dem context
        //        variables.getOrElseUpdate(varID.get, Variable(varID.get)).addMessage(sm)
        //        System.err.println(variables.mkString("VARIABLES: {\n", "\n", "}")) //TODO
      }

      //      val ack = SubjectToSubjectMessageReceived(sm)
      //
      //      log.debug("sending {} to {}", ack, sender)
      //      sender !! ack
      //      sender !! SubjectToSubjectMessageReceived(sm)

      // send information about changed actions to actionchangeactor
      actionChanged(Updated)

      var transition = exitTransitionsMap(fromSubject, messageType)
      var isAutoReceive = false
      if (messages.length != 0 && data.stateModel.autoExecute) {
        //Check if only one ExitCond
        if (exitTransitions.length == 1) {
          isAutoReceive = true
        } else {
          var p, t = exitTransitions(0).priority
          var tr1, tr2 = exitTransitions(0)
          var count = 0
          for (et <- exitTransitions) {
            if (et.priority > p) {
              p = et.priority
              tr1 = et
            }
            if (messageType.equals(et.messageType)) {
              tr2 = et
              count += 1
            }
          }
          //Check if there is a highest priority
          if (p > t) {
            transition = exitTransitionsMap(tr1.subjectID, tr1.messageType)
            isAutoReceive = true
            //Check if there is a matched message type
          } else if (count == 1) {
            transition = exitTransitionsMap(tr2.subjectID, tr2.messageType)
            isAutoReceive = true
          }
        }
        if (isAutoReceive) {
          val message =
            HistoryMessage(transition.messageID, transition.messageType, transition.from, subjectID, messageMatch(transition.messageContent))

          // TODO check if possible
          val msg = DeleteInputPoolMessages(transition.from, transition.messageType, transition.receiveMessages)
          inputPoolActor ! msg

          // change the state and enter the history entry
          changeState(transition.successorID, data, message)
        }
      }

      // try to disable other states, when this state is an observer
      tryDisableNonObserverStates()
    }

    //    case sm: SubjectToSubjectMessage if (exitTransitionsMap.contains((sm.from, sm.messageType))) => {
    //      log.debug("Receive@" + userID + "/" + subjectID + ": Message \"" +
    //        sm.messageType + "\" from \"" + sm.from +
    //        "\" with content \"" + sm.messageContent + "\"")
    //
    //      exitTransitionsMap(sm.from, sm.messageType).addMessage(sm)
    //
    //      val t = exitTransitionsMap(sm.from, sm.messageType).transition
    //      val varID = t.storeVar
    //      if (t.storeToVar && varID.isDefined) {
    //        variables.getOrElseUpdate(varID.get, Variable(varID.get)).addMessage(sm)
    //        System.err.println(variables.mkString("VARIABLES: {\n", "\n", "}")) //TODO
    //      }
    //
    //      //      sender !! SubjectToSubjectMessageReceived(sm)
    //
    //      // try to disable other states, when this state is an observer
    //      tryDisableNonObserverStates()
    //    }

    case InputPoolSubscriptionPerformed => {
      // This state has all inputpool information -> unblock the user
      blockingHandlerActor ! UnBlockUser(userID)
    }

    case KillState => {
      // inform the inputpool, that this state is not waiting for messages anymore
      inputPoolActor ! UnSubscribeIncomingMessages(id)
      suicide()
    }
  }

  override protected def delayUnblockAtStart = true

  private def tryDisableNonObserverStates() {
    // TODO check if timeout is ready
    if (model.observerState && exitTransitionsMap.exists(_._2.ready)) {
      subjectActor ! DisableNonObserverStates
    }
  }

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

    // if this is an observer state disable the other states,
    // because this state fires a transition
    tryDisableNonObserverStates()

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
        messageContent = Some(messageMatch(t.messageContent)), // TODO delete
        messages = Some(t.messages))
    }).toArray

  override protected def changeState(successorID: StateID, prevStateData: StateData, historyMessage: HistoryMessage) {
    // inform the inputpool, that this state is not waiting for messages anymore
    inputPoolActor ! UnSubscribeIncomingMessages(id)

    if (data.stateModel.observerState) {
      subjectActor ! KillNonObserverStates
    }

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
    private var remaining = transition.target.get.min

    val messageData: ArrayBuffer[MessageData] = ArrayBuffer[MessageData]()

    def messages = messageData.toArray

    def receiveMessages: Array[MessageID] = {
      // TODO Transition max valie
      if (ready) messages.take(Math.min(1, messages.size)).map(_.messageID)

      else Array()
    }

    def setMessages(messages: Array[SubjectToSubjectMessage]) {
      clearMessages()
      for (message <- messages) addMessage(message)
    }

    private def clearMessages() {
      // reset all fields
      ready = false
      messageID = -1
      messageContent = None
      remaining = transition.target.get.min
      messageData.clear()
    }

    private def addMessage(message: SubjectToSubjectMessage) {
      // validate
      if (!(message.messageType == messageType && message.from == from)) {
        log.error("Transportmessage is invalid to transition: " + message +
          ", " + this)
        return
      }

      remaining -= 1
      ready = remaining <= 0

      // TODO auf mehrere messages umbauen, anstatt immer nur die letzte
      messageID = message.messageID
      messageContent = Some(message.messageContent)
      val (title, url, iconLink) = message.fileInfo match {
        case Some(GDriveFileInfo(title, url, iconLink)) => (Some(title), Some(url), Some(iconLink))
        case None                                       => (None, None, None)
      }
      messageData += MessageData(message.messageID, message.userID, messageMatch(message.messageContent), title, url, iconLink)
    }
  }

  def messageMatch (msg: Any) = msg match {
    case text: String => text
    case _ => msg.toString // other type will be processed in future.
  }
}
