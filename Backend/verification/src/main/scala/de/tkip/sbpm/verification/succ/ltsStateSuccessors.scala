package de.tkip.sbpm.verification.succ

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.lts.LtsLabel
import de.tkip.sbpm.verification.lts.Tau
import de.tkip.sbpm.newmodel.RangeLimit

object ltsStateSuccessors extends LtsStateSuccessorsFunction {

  def apply(global: GlobalFunctions, ltsState: LtsState): Set[(LtsState, LtsLabel)] = {
    val successorSets =
      (for {
        subject <- ltsState.subjectMap collect { case (_, s: SubjectStatus) => s }
        successors = subjectSuccessors(global, ltsState, subject)
      } yield successors).toSet

    val ltsSuccs = successorSets.flatten.collect {
      case l: LtsSuccessor => (l.state, l.label)
    }
    val subjectSuccs = {
      successorSets.map(s => s.collect {
        case s: SubjectSuccessorLike => s
      }).filterNot(_.isEmpty).toSet
    }
    val combinedSubjects: Set[(LtsState, LtsLabel)] = {
      // if optimized combine the successors
      if (global.optimized) {
        VerificationSuccessor.combine(ltsState, subjectSuccs)
      } else {
        // else create one LTS state for every successor
        val subjectMap = ltsState.subjectMap
        for {
          successorSet <- subjectSuccs
          successor <- successorSet
        } yield {
          successor match {
            case s: SubjectSuccessor => {
              val subject = s.subject
              val channel = subject.channel
              (ltsState.copy(subjectMap = subjectMap - channel + (channel -> subject)),
                successor.label)
            }
            case SubjectTerminated(channel) => {
              (ltsState.copy(subjectMap = subjectMap - channel),
                successor.label)
            }
          }
        }
      }
    }
    ltsSuccs ++ combinedSubjects
  }
}