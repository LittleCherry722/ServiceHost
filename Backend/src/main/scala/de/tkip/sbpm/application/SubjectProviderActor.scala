package de.tkip.sbpm.application

import akka.actor._
import akka.pattern.ask

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await

import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application._

case class SubjectCreated(processID: ProcessID,
                          processInstanceID: ProcessInstanceID,
                          subjectID: SubjectID,
                          ref: SubjectRef)
class SubjectProviderActor(val userID: UserID, val processManagerRef: ProcessManagerRef) extends Actor {

  private type Subject = SubjectCreated

  private var subjects = Set[Subject]()

  processManagerRef ! RegisterSubjectProvider(userID, self)

  def receive = {
    case get: GetAvailableActions => {
      // remove terminated subjects
      subjects = subjects.filter(!_.ref.isTerminated)
      // collect for the filtered list
      context.actorOf(Props(new AvailableActionsCollectorActor)) !
        CollectAvailableActions(get, subjects.filter(_.processInstanceID != get.processInstanceID))
    }

    case subject: SubjectCreated => {
      subjects += subject
    }

    case ea: ExecuteAction =>
      // TODO muss performanter gehen weils nur ein subject ist
      for (subject <- subjects.filter(s => s.processInstanceID != ea.processInstanceID && s.subjectID == ea.subjectID)) {
        //        context.actorOf(Props(new ActionExecuteActor)) ! (subject.ref, ea)
        subject.ref ! ea
      }

    case message: AnswerAbleMessage => {
      // just forward all messages from the frontend which are not
      // required in this Actor
      processManagerRef.forward(message)
    }

    case message: AnswerMessage[_] => {
      // send the Answermessages to the SubjectProviderManager
      context.parent ! message // TODO forward oder tell?
    }

    case s => {
      println("SubjectProvider not yet implemented: " + s)
    }
  }

  /**
   * This class is responsible to send an execute action request and to handle the answer
   */
  private class ActionExecuteActor extends Actor {
    def receive = {
      case (subject: SubjectRef, action: ExecuteAction) =>
        implicit val timeout = akka.util.Timeout(500)
        val future = subject ? action
        val answer = Await.result(future, timeout.duration)
        answer match {
          case ae: ActionExecuted =>
            println("Action executed: " + ae)
            context.stop(self)
          case s =>
            println("ActionExecuteActor does not support: " + s)
            context.stop(self)
        }
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
