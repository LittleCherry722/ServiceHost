package de.tkip.sbpm.newmodel

import ProcessModelTypes._
import StateTypes._

// Exitparams are also used to identify the transition type
sealed trait ExitParams

// Multi/Single-Send/Receive
case class CommunicationParams(messageType: MessageType,
                               // if the messageContent is a Variable:
                               // the name of the variable,
                               //  whose value should be sent
                               contentVarName: Option[String],
                               // the id of the related subject
                               subject: SubjectId,
                               min: RangeLimit,
                               max: RangeLimit,
                               // variable to extract the target channels
                               channelVar: Option[String],
                               // variable to store the messages in
                               storeVar: Option[String])
  extends ExitParams {

  // requirements
  min match {
    case Number(n) => require(n > 0, "min musst be greater than zero")
    case _ =>
  }
  (min, max) match {
    case (Number(n), Number(m)) => require(n <= m, "min <= max")
    // everything is okay:
    case (Number(_), AllMessages) | (AllMessages, AllMessages)
      | (Number(_), AllUser) | (AllUser, AllUser) =>
    // if its not okay, the requirement fails
    case _ => require(false, "Invalid types for min, max")
  }
}

// timeout transition
case class TimeoutParam(duration: Int) extends ExitParams

// a transition, which manual cancels a state and fires the transition
case object BreakUpParam extends ExitParams

// name of the action
case class ActParam(text: String) extends ExitParams

// implicit transition for split state guards
case object ImplicitTransitionParam extends ExitParams

// to show that no exit params are needed
case object NoExitParams extends ExitParams

// the 2 Transitions for the Is IP empty function state
sealed trait IsIPEmptyParam extends ExitParams
case object TrueParam extends IsIPEmptyParam
case object FalseParam extends IsIPEmptyParam

