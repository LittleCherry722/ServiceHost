package de.tkip.sbpm.application.miscellaneous

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.event.Logging
import akka.actor._
import akka.pattern.ask
import scala.concurrent.Future
import de.tkip.sbpm.application.subject.AvailableAction
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.SubjectActor
import de.tkip.sbpm.application.subject.GetAvailableAction

case class CollectAvailableActions(subjects: Iterable[SubjectRef],
                                   processInstanceID: ProcessInstanceID,
                                   generateAnswer: Array[AvailableAction] => Any)

/**
 * This class is responsible to collect the available actions of a set of subjects
 */
class SubjectActionsCollector extends Actor {

  val logger = Logging(context.system, this)

  def receive = {
    case CollectAvailableActions(subjects, processInstanceID, generateAnswer) => {
      implicit val timeout = akka.util.Timeout(3 seconds) // TODO how long the timeout?
      // TODO might check if some subjects has terminated

      // ask every subjects for the available action
      val futures: Array[Future[AvailableAction]] =
        for (subject <- subjects.filterNot(_.isTerminated).toArray)
          yield (subject ? GetAvailableAction(processInstanceID)).asInstanceOf[Future[AvailableAction]]

      // await all question results parallel
      val actions = for (future <- futures.par)
        yield Await.result(future, timeout.duration)

      // results ready -> generate answer -> return
      // TODO for the moment filter endstatetype, later think about a better idea
      sender ! generateAnswer(actions.toArray.filterNot(_.stateType == de.tkip.sbpm.model.StateType.EndStateType.toString()))

      // actions collected -> stop this actor
      context.stop(self)
    }
  }
}
