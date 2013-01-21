package de.tkip.sbpm.application

import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.model._
import de.tkip.sbpm.application.subject.BehaviorRequest

case class ExecuteState(state: StateID)
case class ExecuteStartState() // TODO noetig? / anders nennen?

/**
 * contains the business logic that will be modeled by the graph
 */
class InternalBehaviorActor(processInstanceRef: ProcessInstanceRef,
                            subjectID: SubjectID,
                            userID: UserID,
                            inputPoolActor: ActorRef) extends Actor {
  private val statesMap = collection.mutable.Map[StateID, State]()
  private var startState: StateID = ""
  private var currentState: BehaviorStateRef = null

  def receive = {
    case state: State =>
      addState(state)
    // TODO wie die states ausfuehren? Eigener Stateaktor oder useranfragen ueber internalbehavior
    case e: ExecuteStartState =>
      println("execute: " + statesMap(startState))
      // TODO hier history log?
      execute(startState)

    case e: ExecuteState =>
      println("execute: " + statesMap(e.state))
      // TODO hier history log?
      execute(e.state)

    case br: BehaviorRequest =>
      if (currentState != null) {
        currentState.forward(br)
      }

    case n => println("Not yet supported: " + n)
  }

  private def execute(state: StateID) {
    //    currentState ! End // n�tig?
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
          StartState(state.id, state.transitions(0), self, processInstanceRef, subjectID, userID, inputPoolActor)))
      } else {
        throw new IllegalArgumentException("Startstates may only have 1 Transition")
      }
      // TODO state action?
      case ActStateType =>
        context.actorOf(Props(
          ActState(state.id, state.name, state.transitions, self, processInstanceRef, subjectID, userID, inputPoolActor)))

      case SendStateType =>
        context.actorOf(Props(
          SendState(state.id, state.transitions, self, processInstanceRef, subjectID, userID, inputPoolActor)))

      case ReceiveStateType =>
        context.actorOf(Props(
          ReceiveState(state.id, state.transitions, self, processInstanceRef, subjectID, userID, inputPoolActor)))

      case EndStateType =>
        context.actorOf(Props(
          EndState(state.id, self, processInstanceRef, subjectID, userID, inputPoolActor)))
    }
  }
}
