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

import java.util.UUID
import scala.collection.mutable
import de.tkip.sbpm.instrumentation.InstrumentedActor
import scala.collection.mutable.ArrayBuffer
import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.behavior.state._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.model._
import akka.event.Logging
import akka.actor.Status.Failure
import de.tkip.sbpm.application.history.NewHistoryMessage
import de.tkip.sbpm.application.history.NewHistoryState
import de.tkip.sbpm.application.history.NewHistoryTransitionData
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.SubjectData
import de.tkip.sbpm.application.ProcessInstanceActor.RegisterSubjects
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.application.history.{
Message => HistoryMessage
}
import scala.concurrent.Promise
import akka.util.Timeout
import akka.pattern.ask
import akka.pattern.pipe
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import de.tkip.sbpm.application.subject.{CallMacro, CallMacroStates}
import scala.collection.mutable.Stack
import de.tkip.sbpm.application.subject.behavior.state.ArchiveStateActor
import org.parboiled.support.Var

case object StartMacroExecution

case class ActivateState(id: StateID)

case class DeactivateState(id: StateID)

case class AskForJoinStateID(id: StateID)

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
                             macroId: String,
                             macroStartState: Option[ActorRef],
                             data: SubjectData,
                             inputPoolActor: ActorRef) extends InstrumentedActor {
  // extract the data
  implicit val timeout = Timeout(2000)

  val processInstanceActor = data.processInstanceActor
  val subjectID = data.subject.id
  val userID = data.userID

  private val subjectActor = context.parent

  private val statesMap = collection.mutable.Map[StateID, State]()
  private var startState: StateID = _
  //  private var currentState: BehaviorStateRef = null
  private var internalStatus: InternalStatus = InternalStatus()
  private var modalSplitList: Stack[(Int, Int)] = new Stack // tmp


  def wrappedReceive = {
    case state: State => {
      addStateToModel(state)
    }

    case StartMacroExecution => {
      addState(startState)
    }

    case DisableNonObserverStates => {
      for (
        (id, state) <- currentStatesMap;
        if (!statesMap(id).observerState)
      ) {
        state ! DisableState // """""""""""""
      }

    }
    case KillNonObserverStates => {
      // TODO kill other macros!
      for (
        state <- currentStatesMap.map(_._1);
        if (!statesMap(state).observerState)
      ) {
        killState(state)
      }
    }

    case ActivateState(id) => {
      addState(id)
    }

    case DeactivateState(id) => {
      // TODO this will cause an error, because receive states still will
      // subscribe the inputpool messages
      killState(id)
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
      if (next.stateType == StateType.ArchiveStateType) {
        val msg = new AutoArchive(current.transitions.filter(_.successorID == next.id)(0))
        currentStatesMap(change.nextState) ! msg
      }
      // create the History Entry and send it to the subject
      val msg = current.stateType.toString() match {
        case "$splitguard" =>
          NewHistoryTransitionData(
            NewHistoryState(current.text, current.stateType.toString()),
            "nein", "nein",
            NewHistoryState(next.text, next.stateType.toString()),
            if (change.history != null) Some(NewHistoryMessage(
              change.history.id,
              change.history.from,
              change.history.to,
              change.history.messageType,
              change.history.data))
            else None)
        case _ =>
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
      context.parent ! msg
    }

    case ea: ExecuteAction => {
      currentStatesMap(ea.stateID).forward(ea)
    }

    case terminated: MacroTerminated => {
      if (macroStartState.isDefined) {
        data.blockingHandlerActor ! BlockUser(userID)
        macroStartState.get ! terminated
      }
      context.parent ! terminated
    }

    case m: CallMacro => {
      context.parent ! m
    }

    case m: CallMacroStates => {
      context.parent ! m
    }

    case m: RegisterSubjects => {
      context.parent forward m
    }

    case getActions: GetAvailableAction => {
      // Create a Future with the available actions
      val actionFutures =
        Future.sequence(
          for ((_, c) <- currentStatesMap if (!c.isTerminated)) yield (c ?? getActions).mapTo[AvailableAction])
      // and pipe the actions back to the sender
      actionFutures pipeTo sender
    }

    // general matching
    case message: SubjectProviderMessage => {
      context.parent ! message
    }
    case av: AddVariable => {

      if (!internalStatus.variables.contains(av.variableName)) {
        internalStatus.variables.put(av.variableName, new Variable(av.variableName))
      }
      internalStatus.variables(av.variableName).addMessage(av.message)

    }

    case joinstate: AskForJoinStateID => {
      val stateBuffer = ArrayBuffer[Int]()
      val visited = ArrayBuffer[Int]()
      var notFind = true
      var current = joinstate.id
      stateBuffer += current
      while (!stateBuffer.isEmpty && notFind) {
        for (t <- statesMap(current).transitions; if !visited.contains(t.successorID)) {
          visited += t.successorID
          stateBuffer += t.successorID
          if (statesMap(t.successorID).stateType.toString().equals("modaljoin")) {
            sender ! statesMap(t.successorID).id
            notFind = true
          }
        }
        stateBuffer -= current
        if (!stateBuffer.isEmpty) current = stateBuffer.head
      }
      sender ! -1
    }

    case msg: SubjectToSubjectMessage => {
      context.parent.forward(msg)
    }


    case n => {
      log.error("InternalBehavior - Not yet supported: " + n + " " + subjectID)
    }
  }

  /**
   * Adds a state to the internal model
   */
  private def addStateToModel(state: State) {
    log.debug("addStateToModel: " + state)
    if (state.startState) {
      log.debug("Set startstate: " + state)
      startState = state.id
    }
    statesMap += state.id -> state
  }

  private val currentStatesMap: mutable.Map[StateID, BehaviorStateRef] = mutable.Map()

  private def changeState(from: StateID, to: StateID) = {
    // kill the currentState
    log.debug("kill the current state")
    killState(from)

    // TODO damit umgehen, wenn target ein ModalJoin State ist
    log.debug("Execute: /%s/%s/[%s]->[%s]".format(userID, subjectID, from, to))
    addState(to)
  }

  private def killState(state: StateID) {
    if (currentStatesMap contains state) {
      val currentState = currentStatesMap(state)
      // kill the state
      currentState ! KillState
      currentStatesMap -= state
    } else {
      log.debug("Kill State for a State, which does not exits")
    }
  }

  private def addState(state: StateID) {
    if (statesMap.contains(state)) {
      log.debug("Starting state: /%s/%s/%s/%s".format(userID, subjectID, macroId, state))

      if (currentStatesMap contains state) {
        log.debug("State /%s/%s/%s is already running".format(userID, subjectID, state))
        // TODO hier message wegen modaljoin!
        if (statesMap(state).stateType == ModalJoinStateType) {
          currentStatesMap(state) ! TransitionJoined
          data.blockingHandlerActor ! UnBlockUser(userID)
        }
      } else {
        log.debug("get statesMap, parse it and add result to currentStateMap")
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
      macroId,
      self,
      context.parent,
      processInstanceActor,
      inputPoolActor,
      internalStatus,
      modalSplitList)

    // create the actor which matches to the statetype
    state.stateType match {
      case ActStateType => {
        context.actorOf(Props(new ActStateActor(stateData)), "ActStateActor____" + UUID.randomUUID().toString)
      }

      case SendStateType => {
        context.actorOf(Props(new SendStateActor(stateData)), "SendStateActor____" + UUID.randomUUID().toString)
      }

      case ReceiveStateType => {
        context.actorOf(Props(new ReceiveStateActor(stateData)), "ReceiveStateActor____" + UUID.randomUUID().toString)
      }

      case EndStateType =>
        context.actorOf(Props(new EndStateActor(stateData)), "EndStateActor____" + UUID.randomUUID().toString)

      case CloseIPStateType => {
        context.actorOf(Props(new CloseIPStateActor(stateData)), "CloseIPStateActor____" + UUID.randomUUID().toString)
      }

      case OpenIPStateType => {
        context.actorOf(Props(new OpenIPStateActor(stateData)), "OpenIPStateActor____" + UUID.randomUUID().toString)
      }

      case IsIPEmptyStateType => {
        context.actorOf(Props(new IsIPEmptyStateActor(stateData)), "IsIPEmptyStateActor____" + UUID.randomUUID().toString)
      }

      case ModalSplitStateType => {
        context.actorOf(Props(new ModalSplitStateActor(stateData)), "ModalSplitStateActor____" + UUID.randomUUID().toString)
      }

      case ModalJoinStateType => {
        context.actorOf(Props(new ModalJoinStateActor(stateData)), "ModalJoinStateActor____" + UUID.randomUUID().toString)
      }

      case SplitGuardStateType => {
        context.actorOf(Props(new SplitGuardStateActor(stateData)), "SplitGuardStateActor____" + UUID.randomUUID().toString)
      }

      case MacroStateType => {
        context.actorOf(Props(new MacroStateActor(stateData)), "MacroStateActor____" + UUID.randomUUID().toString)
      }

      case ActivateStateType => {
        context.actorOf(Props(new ActivateStateActor(stateData)), "ActivateStateActor____" + UUID.randomUUID().toString)
      }

      case DeactivateStateType => {
        context.actorOf(Props(new DeactivateStateActor(stateData)), "DeactivateStateActor____" + UUID.randomUUID().toString)
      }

      case ArchiveStateType => {
        context.actorOf(Props(new ArchiveStateActor(stateData)), "ArchiveStateActor____" + UUID.randomUUID().toString)
      }

      case ChooseAgentStateType => {
        context.actorOf(Props(new ChooseAgentStateActor(stateData)), "ChooseAgentStateActor____" + UUID.randomUUID().toString)
      }

      case DecisionStateType => {
        context.actorOf(Props(new DecisionStateActor(stateData)), "DecisionStateActor____" + UUID.randomUUID().toString)
      }

      case BlackboxStateType => {
        context.actorOf(Props(new BlackboxStateActor(stateData)), "BlackboxStateActor____" + UUID.randomUUID().toString())
      }
    }
  }
}
