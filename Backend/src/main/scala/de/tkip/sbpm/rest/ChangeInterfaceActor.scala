package de.tkip.sbpm.rest

import akka.actor.Actor
import de.tkip.sbpm._
import de.tkip.sbpm.application.history._
import spray.routing.HttpService
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._
import akka.util.Timeout
import scala.concurrent.duration._
import de.tkip.sbpm.application.change._
import spray.json._
import de.tkip.sbpm.model._
import akka.pattern.ask
import scala.util.{ Success, Failure }

class ChangeInterfaceActor extends AbstractInterfaceActor with DefaultLogging {

  import context.dispatcher
  implicit val timeout = Timeout(15 seconds)
  private lazy val processManagerActor = ActorLocator.processManagerActor
  private lazy val changeActor = ActorLocator.changeActor
  def actorRefFactory = context

  def routing = runRoute {
    get {
      // frontend request
      pathPrefix("") {
        parameter("since") { (time) =>
            complete {
              //          log.debug(s"${getClass.getName} received polling request with timestemp: $time")
              val future = 
                for {
                  history <- (processManagerActor ? GetHistorySince(time.toLong)).mapTo[Option[HistoryRelatedChange]]
                  process <- (changeActor ? GetProcessChange(time.toLong)).mapTo[Option[ProcessRelatedChange]]
                  action <- (changeActor ? GetActionChange(time.toLong)).mapTo[Option[ActionRelatedChange]]
                  processInstance <- (changeActor ? GetProcessInstanceChange(time.toLong)).mapTo[Option[ProcessInstanceRelatedChange]]
                  message <- (changeActor ? GetMessageChange(time.toLong)).mapTo[Option[MessageRelatedChange]]
                  result = ChangeRelatedData(process, processInstance, action, history, message)	  
                } yield result
                
                future.map(result => result)
            }
            }
      }
    }
  }
}