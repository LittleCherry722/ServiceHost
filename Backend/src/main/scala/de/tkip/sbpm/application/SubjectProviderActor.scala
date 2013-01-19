package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await

class SubjectProviderActor(val userID: UserID, val processManagerRef: ProcessManagerRef) extends Actor {

  val processIDs = collection.mutable.Set[ProcessID]()

  private case class Subject(processID: ProcessID,
                             processInstanceID: ProcessInstanceID,
                             subjectID: SubjectID,
                             ref: SubjectRef)
  private var subjects = Set[Subject]()

  def receive = {
    case get: GetAvailableActions =>
      println("abc")
      // remove terminated subjects
      subjects = subjects.filter(!_.ref.isTerminated)
      // collect for the filtered list
      // TODO start collecting
      CollectAvailableActions(get, subjects.filter(_.processInstanceID == get.processInstanceID))

    case spc: SubjectProviderCreated =>
      processManagerRef.forward(spc)

    case pc: ProcessInstanceCreated =>
      processIDs += pc.processInstanceID
      context.parent.forward(pc)

    case message: AnswerAbleMessage =>
      // just forward all messages from the frontend which are not
      // required in this Actor
      processManagerRef.forward(message)

    case message: AnswerMessage[_] =>
      // send the Answermessages to the SubjectProviderManager

      // TODO forward oder tell?
      context.parent ! message

    case s =>
      println("SubjectProvider not yet implemented: " + s)
  }

  private case class CollectAvailableActions(g: GetAvailableActions, set: Set[Subject])
  private class AvailableActionsCollector extends Actor {
    def receive = {
      case CollectAvailableActions(g, s) =>
        println(s)

        implicit val timeout = akka.util.Timeout(500)
        val futures = ArrayBuffer[scala.concurrent.Future[Any]]()
        for (subject <- subjects) {
          // TOOD richtige message
          import akka.pattern.ask
          val future = subject.ref ? 1
          futures += future
        }
        // TODO non-blocking?
        //        val c = for (c <- futures.map(_.mapTo[Int])) yield c
        //        val x = Await.result(c, timeout.duration)

        val actions = ArrayBuffer[Any]()
        for (f <- futures) {
          actions += Await.result(f, timeout.duration)
        }
        val result = actions.toArray

        // results ready -> return
        sender ! AvailableActionsAnswer(g)

        // actions collected -> stop this actor
        context.stop(self)
    }
  }
}
