package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.BehaviourStateActor
import de.tkip.sbpm.model.EndState
import de.tkip.sbpm.model.ReceiveState
import de.tkip.sbpm.model.StartState
import de.tkip.sbpm.model.SendState
import de.tkip.sbpm.model.ActState
import de.tkip.sbpm.model.State
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.model.EndState

case class ExecuteState(state: StateID)
case class ExecuteStartState() // TODO noetig? / anders nennen?

/**
 * contains the business logic that will be modeled by the graph
 */
class InternalBehaviorActor(processInstanceRef: ProcessInstanceRef,
                            subjectName: String,
                            userID: UserID,
                            inputPoolActor: ActorRef) extends Actor {
  private val statesMap = collection.mutable.Map[StateID, State]()
  private var startState: StateID = ""
  private var currentState: BehaviorStateRef = null

  def receive = {
    case state: State =>
      addState(state)

    // TODO wie die states ausführen? Eigener Stateaktor oder useranfragen über internalbehavior
    case e: ExecuteStartState =>
      println("execute: " + statesMap(startState))
      // TODO hier history log?
      execute(startState)

    case e: ExecuteState =>
      println("execute: " + statesMap(e.state))
      // TODO hier history log?
      execute(e.state)

    case s => println("InternalBehaviorActor not yet implemented: " + s)
  }

  private def execute(state: StateID) {
    //    currentState ! End // nötig?
    if (currentState != null) {
      context.stop(currentState)
      currentState = null
    }

    currentState = parseState(statesMap(state))

  }


  private def addState(state: State) {
    if (state.stateType == StartStateType) {
      startState = state.id
    }
    statesMap += state.id -> state
  }

  private def parseState(state: State) = {
    state.stateType match {
      case StartStateType => if (state.transitions.size == 1) {
        context.actorOf(Props(
          StartState(state.id, state.transitions(0), self, processInstanceRef, subjectName, userID, inputPoolActor)))
      } else {
        throw new IllegalArgumentException("Startstates may only have 1 Transition")
      }
      // TODO state action?
      case ActStateType =>
        context.actorOf(Props(
          ActState(state.id, state.name, state.transitions, self, processInstanceRef, subjectName, userID, inputPoolActor)))

      case SendStateType =>
        context.actorOf(Props(
          SendState(state.id, state.transitions, self, processInstanceRef, subjectName, userID, inputPoolActor)))

      case ReceiveStateType =>
        context.actorOf(Props(
          ReceiveState(state.id, state.transitions, self, processInstanceRef, subjectName, userID, inputPoolActor)))

      case EndStateType =>
        context.actorOf(Props(
          EndState(state.id, self, processInstanceRef, subjectName, userID, inputPoolActor)))
    }
  }
}
