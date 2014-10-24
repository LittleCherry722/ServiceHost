package de.tkip.sbpm.verification.succ.state.srv

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

object createNewSubjectsSuccessors extends ServiceSuccessorFunction[NewSubjectInstances] {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState,
            create: NewSubjectInstances): Set[Successor] = {

    val subjectMap = ltsState.subjectMap
    create match {
      case NewSubjectInstances(relatedSubject, Number(min), Number(max), storeVar) => {
        Set()
        // every subject has to be terminated
        if (subjectMap.map(_._1).exists(_.subjectId == relatedSubject)) {
          Set()
        } else
          (for (count <- min to max) yield {
            val targetChannels =
              for (i <- 0 until count) yield Channel(relatedSubject, i)

            val targetSubjects: Set[SubjectStatus] =
              (for {
                targetChannel <- targetChannels
                targetSubject = global.createSubject(targetChannel)
              } yield targetSubject) toSet

            // create the variable for the created subjects
            val messageList =
              MessageList(targetSubjects.map(s => Message(NoContent, s.channel)).toList)
            val newSubj = subject.fireTransitionOf(state).addVar(storeVar, messageList)

            ltsState.successor(targetSubjects + newSubj, state.singleTransition)
          }) toSet
      }

      case _: NewSubjectInstances => throw new Exception("unsupported")
    }
  }
}