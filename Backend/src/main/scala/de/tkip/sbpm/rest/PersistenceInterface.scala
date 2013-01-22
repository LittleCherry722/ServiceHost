package de.tkip.sbpm.rest
import spray.routing.directives.CompletionMagnet
import akka.actor.Actor
import de.tkip.sbpm.persistence.PersistenceAction
import akka.util.Timeout
import scala.concurrent.Await
import akka.pattern._
import scala.concurrent.duration._
import spray.json._
import de.tkip.sbpm.rest.JsonProtocol._
import spray.routing.directives.CompletionMagnet
import spray.routing.HttpService
import spray.http.HttpHeaders
import spray.http.StatusCodes

/**
 * Inheriting actors have simplified access to persistence actor.
 */
trait PersistenceInterface extends HttpService { self: Actor =>
  // reference to persistence actor
  protected val persistenceActor = context.actorFor("/user/PersistenceActor")
  protected implicit val timeout = Timeout(10 seconds)

  // is required by spray HttpService trait
  def actorRefFactory = context

  /**
   * Sends a message to the persistence actor and waits
   * for the result of type A.
   */
  protected def request[A](action: PersistenceAction): A = {
    val future = persistenceActor ? action
    Await.result(future, timeout.duration).asInstanceOf[A]
  }

  /**
   * Executes the action without waiting for a result.
   */
  protected def execute(action: PersistenceAction) = {
    persistenceActor ! action
  }

  /**
   * Completes the request with 201 Created status code and
   * Location to given location. 
   */
  protected def created(locationFormat: String, args: Any*) =
    respondWithHeader(HttpHeaders.Location(locationFormat.format(args : _*))) {
      _.complete(StatusCodes.Created)
    }
  
  /**
   * Completes the request with 202 Accepted status code.
   */
  protected def accepted() = complete(StatusCodes.Accepted)
  
  /**
   * Completes the request with 404 Not Found status code and
   * given error message. 
   */
  protected def notFound(msgFormat: String, args: Any*) = 
    complete(StatusCodes.NotFound, msgFormat.format(args : _*))
}