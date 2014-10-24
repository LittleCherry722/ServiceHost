package de.tkip.sbpm.newmodel

import ProcessModelTypes._
import StateTypes._
import Operation._

// => InternalFunction
sealed trait InternalServiceParams
case object NoServiceParams extends InternalServiceParams
case class ActivateState(id: StateId) extends InternalServiceParams
case class DeactivateState(id: StateId) extends InternalServiceParams

// Executung a macro needs the macros name and the state, where to start the execution
case class ExecuteMacro(macroName: String) extends InternalServiceParams

// Open and Close InputPool with wildcard operator
// if the Option is None, it means * (all)
case class OpenIP(messageType: Option[MessageType],
                  subjectId: Option[SubjectId]) extends InternalServiceParams
case class CloseIP(messageType: Option[MessageType],
                   subjectId: Option[SubjectId]) extends InternalServiceParams
case class IsIPEmpty(messageType: Option[MessageType],
                     subjectId: Option[SubjectId]) extends InternalServiceParams

case class VariableManipulation(v1: VarName,
                                op: Operation,
                                v2: Option[VarName],
                                target: VarName)
  extends InternalServiceParams {

  // requirements
  op match {
    case Concatenation | Difference =>
      require(v2 != None, "binop needs second variable")
    case Selection | ExtractMessageContent =>
      require(v2 == None, "unary op does not allow second variable")
    case s =>
      require(false, "Unknown operator: " + s)
  }
}

/**
 * Creates new SubjectInstances of
 */
case class NewSubjectInstances(subject: SubjectId,
                               min: RangeLimit,
                               max: RangeLimit,
                               storeVar: String)
  extends InternalServiceParams {

  // requirements
  (min, max) match {
    case (Number(n), Number(m)) => require(n <= m, "min <= max")
    case (Number(_), AllUser) =>
    case _ => require(false, "Unsupported NewSubjectInstances parameters")
  }
}
