package de.tkip.sbpm.verification.lts

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.misc.HashCodeCache
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.subject.SubjectStatus
import de.tkip.sbpm.verification.subject.VerificationSubject
import de.tkip.sbpm.verification.subject.SubjectStatus

/*
 * activated subjects
 * Channel -> SubjectStatus
 *   Subject muss terminieren und dann raus
 */
case class LtsState(subjectMap: Map[Channel, VerificationSubject]) extends HashCodeCache {
  //  var hasSucc: Boolean = false
  lazy val normalSubjectMap: Map[Channel, SubjectStatus] =
    subjectMap collect { case (c, s: SubjectStatus) => (c, s) }

  def exchange(subject: VerificationSubject) =
    LtsState(subjectMap - subject.channel + (subject.channel -> subject))


  def -(channel: Channel): Successor =
    SubjectTerminated(channel)

  def successorSet(newSubject: SubjectStatus,
                   transition: Transition): Set[Successor] =
    Set(successor(newSubject, transition))

  def successorSet(newSubjects: Set[SubjectStatus],
                   transition: Transition): Set[Successor] =
    Set(successor(newSubjects, transition))

  def successor(newSubject: SubjectStatus,
                transition: Transition,
                statePriority: SuccessorStatePriority = UsualState): SubjectSuccessor = {
    SubjectSuccessor(
      newSubject,
      Tau,
      transition.priority,
      statePriority)
  }

  def successor(newSubjects: Set[SubjectStatus],
                transition: Transition): LtsSuccessor = {
    LtsSuccessor(
      LtsState(subjectMap ++ (newSubjects map (s => s.channel -> s))),
      Tau,
      transition.priority)
  }

  def mkString = subjectMap.map(_._2).mkString("", " | ", "")
}