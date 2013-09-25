package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.actor.Props
import akka.actor.PoisonPill

import spray.routing.HttpService
import spray.routing.RequestContext
import scala.reflect.ClassTag

class FrontendInterfaceActor extends Actor with HttpService {

  def actorRefFactory = context

  def receive = runRoute({
    // we just have one route, but thats the way we handle this in sbpm
    // (every request creates a new actor)
    pathPrefix("subject") {
      handleWith[ProcessInstanceInterfaceActor]
    }
  })

  /**
   * Redirect the current request to the specified *InterfaceActor
   * without authentication.
   */
  private def handleWith[A <: Actor: ClassTag]: RequestContext => Unit = {
    requestContext =>
      var actor = context.actorOf(Props[A])
      actor ! requestContext
      // kill actor after handling the request
      actor ! PoisonPill
  }
}