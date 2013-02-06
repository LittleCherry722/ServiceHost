package de.tkip.sbpm.application.subject

import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.history.{
  Transition => HistoryTransition,
  Message => HistoryMessage,
  State => HistoryState
}
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.model._
import akka.event.Logging

// TODO this is for history + statechange
case class ChangeState(currenState: StateID,
                       nextState: StateID,
                       history: HistoryMessage)

/**
 * contains the business logic that will be modeled by the graph
 */
class InternalBehaviorActor(processInstanceActor: ProcessInstanceRef,
                            subjectID: SubjectID,
                            userID: UserID,
                            inputPoolActor: ActorRef) extends Actor {
  private val statesMap = collection.mutable.Map[StateID, State]()
  private var startState: StateID = 0
  private var currentState: BehaviorStateRef = null

  
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

    case ess: StartSubjectExecution => {
      nextState(startState)
      if (currentState != null) {
        currentState ! ess
      }
    }

    case es: NextState => {
      nextState(es.state)
    }

    case change: ChangeState => {
      // TODO check if current state is correct?
      nextState(change.nextState)

      val current: State = statesMap(change.currenState)
      val next: State = statesMap(change.nextState)
      // create the History Entry and send it to the subject
      context.parent !
        HistoryTransition(
          HistoryState(current.name, current.stateType.toString()),
          HistoryState(next.name, next.stateType.toString()),
          change.history)
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
      } else {
        // TODO signalisieren das die message nicht ausfuehrbar ist
      }
    }

    case h: de.tkip.sbpm.application.history.Transition => {
      context.parent ! h
    }

    // general matching
    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    case n => {
      logger.error("InternalBehavior - Not yet supported: " + n + " " + subjectID)
    }
  }

  private def addState(state: State) {
    if (state.stateType == StartStateType) {
      logger.debug("startstate " + state)
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
      logger.debug("Execute: /" + userID + "/" + subjectID + "/" + state)
      currentState = parseState(statesMap(state))
    } else {
      logger.error("ERROR: /" + userID + "/" + subjectID + "/" + state + "does not exist")
    }
  }

  private def parseState(state: State) = {
    state.stateType match {
      case StartStateType => if (state.transitions.size == 1) {
        context.actorOf(Props(
          StartStateActor(state.id, state.transitions(0), self, processInstanceActor, subjectID, userID, inputPoolActor)))
      } else {
        throw new IllegalArgumentException("Startstates may only have 1 Transition")
      }
      // TODO state action?
      case ActStateType =>
        context.actorOf(Props(
          ActStateActor(state.id, state.name, state.transitions, self, processInstanceActor, subjectID, userID, inputPoolActor)))

      case SendStateType =>
        context.actorOf(Props(
          SendStateActor(state.id, state.transitions, self, processInstanceActor, subjectID, userID, inputPoolActor)))

      case ReceiveStateType =>
        context.actorOf(Props(
          ReceiveStateActor(state.id, state.transitions, self, processInstanceActor, subjectID, userID, inputPoolActor)))

      case EndStateType =>
        context.actorOf(Props(
          EndStateActor(state.id, self, processInstanceActor, subjectID, userID, inputPoolActor)))
    }
  }
}
