package de.tkip.sbpm.verification.succ.state

import de.tkip.sbpm.newmodel.Channel
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.subject.SubjectStatus
import de.tkip.sbpm.verification.subject.ExtendedState
import de.tkip.sbpm.verification.succ.LtsSuccessor
import de.tkip.sbpm.verification.succ.GlobalFunctions
import de.tkip.sbpm.verification.succ.Successor
package object srv {
  type ServiceSuccessorFunction[Service] = Function5[GlobalFunctions, LtsState, SubjectStatus, ExtendedState, Service, Set[Successor]]
}