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
import scala.util.{Success,Failure}

class ChangeInterfaceActor extends Actor with HttpService with DefaultLogging{
  
  import context.dispatcher
  implicit val timeout = Timeout(15 seconds)
  private lazy val processManagerActor = ActorLocator.processManagerActor
  private lazy val changeActor = ActorLocator.changeActor
  def actorRefFactory = context
  
  
  def receive = runRoute {
    get {
      // frontend request
      pathPrefix("") {
        parameter("since") { (time) => ctx =>
          log.debug(s"${getClass.getName} received polling request with timestemp: $time")
          (processManagerActor ? GetHistorySince(time.toLong)).mapTo[String].onComplete {
            case Success(history) => {
              (changeActor ? GetProcessChange(time.toLong)).mapTo[Option[ProcessRelatedChange]].onComplete {
                case Success(process) => {
                  (changeActor ? GetActionChange(time.toLong)).mapTo[Option[ActionRelatedChange]].onComplete {
                    case Success(action) => {
                      val result = ChangeRelatedData(process,action)
                      complete(ChangeRelatedData(process, action))
                    }
                    case Failure(e) => complete(e) 
                  }  
                  
                }
                case Failure(e) => complete(e)
              }
            }
            case Failure(e) => complete(e)
          }
     
        }
      }
    }
  }
}