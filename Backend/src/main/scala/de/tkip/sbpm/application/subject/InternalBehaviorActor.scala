package de.tkip.sbpm.application.subject

import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.model._

/**
 * contains the business logic that will be modeled by the graph
 */
class InternalBehaviorActor(processInstanceRef: ProcessInstanceRef,
                            subjectID: SubjectID,
                            userID: UserID,
                            inputPoolActor: ActorRef) extends Actor {
  private val statesMap = collection.mutable.Map[StateID, State]()
  private var startState: StateID = 0
  private var currentState: BehaviorStateRef = null

  def receive = {
    case state: State => {
      addState(state)
    }

    case ess: StartSubjectExecution => {
      // TODO hier history log?
      nextState(startState)
      if (currentState != null) {
        currentState ! ess
      }
    }

    case es: NextState => {
      // TODO hier history log?
      nextState(es.state)
    }

    case ea: ExecuteAction =>
      currentState.forward(ea)

    case terminated: SubjectTerminated => {
      context.parent ! terminated
    }

    case br: SubjectBehaviorRequest => {
      if (currentState != null) {
        currentState.forward(br)
      } else {
        // TODO signalisieren das die message nicht ausfuehrbar ist
      }
    }

    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    case n => {
      println("InternalBehavior - Not yet supported: " + n + " " + subjectID)
    }
  }

  private def addState(state: State) {
    if (state.stateType == StartStateType) {
      println("startstate " + state)
      startState = state.id
    }
    statesMap += state.id -> state
  }

  private def nextState(state: StateID) {
    //    currentState ! End // n�tig?
    if (currentState != null) {
      context.stop(currentState)
      currentState = null
    }

    if (statesMap.contains(state)) {
      println("Execute: /" + userID + "/" + subjectID + "/" + state)
      currentState = parseState(statesMap(state))
    } else {
      System.err.println("ERROR: /" + userID + "/" + subjectID + "/" + state + "does not exist")
    }
  }

  private def parseState(state: State) = {
    state.stateType match {
      case StartStateType => if (state.transitions.size == 1) {
        context.actorOf(Props(
          StartStateActor(state.id, state.transitions(0), self, processInstanceRef, subjectID, userID, inputPoolActor)))
      } else {
        throw new IllegalArgumentException("Startstates may only have 1 Transition")
      }
      // TODO state action?
      case ActStateType =>
        context.actorOf(Props(
          ActStateActor(state.id, state.name, state.transitions, self, processInstanceRef, subjectID, userID, inputPoolActor)))

      case SendStateType =>
        context.actorOf(Props(
          SendStateActor(state.id, state.transitions, self, processInstanceRef, subjectID, userID, inputPoolActor)))

      case ReceiveStateType =>
        context.actorOf(Props(
          ReceiveStateActor(state.id, state.transitions, self, processInstanceRef, subjectID, userID, inputPoolActor)))

      case EndStateType =>
        context.actorOf(Props(
          EndStateActor(state.id, self, processInstanceRef, subjectID, userID, inputPoolActor)))
    }
  }
}
