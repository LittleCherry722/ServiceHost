package de.tkip.sbpm.verification

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.verification.lts._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._

object Verification {

  def buildLts(model: ProcessModel, optimize: Boolean = false): Lts = {
    val global = new GlobalFunctions(model, optimize)

    def successors(ltsState: LtsState) = ltsStateSuccessors(global, ltsState)

    def calculateLtsStartStates: Map[Channel, SubjectStatus] =
      (for {
        subject <- model.startSubjects
        val channel = Channel(subject.id, 0)
      } yield channel -> createSubject(model, channel)).toMap

    // berechne die Startzustaende
    val startLtsStates = calculateLtsStartStates

    // Die Kantenmenge ist zunaechst leer
    var edges = Set[LtsTransition]()
    // Die Knotenmenge und die Arbeitsmenge enthalten die Startzustaende
    var nodes = Set(LtsState(startLtsStates))
    var workSet = Set(LtsState(startLtsStates))
    // solange noch LTS Zustaende vorhanden sind auf denen gearbeitet werden kann
    while (workSet.nonEmpty) {
      val (nextWorkSet: Set[LtsState], newTransitions: Set[LtsTransition]) =
        (for {
          // iteriere über die aktuelle Arbeitsmenge
          ltsState <- workSet
          // und erstelle berechne die Nachfolger
          // iteriere anschließend über die Nachfolger
          (successor, label) <- successors(ltsState)
        } yield (successor, LtsTransition(ltsState, label, successor))).unzip

      // das die naechste Arbeitsmenge ist die neu Erstellte
      // ohne die LTS Zustaende, die schon bekannte sind
      workSet = nextWorkSet diff nodes
      // füge die neuen LTS Zustaende zu der Knotenmenge hinzu
      nodes ++= workSet
      // füge die neuen LTS Transitionen zu der Kantenmenge hinzu
      edges ++= newTransitions
    }

    // erstelle das LTS
    Lts(nodes, edges, LtsState(startLtsStates))
  }

  private def createSubject(model: ProcessModel, channel: Channel): SubjectStatus = {
    val subject = model.subject(channel.subjectId)
    SubjectStatus(subject, channel, InputPool.empty, Variables.empty,
      Set(ExtendedState(subject.state(subject.startState))))
  }
}
