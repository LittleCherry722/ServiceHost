package de.tkip.sbpm.application

import akka.actor._
import akka.pattern.ask

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await

import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.AvailableAction
import de.tkip.sbpm.application.subject.GetAvailableAction

case class SubjectCreated(processID: ProcessID,
                          processInstanceID: ProcessInstanceID,
                          subjectID: SubjectID,
                          ref: SubjectRef)
class SubjectProviderActor(val userID: UserID, val processManagerRef: ProcessManagerRef) extends Actor {

  private type Subject = SubjectCreated

  private var subjects = Set[Subject]()

  def receive = {
    case get: GetAvailableActions => {
      // remove terminated subjects
      subjects = subjects.filter(!_.ref.isTerminated)
      // collect for the filtered list
      // TODO start collecting
      context.actorOf(Props(new AvailableActionsCollector)) !
        CollectAvailableActions(get, subjects.filter(_.processInstanceID == get.processInstanceID))
    }

    case spc: SubjectProviderCreated => {
      processManagerRef.forward(spc)
    }

    case subject: SubjectCreated => {
      subjects += subject
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

  private case class CollectAvailableActions(request: GetAvailableActions, subjects: Set[Subject])

  private class AvailableActionsCollector extends Actor {
    def receive = {
      case CollectAvailableActions(request, sub) => {
        implicit val timeout = akka.util.Timeout(500)

        val futures = ArrayBuffer[scala.concurrent.Future[Any]]()
        for (subject <- sub) {
          val future = subject.ref ? GetAvailableAction(request.processInstanceID)
          futures += future
        }
        // TODO non-blocking?
        //        val c = for (c <- futures.map(_.mapTo[Int])) yield c
        //        val x = Await.result(c, timeout.duration)

        val actions = ArrayBuffer[AvailableAction]()
        for (f <- futures) {
          actions += Await.result(f, timeout.duration).asInstanceOf[AvailableAction]
        }

        // results ready -> return
        sender ! AvailableActionsAnswer(request, actions.toArray)

        // actions collected -> stop this actor
        context.stop(self)
      }
    }
  }
}
