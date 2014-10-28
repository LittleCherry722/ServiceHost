package de.tkip.sbpm.verification

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.verification.graph.VerificationGraphWriter
import de.tkip.sbpm.verification.lts.{Lts, LtsState}

class Verificator(model: ProcessModel) {

  var lts = Lts(Set(), Set(), LtsState(Map()))

  var optimize: Boolean = false

  def verificate() {
    lts = Verification.buildLts(model, optimize)
  }

  def pruneLts() {
    lts = lts.copy(transitions = lts.transitions.filterNot(x => x.fromState == x.toState))

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
