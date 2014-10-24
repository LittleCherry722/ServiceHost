package de.tkip.sbpm.verification.succ

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

package object optimize {
  type IsStateExecuteAbleFunction = Function4[GlobalFunctions, LtsState, SubjectStatus, ExtendedState, Boolean]
  type IsStateExecuteAbleFunction2 = Function3[GlobalFunctions, SubjectStatus, ExtendedState, Boolean]
  type GetStateExecuteAbleFunction = Function3[GlobalFunctions, LtsState, SubjectStatus, Set[ExtendedState]]
}