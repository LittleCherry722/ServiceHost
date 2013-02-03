package de.tkip.sbpm.application

import akka.actor._
import akka.pattern.ask
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application._
import de.tkip.sbpm.ActorLocator

protected case class SubjectCreated(userID: UserID,
                                    processID: ProcessID,
                                    processInstanceID: ProcessInstanceID,
                                    subjectID: SubjectID,
                                    ref: SubjectRef)
    extends SubjectProviderMessage

class SubjectProviderActor(userID: UserID) extends Actor {

  private type Subject = SubjectCreated

  private var subjects = Set[Subject]()

  private lazy val processManagerActor = ActorLocator.processManagerActor

  processManagerActor ! RegisterSubjectProvider(userID, self)

  def receive = {
    case subject: SubjectCreated => {
      subjects += subject
    }

    case get: GetAvailableActions => {
      // TODO increase performance
      // remove the subjects the user is not interested about:
      // - terminated
      // - different process instance id
      // - different subject id
      val collectSubjects: Set[Subject] =
        subjects.filter(
          (s: Subject) =>
            !s.ref.isTerminated &&
              (if (get.processInstanceID == AllProcessInstances)
                true
              else
                (if (get.subjectID == AllSubjects)
                  get.processInstanceID == s.processInstanceID
                else
                  get.processInstanceID == s.processInstanceID &&
                    get.subjectID == s.subjectID)))
      // collect for the filtered list
      context.actorOf(Props(new AvailableActionsCollectorActor)) !
        CollectAvailableActions(get, collectSubjects)
    }

    // general matching
    case message: PersistenceMessage => {
      processManagerActor.forward(message)
    }

    // Route processInstance messages to the process manager
    case message: ProcessInstanceMessage => {
      processManagerActor ! message
    }

    // send subject messages direct to the subject
    case message: SubjectMessage => {
      // TODO muss performanter gehen weils nur ein subject ist
      for (subject <- subjects.filter(s => s.processInstanceID == message.processInstanceID && s.subjectID == message.subjectID)) {
        subject.ref ! message
      }
    }

    case message: AnswerMessage => {
      // send the Answermessages to the SubjectProviderManager
      context.parent ! message // TODO forward oder tell?
    }

    case message: AnswerAbleMessage => {
      // just forward all messages from the frontend which are not
      // required in this Actor
      processManagerActor.forward(message)
    }

    case s => {
      println("SubjectProvider not yet implemented: " + s)
    }
  }

  private case class CollectAvailableActions(request: GetAvailableActions, subjects: Set[Subject])
  /**
   * This class is responsible to collect the available actions of a set of subjects
   */
  private class AvailableActionsCollectorActor extends Actor {
    def receive = {
      case CollectAvailableActions(request, sub) => {
        implicit val timeout = akka.util.Timeout(5000)

        val futures = ArrayBuffer[scala.concurrent.Future[Any]]()
        for (subject <- sub) {
          val future = subject.ref ? GetAvailableAction(request.processInstanceID)
          futures += future
        }
        // TODO non-blocking?
        //        val c = for (c <- futures.map(_.mapTo[Int])) yield c
        //        val x = Await.result(c, timeout.duration)
        var h = null
        val actions = ArrayBuffer[AvailableAction]()
        for (f <- futures) {
          try {

            actions += Await.result(f, timeout.duration).asInstanceOf[AvailableAction]
          } catch {
            case h: java.util.concurrent.TimeoutException => {
              println(f + " timed out")
            }
          }
        }

        // results ready -> return
        sender ! AvailableActionsAnswer(request, actions.toArray)

        // actions collected -> stop this actor
        context.stop(self)
      }
    }
  }
}
