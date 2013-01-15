package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.BehaviourState
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
  private val statesMap = collection.mutable.Map[StateID, BehaviourState]()
  private var startState: StateID = ""
  private val currentState: StateID = ""

  def receive = {
    // TODO das kommt raus
    case b: BehaviourState => addState(b)

    case state: State      => addState(parseState(state))

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

    if (statesMap(state).isInstanceOf[EndState]) {
      println("Internalbehavior@" + subjectName + ": Exit")
    } else {
      val nextstate =
        statesMap(state).performAction(
          processInstanceRef,
          subjectName,
          userID.toString,
          inputPoolActor)
      self ! ExecuteState(nextstate)
    }

  }

  private def addState(state: BehaviourState) {
    // TODO raus momentan nur fuer kompatibilitaet vorheriger versionen
    if (startState.isEmpty()) startState = state.stateID

    if (state.isInstanceOf[StartState]) {
      startState = state.stateID
    }

    statesMap += state.stateID -> state
  }

  private def parseState(state: State) =
    state.stateType match {
      case StartStateType => if (state.transitions.size == 1) {
        StartState(state.id, state.transitions(0))
      } else {
        throw new IllegalArgumentException("Startstates may only have 1 Transition")
      }
      // TODO state action?
      case ActStateType     => ActState(state.id, state.name, state.transitions)

      case SendStateType    => SendState(state.id, state.transitions)

      case ReceiveStateType => ReceiveState(state.id, state.transitions)

      case EndStateType     => EndState(state.id)
    }
}
