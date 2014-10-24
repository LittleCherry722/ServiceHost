package de.tkip.sbpm.verification.succ

import de.tkip.sbpm.verification.lts._
import de.tkip.sbpm.verification.subject.SubjectStatus
import de.tkip.sbpm.newmodel.Subject
import de.tkip.sbpm.verification.subject.SubjectStatus
import de.tkip.sbpm.newmodel.Channel

trait SuccessorStatePriority
case object UsualState extends SuccessorStatePriority
case object ObserverState extends SuccessorStatePriority
case object EndState extends SuccessorStatePriority

object VerificationSuccessor {
  def main(args: Array[String]) {
    // TODO raus
    def x(i: Int) = SubjectStatus(null, Channel(i.toString, 2), null, null, null)
    println(
      combine(
        LtsState(Map()),
        Set(Set(SubjectSuccessor(x(1), Tau, 1), SubjectSuccessor(x(2), Tau, 2)),
          Set(SubjectSuccessor(x(3), Tau, 3), SubjectSuccessor(x(4), Tau, 4)))))
  }
  /**
   * combines the possible SubjectSuccessors to the corresponding LtsSuccessors
   * by using the cartesian product
   */
  def combine(preState: LtsState, subjects: Set[Set[SubjectSuccessorLike]]): Set[(LtsState, LtsLabel)] = {
    // create the cartesian product
    val wrapped = subjects.map(_.map(Set(_)))
    if (wrapped.isEmpty) {
      return Set()
    }
    val combined = wrapped.reduceLeft((xs, ys) => for { x <- xs; y <- ys } yield x ++ y)

    def insertSubjects(subjects: Set[SubjectSuccessorLike]): LtsState = {
      var iterState = preState
      for (subjectLike <- subjects) {
        subjectLike match {
          case s: SubjectSuccessor => {
            val subject = s.subject
            val newSubjectMap = iterState.subjectMap + (subject.channel -> subject)
            iterState = iterState.copy(subjectMap = newSubjectMap)
          }
          case SubjectTerminated(channel) => {
            val newSubjectMap = iterState.subjectMap - channel
            iterState = iterState.copy(subjectMap = newSubjectMap)
          }
        }
      }
      iterState
    }

    for {
      subjectSet <- combined
      labels = subjectSet.map(_.label).find(_ != Tau)
    } yield (insertSubjects(subjectSet), labels.getOrElse(Tau))
  }
}

sealed trait Successor {
  val label: LtsLabel
  val transitionPriority: Int
  val statePriority: SuccessorStatePriority
  val optional: Boolean

  def endState = statePriority == EndState
  def obsState = statePriority == ObserverState
}

sealed trait SubjectSuccessorLike extends Successor
case class SubjectTerminated(channel: Channel) extends SubjectSuccessorLike {
  val label: LtsLabel = Tau
  val transitionPriority: Int = 0
  val statePriority: SuccessorStatePriority = EndState
  val optional: Boolean = false
}

case class SubjectSuccessor(subject: SubjectStatus,
                            label: LtsLabel = Tau,
                            // the priority of the used transition
                            transitionPriority: Int = 0,
                            // the priority of the state
                            statePriority: SuccessorStatePriority = UsualState,
                            // if this successor is optional (e.g. Timeout transitions)
                            optional: Boolean = false) extends SubjectSuccessorLike

case class LtsSuccessor(state: LtsState,
                        label: LtsLabel = Tau,
                        // the priority of the used transition
                        transitionPriority: Int = 0,
                        // the priority of the state
                        statePriority: SuccessorStatePriority = UsualState,
                        // if this successor is optional (e.g. Timeout transitions)
                        optional: Boolean = false) extends Successor 