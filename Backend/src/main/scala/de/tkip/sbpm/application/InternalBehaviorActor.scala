package de.tkip.sbpm.application

import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.model._
import de.tkip.sbpm.application.subject.SubjectBehaviorRequest
import de.tkip.sbpm.application.subject.ActionExecuted
import de.tkip.sbpm.application.subject.ExecuteAction

// TODO hierher oder alle zusammen in eine Datei
case class ExecuteState(state: StateID) extends SubjectBehaviorRequest
case class ExecuteStartState() extends SubjectBehaviorRequest // TODO noetig? / anders nennen?

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
    case ess: ExecuteStartState =>
      println("execute: " + statesMap(startState))
      // TODO hier history log?
      setState(startState)
      if (ess.isInstanceOf[Debug]) {
        currentState ! new ExecuteState(startState) with Debug
      } else {
        currentState ! ExecuteAction(userID,
                         -1,
                         subjectID,
                         startState,
                         StateType.StartStateType,
                         null)
      }

    case es: ExecuteState =>
      println("execute: " + statesMap(es.state))
      // TODO hier history log?
      setState(es.state)
      currentState ! es

    case br: SubjectBehaviorRequest =>
      if (currentState != null) {
        currentState.forward(br)
      }
      
    case ae: ActionExecuted =>
      setState(ae.stateID)

    case n => println("InternalBehavior - Not yet supported: " + n + " " + subjectID)
  }

  private def setState(state: StateID) {
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
