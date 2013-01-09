package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessModel
import de.tkip.sbpm.model.Subject

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(val id: ProcessInstanceID, val process: ProcessModel) extends Actor {

  // TODO wie übergeben?
  private val contextResolver = context.actorOf(Props(new ContextResolverActor))

  // this pool stores the message to the subject, which does not exist,
  // but will be created soon (the UserID is requested)
  private var messagePool = Set[(ActorRef, SubjectMessage)]()

  private var subjectCounter = 0
  private val subjectMap = collection.mutable.Map[SubjectName, SubjectRef]()

  def receive = {

    case as: AddSubject =>
      val subject: Subject = getSubject(as.subjectName)

      println("addsubject" + subject)
      val subjectRef = context.actorOf(Props(new SubjectActor(self, subject)))
      subjectMap += subject.subjectName -> subjectRef
      subjectCounter += 1

      println("process " + id + " created subject " + subject.subjectName + " for user " + as.userID) //TODO
      // if there are messages to deliver to the new subject,
      // forward them to the subject 
      if (!messagePool.isEmpty) {
        for ((orig, sm) <- messagePool if sm.toCond.subjectName == subject.subjectName) {
          subjectRef.!(sm)(orig)
        }
        messagePool = messagePool.filterNot(_._2.toCond.subjectName == subject.subjectName)
      }

      // TODO subjecte direkt ausfuehren?
      subjectRef ! ExecuteRequest(as.userID, id)

    case End =>
      println("shutting down processInstance " + id)
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
        messagePool += ((sender, sm))
        // ask the Contextresolver for the userid to answer with an AddSubject
        contextResolver !
          RequestUserID(
            SubjectInformation(sm.toCond.subjectName),
            AddSubject(_, id, sm.toCond.subjectName))
      }

    case pr: ExecuteRequest =>
      println("execute")
      subjectMap.values.map(_ ! pr) // TODO: send to all subjects?

    case asts: AddState =>
      if (subjectMap.contains(asts.subjectName))
        subjectMap(asts.subjectName) ! asts.behaviourState

    case ss => println("ProcessInstaceActor: not yet implemented Message: " + ss)
  }

  private def getSubject(name: String): Subject = {
    // TODO increase performance
    process.subjects.find(_.subjectName == name).get
  }
}