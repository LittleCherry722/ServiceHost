package de.tkip.sbpm.application.subject

import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import akka.actor.ActorContext
import akka.actor.Props
import de.tkip.sbpm.application.miscellaneous.SubjectMessage
import de.tkip.sbpm.application.SubjectCreated
import akka.event.LoggingAdapter
import akka.actor.ActorRef
import de.tkip.sbpm.application.miscellaneous.BlockUser

/**
 * This class is responsible to hold a subjects, and can represent
 * a single subject or a multisubject
 */
class SubjectContainer(
  subject: Subject,
  processID: ProcessID,
  processInstanceID: ProcessInstanceID,
  logger: LoggingAdapter,
  blockingHandlerActor: ActorRef,
  increaseSubjectCounter: () => Unit,
  decreaseSubjectCounter: () => Unit)(implicit context: ActorContext) {
  import scala.collection.mutable.{ Map => MutableMap }

  private val multi = subject.multi
  private val external = subject.external

  private val subjects = MutableMap[UserID, SubjectInfo]()

  /**
   * Adds a Subject to this multisubject
   */
  // TODO ueberarbeiten
  def createSubject(userID: UserID) {
    val subjectData =
      SubjectData(
        userID,
        processInstanceID,
        context.self,
        blockingHandlerActor,
        subject)
    // create subject
    val subjectRef =
      context.actorOf(Props(new SubjectActor(subjectData)))
    // and store it in the map
    subjects += userID -> SubjectInfo(subjectRef, userID)

    logger.debug("Processinstance [" + processInstanceID + "] created Subject " +
      subject.id + " for user " + userID)

    // inform the subject provider about his new subject
    context.parent !
      SubjectCreated(userID, processID, processInstanceID, subject.id, subjectRef)

    reStartSubject(userID)
  }

  def handleSubjectTerminated(message: SubjectTerminated) {

    logger.debug("Processinstance [" + processInstanceID + "] Subject " + subject.id + "[" +
      message.userID + "] terminated")

    // decrease the subject counter
    decreaseSubjectCounter()

    subjects(message.userID).running = false
  }

  /**
   * Forwards a message to all Subjects of this MultiSubject
   */
  def send(message: SubjectToSubjectMessage) {

    if (message.target.toVariable) {
      // TODO why not targetUsers = var subjects?
      sendTo(message.target.varSubjects.map(_._2), message)
    } else {
      sendTo(message.target.targetUsers, message)
    }
  }

  def send(message: SubjectMessage) {
    if (subjects.contains(message.userID)) {
      subjects(message.userID).ref.forward(message)
    }
  }

  /**
   * Forwards the message to the array of subjects
   */
  private def sendTo(targetSubjects: Array[UserID],
    message: SubjectToSubjectMessage) {

    for (userID <- targetSubjects) {
      if (!subjects.contains(userID)) {
        createSubject(userID)
      } else if (!subjects(userID).running) {
        reStartSubject(userID)
      }

      //        blockingHandlerActor ! BlockUser(userID)
      subjects(userID).ref.forward(message)
    }
  }

  private def reStartSubject(userID: UserID) {
    if (subjects.contains(userID)) {
      blockingHandlerActor ! BlockUser(userID)
      increaseSubjectCounter()
      subjects(userID).running = true
      // start the execution
      subjects(userID).ref ! StartSubjectExecution()
    } else {
      logger.error("User %i unknown for subject %s, (re)start failed!"
        .format(userID, subject.id))
    }
  }

  private case class SubjectInfo(
    ref: SubjectRef,
    userID: UserID,
    var running: Boolean = true)
}