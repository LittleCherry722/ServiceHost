package de.tkip.sbpm.application.history

import akka.actor.Actor
import de.tkip.sbpm
import de.tkip.sbpm.application.history._
import spray.routing.HttpService
import de.tkip.sbpm.logging.DefaultLogging
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.collection.mutable.Buffer
import scala.util.{Try, Success, Failure}

class HistoryChangeActor extends Actor with HttpService with DefaultLogging{
  
  import context.dispatcher
  
  private lazy val processManagerActor = sbpm.ActorLocator.processManagerActor

  def actorRefFactory = context
  implicit val timeout = Timeout(15 seconds)
  
  def receive = runRoute {
    get {
      // frontend request
      pathPrefix("") {
        parameter("since") { (time) => ctx =>
          log.debug(s"${getClass.getName} received authentication init get with timestemp: $time")
          (processManagerActor ? GetHistorySince(time.toLong)).mapTo[String].onComplete {
            case Success(entries) => ctx.complete(entries)
          }
     
        }
      }
    }
  }
}