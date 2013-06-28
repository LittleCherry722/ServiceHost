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

import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.behavior.state._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.model._
import akka.event.Logging
import akka.actor.Status.Failure
import de.tkip.sbpm.application.history.{Message => HistoryMessage}
import de.tkip.sbpm.application.history.{State => HistoryState}
import de.tkip.sbpm.application.history.{Transition => HistoryTransition}
import de.tkip.sbpm.application.history.{NewMessage => NewHistoryMessage}
import de.tkip.sbpm.application.history.{NewState => NewHistoryState}
import de.tkip.sbpm.application.history.{NewTransition => NewHistoryTransition}
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.SubjectData

// TODO this is for history + statechange
case class ChangeState(
  currenState: StateID,
  nextState: StateID,
  internalStatus: InternalStatus,
  history: HistoryMessage)

/**
 * contains the business logic that will be modeled by the graph
 */
class InternalBehaviorActor(
  data: SubjectData,
  inputPoolActor: ActorRef) extends Actor {
  // extract the data

  val processInstanceActor = data.processInstanceActor
  val subjectID = data.subject.id
  val userID = data.userID

  private val statesMap = collection.mutable.Map[StateID, State]()
  private var startState: StateID = 0
  private var currentState: BehaviorStateRef = null
  private var internalStatus: InternalStatus = InternalStatus()

  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    logger.debug(getClass.getName + " stopped.")
  }

  def receive = {
    case state: State => {
      addState(state)
    }

    case message: StartSubjectExecution => {
      nextState(startState)
    }

    case change: ChangeState => {
      // update the internal status
      internalStatus = change.internalStatus
      // TODO check if current state is correct?
      // change the state
      nextState(change.nextState)

      val current: State = statesMap(change.currenState)
      val next: State = statesMap(change.nextState)
      // create the History Entry and send it to the subject
      context.parent !
        HistoryTransition(
          HistoryState(current.text, current.stateType.toString()),
          HistoryState(next.text, next.stateType.toString()),
          change.history)
      context.parent !
        NewHistoryTransition(
          NewHistoryState(current.text, current.stateType.toString()),
          current.transitions.filter(_.successorID == next.id)(0).messageType.toString(),
          current.transitions.filter(_.successorID == next.id)(0).myType.getClass().getSimpleName(),
          NewHistoryState(next.text, next.stateType.toString())
        )
    }

    case ea: ExecuteAction => {
      currentState.forward(ea)
    }

    case terminated: SubjectTerminated => {
      context.parent ! terminated
    }

    case br: SubjectBehaviorRequest => {
      if (currentState != null) {
        currentState.forward(br)
      } else if (br.isInstanceOf[AnswerAbleMessage]) {
        br.asInstanceOf[AnswerAbleMessage].sender !
          Failure(new Exception(
            "Subject : " + subjectID + "of process instance " +
              data.processInstanceID + " has no running subject"))
      }
    }

    case historyTransition: de.tkip.sbpm.application.history.Transition => {
      context.parent ! historyTransition
    }

    // general matching
    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    case n => {
      logger.error("InternalBehavior - Not yet supported: " + n + " " + subjectID)
    }
  }

  /**
   * Adds a state to the internal model
   */
  private def addState(state: State) {
    if (state.startState) {
      logger.debug("startstate " + state)
      startState = state.id
    }
    statesMap += state.id -> state
  }

  /**
   * Executes the nextstate and terminates the currentstate
   */
  private def nextState(state: StateID) {
    if (currentState != null) {
      context.stop(currentState)
      currentState = null
    }

    if (statesMap.contains(state)) {
      logger.debug("Execute: /" + userID + "/" + subjectID + "/" + state)
      currentState = parseState(statesMap(state))
    } else {
      logger.error("ERROR: /" + userID + "/" + subjectID + "/" + state + "does not exist")
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
      internalStatus)

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
    }
  }
}
