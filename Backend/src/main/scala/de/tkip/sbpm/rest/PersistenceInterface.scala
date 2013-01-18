package de.tkip.sbpm.rest

import akka.actor.Actor
import de.tkip.sbpm.persistence.PersistenceAction
import akka.util.Timeout
import scala.concurrent.Await
import akka.pattern._
import scala.concurrent.duration._
import spray.json._
import de.tkip.sbpm.rest.JsonProtocol._

/**
 * Inheriting actors have simplified access to persistence actor.
 */
trait PersistenceInterface { self: Actor =>
  	// reference to persistence actor
	protected val persistenceActor = context.actorFor("/user/PersistenceActor")
	// http status message for "successfult"
	protected val STATUS_OK = "ok";
	// http status message for "entity not found"
	protected val STATUS_NOT_FOUND = "not_found";
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
}