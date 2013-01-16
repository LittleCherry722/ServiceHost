package de.tkip.sbpm.rest

import akka.actor.Actor
import de.tkip.sbpm.persistence.PersistenceAction
import akka.util.Timeout
import scala.concurrent.Await
import akka.pattern._
import scala.concurrent.duration._
import spray.json._
import de.tkip.sbpm.rest.JsonProtocol._

trait PersistenceInterface { self: Actor =>
	protected val persistenceActor = context.actorFor("/user/PersistenceActor")
	protected val STATUS_OK = "ok";
	protected val STATUS_NOT_FOUND = "not_found";
	protected implicit val timeout = Timeout(10 seconds)
	def actorRefFactory = context
	
	protected def request[A](action: PersistenceAction): A = {
	  val future = persistenceActor ? action
	  Await.result(future, timeout.duration).asInstanceOf[A]
	}
	
	protected def execute(action: PersistenceAction) = {
	  persistenceActor ! action
	}
}