package de.tkip.sbpm.newmodel

import ProcessModelTypes._
import StateTypes._

case class State(id: StateId,
                 text: String,
                 stateType: StateType,
                 transitions: Set[Transition],
                 serviceParams: InternalServiceParams) {

  // requirements
  // Check exitparam types for the transitions
  stateType match {
    case Act => {
      require(
        (transitions collect {
          case t @ Transition(ActParam(_), _, _) => t
          case t @ Transition(TimeoutParam(_), _, _) => t
          case t @ Transition(BreakUpParam, _, _) => t
        }).size == transitions.size,
        "Invalid transition type for act state")
    }
    case Receive | Observer | Send => {
      require(
        (transitions collect {
          case t @ Transition(_: CommunicationParams, _, _) => t
          case t @ Transition(TimeoutParam(_), _, _) => t
          case t @ Transition(BreakUpParam, _, _) => t
        }).size == transitions.size,
        "Invalid transition type for receive state")
    }
    //    case Send =>
    case End => {
      require(
        transitions.size == 0,
        "An end state is not allowed to have transitions")
    }
    case Split => {
      require(
        (transitions collect {
          case t @ Transition(NoExitParams, _, _) => t
        }).size == transitions.size,
        "Invalid transition type for split state")
    }
    case SplitGuard => {
      require(transitions.size == 2 &&
        transitions.map(_.exitParams) == Set(NoExitParams, ImplicitTransitionParam),
        "A split guard need a NoExitParams and a ImplicitTransitionParam transition")
    }
    case Join => {
      require(
        transitions.size == 1 &&
          (transitions collect {
            case t @ Transition(NoExitParams, _, _) => t
          }).size == 1,
        "An join is only allowed to have one outgoing transition" +
          " without exit params")

    }
    case Function => {
    }
  }
  // check serviceparams
  if (stateType != Function) {
    require(
      serviceParams == NoServiceParams,
      "Only predefined service states are allowed to have an service params")
  } else serviceParams match {
    case NoServiceParams => {
      require(false, "An predefined service state needs a service")
    }

    case s => {
      require(transitions.size == 1, s + " needs 1 outgoing transition")
      require(
        transitions forall (_.exitParams == NoExitParams),
        s + " outgoing transitions are not allowed to have exit params")
    }
  }
}
