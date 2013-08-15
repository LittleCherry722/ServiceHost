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

package de.tkip.sbpm.application.subject.behavior

import scala.collection.mutable
import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.behavior.state._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.model._
import akka.event.Logging
import akka.actor.Status.Failure
import de.tkip.sbpm.application.history.{ Message => HistoryMessage }
import de.tkip.sbpm.application.history.{ State => HistoryState }
import de.tkip.sbpm.application.history.{ Transition => HistoryTransition }
import de.tkip.sbpm.application.history.NewHistoryMessage
import de.tkip.sbpm.application.history.NewHistoryState
import de.tkip.sbpm.application.history.NewHistoryTransitionData
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.SubjectData
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.application.subject.misc.TryTransportMessages
import scala.concurrent.Promise
import akka.util.Timeout
import akka.pattern.ask
import akka.pattern.pipe
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.collection.mutable.Stack

// TODO this is for history + statechange
case class ChangeState(
  currenState: StateID,
  nextState: StateID,
  internalStatus: InternalStatus,
  prevStateData: StateData,
  history: HistoryMessage)

/**
 * contains the business logic that will be modeled by the graph
 */
class InternalBehaviorActor(
  data: SubjectData,
  inputPoolActor: ActorRef) extends Actor with DefaultLogging {
  // extract the data
  implicit val timeout = Timeout(2000)

  val processInstanceActor = data.processInstanceActor
  val subjectID = data.subject.id
  val userID = data.userID

  private val statesMap = collection.mutable.Map[StateID, State]()
  private var startState: StateID = 0
  //  private var currentState: BehaviorStateRef = null
  private var internalStatus: InternalStatus = InternalStatus()
  private var modalSplitList: Stack[(Int, Int)] = new Stack // tmp

  def receive = {
    case state: State => {
      addStateToModel(state)
    }

    case message: StartSubjectExecution => {
      addState(startState)
    }

    case change: ChangeState => {
      // update the internal status
      internalStatus = change.internalStatus
      modalSplitList = change.prevStateData.visitedModalSplit
      // TODO check if current state is correct?
      // change the state
      changeState(change.currenState, change.nextState)

      val current: State = statesMap(change.currenState)
      val next: State = statesMap(change.nextState)
      // create the History Entry and send it to the subject
      context.parent !
        HistoryTransition(
          HistoryState(current.text, current.stateType.toString()),
          HistoryState(next.text, next.stateType.toString()),
          change.history)
      context.parent !
        NewHistoryTransitionData(
          NewHistoryState(current.text, current.stateType.toString()),
          current.transitions.filter(_.successorID == next.id)(0).messageType.toString(),
          current.transitions.filter(_.successorID == next.id)(0).myType.getClass().getSimpleName(),
          NewHistoryState(next.text, next.stateType.toString()),
          if (change.history != null) Some(NewHistoryMessage(
            change.history.id,
            change.history.from,
            change.history.to,
            change.history.messageType,
            change.history.data))
          else None)
    }

    case ea: ExecuteAction => {
      currentStatesMap(ea.stateID).forward(ea)
    }

    case terminated: SubjectTerminated => {
      context.parent ! terminated
    }

    case br: GetAvailableAction => {
      // Create a Future with the available actions
      val actionFutures =
        Future.sequence(
          for ((_, c) <- currentStatesMap) yield (c ? br).mapTo[AvailableAction])

      // and pipe the actions back to the sender
      actionFutures pipeTo sender
    }

    case historyTransition: de.tkip.sbpm.application.history.Transition => {
      context.parent ! historyTransition
    }

    // general matching
    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    case n => {
      log.error("InternalBehavior - Not yet supported: " + n + " " + subjectID)
    }
  }

  /**
   * Adds a state to the internal model
   */
  private def addStateToModel(state: State) {
    if (state.startState) {
      log.debug("startstate " + state)
      startState = state.id
    }
    statesMap += state.id -> state

    inputPoolActor ! TryTransportMessages
  }

  private val currentStatesMap: mutable.Map[StateID, BehaviorStateRef] = mutable.Map()
  private def changeState(from: StateID, to: StateID) = {
    // kill the currentState
    if (currentStatesMap contains from) {
      val currentState = currentStatesMap(from)
      // kill the from State
      currentState ! PoisonPill
      currentStatesMap -= from
    } else {
      log.debug("Change State from a State, which does not exits")
    }

    // TODO damit umgehen, wenn target ein ModalJoin State ist
    log.debug("Execute: /%s/%s/[%s]->[%s]".format(userID, subjectID, from, to))
    addState(to)
  }
  private def addState(state: StateID) {
    if (statesMap.contains(state)) {
      log.debug("Execute: /%s/%s/%s".format(userID, subjectID, state))

      if (currentStatesMap contains state) {
        log.debug("State /%s/%s/%s is already running".format(userID, subjectID, state))
        // TODO hier message wegen modaljoin!
        if (statesMap(state).stateType == ModalJoinStateType) {
          currentStatesMap(state) ! TransitionJoined
          data.blockingHandlerActor ! UnBlockUser(userID)
        }
      } else {
        currentStatesMap += state -> parseState(statesMap(state))
      }
    } else {
      log.error("ERROR: /" + userID + "/" + subjectID + "/" + state + "does not exist")
    }
  }

  /**
   * Parses a state and create the corresponding state actor
   */
  private def parseState(state: State) = {
    // create the data every state needs
    val stateData = StateData(
      data,
      state,
      userID,
      subjectID,
      self,
      processInstanceActor,
      inputPoolActor,
      internalStatus,
      modalSplitList)

    // create the actor which matches to the statetype
    state.stateType match {
      case ActStateType => {
        context.actorOf(Props(ActStateActor(stateData)))
      }

      case SendStateType => {
        context.actorOf(Props(SendStateActor(stateData)))
      }

      case ReceiveStateType => {
        context.actorOf(Props(ReceiveStateActor(stateData)))
      }

      case EndStateType => {
        context.actorOf(Props(EndStateActor(stateData)))
      }

      case CloseIPStateType => {
        context.actorOf(Props(CloseIPStateActor(stateData)))
      }

      case OpenIPStateType => {
        context.actorOf(Props(OpenIPStateActor(stateData)))
      }

      case IsIPEmptyStateType => {
        context.actorOf(Props(IsIPEmptyStateActor(stateData)))
      }

      case ModalSplitStateType => {
        context.actorOf(Props(ModalSplitStateActor(stateData)))
      }

      case ModalJoinStateType => {
        context.actorOf(Props(ModalJoinStateActor(stateData)))
      }
    }
  }
}
