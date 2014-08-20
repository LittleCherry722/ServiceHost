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
import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.FSM
import akka.actor.Props
import akka.actor.Status.Failure
import akka.actor.actorRef2Scala
import akka.event.Logging
import de.tkip.sbpm.application.history.{ Message => HistoryMessage }
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.timeoutLabel
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.InternalBehaviorRef
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.ProcessInstanceRef
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.StateID
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.SubjectID
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.UserID
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import de.tkip.sbpm.application.subject.SubjectData
import de.tkip.sbpm.application.subject.behavior.ChangeState
import de.tkip.sbpm.application.subject.behavior.InternalStatus
import de.tkip.sbpm.application.subject.behavior.TimeoutCond
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.subject.misc.AvailableAction
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.GetAvailableAction
import de.tkip.sbpm.model.State
import de.tkip.sbpm.model.StateType.SendStateType
import scala.collection.mutable.Stack
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import akka.actor.PoisonPill
import de.tkip.sbpm.application.subject.misc.ActionIDProvider
import scala.collection.mutable.ArrayBuffer
import de.tkip.sbpm.application.subject.misc.AvailableAction
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.model.ChangeDataMode._
import de.tkip.sbpm.model.ActionDelete
import java.util.Date
import de.tkip.sbpm.model.ActionChange
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import de.tkip.sbpm.application.subject.misc.ActionExecuted

/**
 * The data, which is necessary to create any state
 */
case class StateData(
  subjectData: SubjectData,
  stateModel: State,
  userID: UserID,
  subjectID: SubjectID,
  macroID: String,
  internalBehaviorActor: InternalBehaviorRef,
  subjectActor: SubjectRef,
  processInstanceActor: ProcessInstanceRef,
  inputPoolActor: ActorRef,
  internalStatus: InternalStatus,
  visitedModalSplit: Stack[(Int, Int)] = new Stack) // (id, number of branches)

// the correct way to kill a state instead of PoisonPill
private[subject] case object KillState

// the message to signal, that a timeout has expired
private case object TimeoutExpired

// disables the state, so it cannot execute actions
case object DisableState

/**
 * models the behavior through linking certain ConcreteBehaviorStates and executing them
 */
protected abstract class BehaviorStateActor(data: StateData) extends InstrumentedActor with DefaultLogging {
  protected val blockingHandlerActor = data.subjectData.blockingHandlerActor
  protected val model = data.stateModel
  protected val stateOptions = model.options
  protected val id = model.id
  protected val userID = data.userID
  protected val processID = data.subjectData.processID
  protected val processInstanceID = data.subjectData.processInstanceID
  protected val subjectID = data.subjectID
  protected val macroID = data.macroID
  protected val stateText = model.text
  protected val startState = model.startState
  protected val stateType = model.stateType
  protected val transitions = model.transitions
  protected val internalBehaviorActor = data.internalBehaviorActor
  protected val subjectActor = data.subjectActor
  protected val processInstanceActor = data.processInstanceActor
  protected val inputPoolActor = data.inputPoolActor
  protected val internalStatus = data.internalStatus
  protected val variables = internalStatus.variables
  protected val timeoutTransition = transitions.find(_.isTimeout)
  protected val exitTransitions = transitions.filter(_.isExitCond)

  private var disabled = false

  override def preStart() {

    // if it is needed, send a SubjectStarted message
    if (!delayUnblockAtStart) {
      internalStatus.subjectStartedSent = true
      // TODO so richtig?
      blockingHandlerActor ! UnBlockUser(userID)
    }

    // if the state has a(n automatic) timeout transition, start the timeout timer
    if (timeoutTransition.isDefined) {
      val stateTimeout = timeoutTransition.get.myType.asInstanceOf[TimeoutCond]
      if (!stateTimeout.manual) {
        context.system.scheduler.scheduleOnce(FiniteDuration(stateTimeout.duration, "s"), self, TimeoutExpired)
      }
    }
    actionChanged(Inserted)
  }

  // first try the "receive" function of the inheritance state
  // then use the "receive" function of this behavior state
  final def wrappedReceive = generalReceive orElse stateReceive orElse errorReceive

  // the inheritance state must implement this function
  protected def stateReceive: Receive

  // the receive of this behavior state, it will be executed
  // if the state-receive does not match
  private def generalReceive: Receive = {

    case DisableState => {
      if (!disabled) {
        disabled = true
        actionChanged()
      }
    }

    case action: ExecuteAction if disabled => {
      log.error(s"Cannot execute $action, this state is disabled")
      val message = Failure(new IllegalArgumentException(
        "Invalid Argument: The state of the action is disabled."))
      val receiver = action.asInstanceOf[AnswerAbleMessage].sender
      receiver ! message
    }

    // filter all invalid action
    case action: ExecuteAction if {
      action.userID != userID ||
        action.processInstanceID != processInstanceID ||
        action.subjectID != subjectID ||
        action.stateType != stateType.toString
    } => {
      val message = Failure(new IllegalArgumentException(
        "Invalid Argument: The action does not match to the current state."))
      val receiver = action.asInstanceOf[AnswerAbleMessage].sender
      receiver ! message
    }

    case ga: GetAvailableAction => sender !! createAvailableAction

    case TimeoutExpired => executeTimeout()

    case action: ExecuteAction if {
      action.actionData.transitionType == timeoutLabel
    } => {
      executeTimeout()
      processInstanceActor ! ActionExecuted(action)
    }
  }

  import de.tkip.sbpm.model.StateType._
  private def errorReceive: Receive = {

    case KillState => suicide()

    case message: AnswerAbleMessage => {
      message match {
        case action: ExecuteAction => {
          stateType match {
            case SendStateType if (!action.actionData.messageContent.isDefined) => {
              val failure = Failure(new IllegalArgumentException(
                "Invalid Argument: messageContent not defined, a sendstate needs a MessageContent"))
              message.sender !! failure

            }
          }

        }
        case _ => {
          val failure = Failure(new Exception("Internal Server Error in " + stateType.toString()))
          message.sender !! failure
        }
      }
      log.error("BehaviorStateActor does not support: " + message)
    }

    case s => log.error("BehaviorStateActor does not support: " + s)
  }

  protected def suicide() {
    self ! PoisonPill
  }

  /**
   * Executes a timeout by executing the timeout edge
   *
   * override this function to execute an other transition when a timeout appears
   */
  protected def executeTimeout() {
    if (timeoutTransition.isDefined) {
      log.debug("Executing Timeout")
      changeState(timeoutTransition.get.successorID, data, null)
    }
  }

  /**
   * This function returns if the subjectready message should be delayed,
   * default value is false
   *
   * override this function to delay the subject ready message
   *
   * @return whether the subject ready message should be delayed
   */
  protected def delayUnblockAtStart = false

  /**
   * Changes the state and creates a history entry with the history message
   */
  protected def changeState(successorID: StateID, prevStateData: StateData, historyMessage: HistoryMessage) {
    val delete =  ActionDelete(actionID, new Date())
    ActorLocator.changeActor ! delete
    blockingHandlerActor     ! BlockUser(userID)
    internalBehaviorActor    ! ChangeState(id, successorID, internalStatus, prevStateData, historyMessage)
  }

  private lazy val actionID = ActionIDProvider.nextActionID()

  /**
   * Call this method, when the action has changed
   *
   * it informs the ChangeActor about the new action
   */
  protected def actionChanged(changeMode: ChangeMode = Updated) {
    val message = ActionChange(createAvailableAction, changeMode, new Date())
    ActorLocator.changeActor ! message
  }

  /**
   * Returns the available actions of the state
   */
  protected def getAvailableAction: Array[ActionData]

  /**
   * Creates the Available Action, which belongs to this state
   */
  protected def createAvailableAction = {
    var actionData = getAvailableAction
    if (timeoutTransition.isDefined) {
      actionData ++= Array(ActionData("timeout", true, timeoutLabel))
    }
    if (disabled) {
      // if disabled, disable all action
      actionData.foreach(_.executeAble = false)
    }
    AvailableAction(
      actionID,
      userID,
      processInstanceID,
      subjectID,
      macroID,
      id,
      stateText,
      stateType.toString(),
      actionData)
  }
}
