package de.tkip.sbpm.application.miscellaneous

import ProcessAttributes._
import de.tkip.sbpm.model.Transition

sealed trait AddStateMessage

case class AddStateToSubject(subjectName: SubjectName, addStateMessage: AddStateMessage) extends AddStateMessage

case class AddActState(id: StateID, stateAction: StateAction, transitions: Array[Transition]) extends AddStateMessage
case class AddEndState(StateID: StateID) extends AddStateMessage
case class AddReceiveState(s: StateID, val transitions: Array[Transition]) extends AddStateMessage
case class AddSendState(s: StateID, transitions: Array[Transition]) extends AddStateMessage

