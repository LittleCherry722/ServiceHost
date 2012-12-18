package de.tkip.sbpm.application
import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

/**
 * contains the businesslogic that will be modeled by the Graph
 */
class InternalBehaviorActor extends Actor {
  private val statesMap = collection.mutable.Map[StateID, BehaviourState]()
  private var startState: StateID = ""

  def receive = {
    case b: BehaviourState => addState(b)
    case p: ProcessBehaviour => processBehaviour(p.processManager, p.subjectName, p.subjectProviderName, p.inputPool)
    case _ => "not yet implemented"
  }

  private def addState(state: BehaviourState) {
    println("add behaviorState: " + state.stateAction)
    if (startState.isEmpty()) startState = state.stateID
    statesMap += state.stateID -> state
  }

  private def processBehaviour(processManager: ProcessManagerRef, subjectName: SubjectName, subjectProviderName: SubjectName, inputPool: ActorRef) {
    var nextstate = startState

    while (nextstate != null) {
      nextstate = statesMap(nextstate).performAction(processManager, subjectName, subjectProviderName, inputPool)
    }

  }

} // class InternalBehaviour