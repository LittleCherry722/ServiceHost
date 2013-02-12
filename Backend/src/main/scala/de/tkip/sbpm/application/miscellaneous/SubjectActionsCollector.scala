package de.tkip.sbpm.application.miscellaneous

import akka.actor._
import akka.pattern.ask
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import de.tkip.sbpm.application.subject.AvailableAction
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.SubjectActor
import de.tkip.sbpm.application.subject.GetAvailableAction
import akka.event.Logging

case class CollectAvailableActions(subjects: Set[SubjectRef],
                                   processInstanceID: ProcessInstanceID,
                                   generateAnswer: Array[AvailableAction] => Any)

/**
 * This class is responsible to collect the available actions of a set of subjects
 */
class SubjectActionsCollector extends Actor {

  val logger = Logging(context.system, this)

  def receive = {
    case CollectAvailableActions(subjects, processInstanceID, generateAnswer) => {
      implicit val timeout = akka.util.Timeout(5000)

      val futures = ArrayBuffer[scala.concurrent.Future[Any]]()
      for (subject <- subjects) {
        val future = subject ? GetAvailableAction(processInstanceID)
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
            logger.error(f + " timed out")
          }
        }
      }

      // results ready -> generate answer -> return
      sender ! generateAnswer(actions.toArray)

      // actions collected -> stop this actor
      context.stop(self)
    }
  }
}
