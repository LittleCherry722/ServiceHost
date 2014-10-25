package de.tkip.sbpm.verification

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.lts.LtsLabel

package object succ {
  type LtsStateSuccessorsFunction = Function2[GlobalFunctions, LtsState, Set[(LtsState, LtsLabel)]]
  type SubjectSuccessorFunction = Function3[GlobalFunctions, LtsState, SubjectStatus, Set[Successor]]
  type StateSuccessorFunction = Function4[GlobalFunctions, LtsState, SubjectStatus, ExtendedState, Set[Successor]]
}