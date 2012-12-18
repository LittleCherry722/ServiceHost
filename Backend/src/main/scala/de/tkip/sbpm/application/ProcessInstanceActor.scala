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
      val subjectRef = context.actorOf(Props(new SubjectActor(context.parent, as.subjectName)))
      subjectMap += as.subjectName -> subjectRef
      subjectCounter += 1

    case End =>
      subjectCounter -= 1
      if (subjectCounter == 0) context.system.shutdown()

    case sm: SubjectMessage =>
      subjectMap(sm.fromCond.subjectName) forward sm

    case pr: StatusRequest =>
    	subjectMap.values.map(_ ! pr) // TODO: send to all subjects?

    case asts: AddState =>
      if (subjectMap.contains(asts.subjectName))
        subjectMap(asts.subjectName) ! asts.behaviourState
  }

}