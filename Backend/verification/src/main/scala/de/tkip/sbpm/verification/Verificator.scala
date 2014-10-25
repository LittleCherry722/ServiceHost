package de.tkip.sbpm.verification

import scala.collection.mutable
import de.tkip.sbpm.factory.ProcessFactory
import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject.Variables
import de.tkip.sbpm.verification.subject.SubjectStatus
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.subject.InputPool
import de.tkip.sbpm.verification.subject.ClosedChannels
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.subject.ExtendedState
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.succ.GlobalFunctions
import de.tkip.sbpm.verification.lts.LtsTransition
import de.tkip.sbpm.verification.graph.VerificationGraphWriter
import de.tkip.sbpm.verification.lts.Tau
import de.tkip.sbpm.verification.lts.LtsTransition
import de.tkip.sbpm.verification.lts.LtsLabel
import de.tkip.sbpm.verification.lts.Lts

class Verificator(model: ProcessModel) {

  var lts = Lts(Set(), Set(), LtsState(Map()))

  var optimize: Boolean = false

  def verificate() {
    lts = Verification.buildLts(model, optimize)
  }

  def pruneLts() {
    lts = lts.copy(transitions = lts.transitions.filterNot(x => x.from == x.to))

    // Create the pruned LTS for an interface
    val in = model.subjects.find(_.isInstanceOf[InstantInterface])
    if (in.isDefined) {
      val interfaceName = in.get.id
      lts = lts.createFor(interfaceName)
      println(s"Create for $interfaceName")
    }
    lts = lts.prune
  }

  def printLtsSize() {
    println("LTS Size:")
    println("  LTS States:\t\t" + lts.states.size)
    println("  LTS Transitions:\t" + lts.transitions.size)
  }

  def checkValidity(): Boolean = {
    // return whether the lts is valid
    lts.valid
  }

  def writeGraph() {
    VerificationGraphWriter.writeLts(lts)
  }
}
