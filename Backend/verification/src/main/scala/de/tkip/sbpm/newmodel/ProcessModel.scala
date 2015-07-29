package de.tkip.sbpm.newmodel

import ProcessModelTypes._
import StateTypes._

case class ProcessModel(id: ProcessId,
                        name: String,
                        subjects: Set[SubjectLike],
                        // TODO mit dieser Map werden die messagetypes und ihre MessageContents spaeter spezifiziert
                        messageTypes: Map[MessageType, MessageContentType] = Map()) {
  val startSubjects = subjects collect { case s: Subject if (s.startSubject) => s }
  // requirements
  require(
    subjects.size > 0,
    "A process must contain at least one subject")
  require(
    (subjects map (_.id)).size == subjects.size,
    "Subject id's must be unique")
  require(
    startSubjects.size > 0,
    "A process needs at least one start-subject")
  for (
    sub <- subjects;
    state <- sub.states;
    mcParams <- state.transitions collect { case Transition(m: CommunicationParams, _, _) => m }
  ) {
    require(
      subjects map (_.id) contains mcParams.subject,
      "The target of Communication Transitions musst exists, failed for: "
        + mcParams.subject)
  }
  for (
    sub <- subjects;
    state <- sub.states.filter(_.stateType == Send);
    mcParams <- state.transitions collect { case Transition(m: CommunicationParams, _, _) => m }
  ) {
    val related = subjects.find(_.id == mcParams.subject).get
    if (related.multi) {
      require(
        mcParams.channelVar.isDefined,
        "A Send to a MultiSubject musst be a Send to Var")
      require(mcParams.min == AllUser, "Send to Var musst be to AllUser")
    }
  }
  for (
    sub <- subjects;
    relatedSubject <- sub.states.filter(s => s.stateType == Function).map(_.serviceParams)
      collect { case NewSubjectInstances(r, _, _, _) => r }
  ) {
    require(
      subjects.map(_.id).contains(relatedSubject),
      "The target of CreateNewSubjectInstance musst exists, failed for: "
        + relatedSubject)
  }

  // helping methods
  private val subjectMap = subjects collect { case s: Subject => (s.id, s) } toMap
  private val allSubjectMap = subjects map (s => (s.id, s)) toMap
  def subject(id: SubjectId): Subject = subjectMap(id)
  def subjectOrInterFace(id: SubjectId): SubjectLike = allSubjectMap(id)
  def state(subjectId: SubjectId, stateId: StateId): State =
    subjectMap(subjectId) match {
      case s: Subject => s.state(stateId)
      case s => s.states.find(_.id == stateId).getOrElse(null)
    }
}