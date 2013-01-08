package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(val id: ProcessID) extends Actor {

  // TODO wie übergeben?
  private val contextResolver = context.actorOf(Props(new ContextResolverActor))

  // this pool stores the message to the subject, which does not exist,
  // but will be created soon (the UserID is requested)
  private var messagePool = Set[SubjectMessage]()

  private var subjectCounter = 0
  private val subjectMap = collection.mutable.Map[SubjectName, SubjectRef]()

  def receive = {
    case as: AddSubject =>
      val subjectRef = context.actorOf(Props(new SubjectActor(self, as.subjectName)))
      subjectMap += as.subjectName -> subjectRef
      subjectCounter += 1

      println("process " + id + " created subject " + as.subjectName) //TODO
      if (!messagePool.isEmpty) {
        for (sm <- messagePool if sm.toCond.subjectName == as.subjectName) {
          subjectRef.forward(sm)
        }
        messagePool = messagePool.filterNot(_.toCond.subjectName == as.subjectName)
      }

    case End =>
      subjectCounter -= 1
      if (subjectCounter == 0) {
        context.system.shutdown()
      }

    case sm: SubjectMessage =>
      if (subjectMap.contains(sm.toCond.subjectName)) {
        // if the subject already exist just forward the message
        subjectMap(sm.toCond.subjectName).forward(sm)
      } else {
        // if the subject does not exist create the subject and forward the
        // message afterwards
        // store the message in the message-pool
        messagePool += sm
        // ask the Contextresolver for the userid to answer with an AddSubject
        contextResolver !
          RequestUserID(
            SubjectInformation(sm.toCond.subjectName),
            AddSubject(_, id, sm.toCond.subjectName))
      }

    case pr: ExecuteRequest =>
      subjectMap.values.map(_ ! pr) // TODO: send to all subjects?

    case asts: AddState =>
      if (subjectMap.contains(asts.subjectName))
        subjectMap(asts.subjectName) ! asts.behaviourState
  }

}