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
import spray.http.StatusCode
import spray.routing.StandardRoute
import spray.httpx.marshalling.Marshaller
import spray.util.LoggingContext
import spray.routing.ExceptionHandler
import de.tkip.sbpm.ActorLocator
import akka.actor.PoisonPill
import akka.actor.ActorLogging
import de.tkip.sbpm.persistence.EntityNotFoundException

/**
 * Inheriting actors have simplified access to persistence actor.
 */
trait PersistenceInterface extends HttpService with ActorLogging { self: Actor =>
  // reference to persistence actor
  protected val persistenceActor = ActorLocator.persistenceActor
  protected implicit val timeout = Timeout(10 seconds)

  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }

  // is required by spray HttpService trait
  def actorRefFactory = context

  // spray exception handler: turns exceptions that occur while
  // processing the request into internal server error response
  // with exception message as payload (also logs the exception)
  implicit def exceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler.fromPF {
      case e: EntityNotFoundException => ctx =>
        ctx.complete(StatusCodes.NotFound, e.getMessage)
      case e: Exception => ctx =>
        log.error(e, e.getMessage)
        ctx.complete(StatusCodes.InternalServerError, e.getMessage)
    }

  /**
   * Completes the request and stops the actor afterwards via PoisonPill.
   */
  def complete(magnet: CompletionMagnet): StandardRoute = {
    self.self ! PoisonPill
    super.complete(magnet)
  }

  /**
   * Sends a message to the persistence actor and waits
   * for the result of type A.
   */
  protected def request[A](action: PersistenceAction): A = {
    val future = persistenceActor ? action
    Await.result(future, timeout.duration).asInstanceOf[A]
  }

  /**
   * Completes the request with the result of a query to the
   * persistence actor. Responses with code 404 and given
   * message if result from persistence actor is None.
   */
  protected def completeWithQuery[A](action: PersistenceAction, notFoundMsgFormat: String, notFoundMsgArgs: Any*)(implicit marshaller: Marshaller[A]) = {
    val res = request[Option[A]](action)
    if (res.isDefined)
      complete(res.get)
    else
      notFound(notFoundMsgFormat, notFoundMsgArgs: _*)
  }

  /**
   * Completes the request with the result of a query to the
   * persistence actor.
   */
  protected def completeWithQuery[A](action: PersistenceAction)(implicit marshaller: Marshaller[A]) = {
    complete(request[A](action))
  }

  /**
   * Completes the request with the result of a save operation
   * sent to the persistence actor. Responses with code 201 if
   * new entity was created otherwise 200 (entity as payload).
   * Location path is used as base for created resource in Location header.
   * idSetter function is used to inject generated id into entity.
   */
  protected def completeWithSave[A, B](action: PersistenceAction, entity: A, locationPath: String, idSetter: (A, B) => A = (a: A, b: B) => a)(implicit marshaller: Marshaller[A]) = {
    val id = request[Option[B]](action)
    if (id.isDefined) {
      val newEntity = idSetter(entity, id.get)
      created(newEntity, locationPath, id.get)
    } else {
      complete(entity)
    }
  }

  /**
   * Completes the request with the result of a delete operation
   * sent to the persistence actor. Responses with code 404 and given
   * message if not entity was deleted otherwise 204 (No Content).
   */
  protected def completeWithDelete(action: PersistenceAction, notFoundMsgFormat: String, notFoundMsgArgs: Any*) = {
    val res = request[Int](action)
    if (res == 0)
      notFound(notFoundMsgFormat, notFoundMsgArgs: _*)
    else
      noContent()
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
  protected def created[A](entity: A, locationFormat: String, args: Any*)(implicit marshaller: Marshaller[A]) =
    respondWithHeader(HttpHeaders.Location(locationFormat.format(args: _*))) {
      _.complete(StatusCodes.Created, entity)
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
    complete(StatusCodes.NotFound, msgFormat.format(args: _*))

  /**
   * Completes the request with 500 Internal Server Error
   * status code and the message from the exception.
   */
  protected def serverError(ex: Exception): StandardRoute =
    serverError(ex.getMessage)

  /**
   * Completes the request with 500 Internal Server Error
   * status code and the given message.
   */
  protected def serverError(msg: String, args: Any*): StandardRoute =
    complete(StatusCodes.InternalServerError, msg.format(args: _*))

  /**
   * Completes the request with 204 No Content status code.
   */
  protected def noContent() = complete(StatusCodes.NoContent)

  /**
   * Generate the url path for a created entity.
   * @param name entity name e.g. "group"
   * @param format string format for primary key e.g. "%d"
   */
  protected def pathForEntity(name: String, format: String) =
    "/%s/%s".format(name, format)

}