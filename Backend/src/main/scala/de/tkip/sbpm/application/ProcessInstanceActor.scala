package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor extends Actor {

  private val subjectMap = collection.mutable.Map[SubjectName, SubjectRef]()
  private var subjectCounter = 0

  def receive = {
    case as: AddSubject =>
      // TODO hier sollte man lieber self uebergeben, da man sonst selbst bei aufrufen übersprungen wird
      // instanznamen von manager in instance ändern
      //    val subjectRef = context.actorOf(Props(new SubjectActor(context.parent, as.subjectName)))
      val subjectRef = context.actorOf(Props(new SubjectActor(self, as.subjectName)))
      subjectMap += as.subjectName -> subjectRef
      subjectCounter += 1

    case End =>
      subjectCounter -= 1
      if (subjectCounter == 0) {
        context.system.shutdown()
      }

    case sm: SubjectMessage =>
      subjectMap(sm.toCond.subjectName) forward sm

    case pr: ExecuteRequest =>
      subjectMap.values.map(_ ! pr) // TODO: send to all subjects?

    case asts: AddState =>
      if (subjectMap.contains(asts.subjectName))
        subjectMap(asts.subjectName) ! asts.behaviourState
  }

}