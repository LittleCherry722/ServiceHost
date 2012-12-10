package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.actor.Props
import spray.routing._
import spray.http._
import MediaTypes._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.event.Logging

object Entity {
  val PROCESS = "process"
  val	EXECUTION = "execution"
  val	USER = "user"
  val	ROLE = "role"
  // TODO define more entities if you need them  
}
class FrontendInterfaceActor extends Actor with HttpService {

  val logger = Logging(context.system, this)
  
  override def preStart() {
    logger.debug("REST Api starts.")
  }
  
  def actorRefFactory = context

  def receive = runRoute({
    /**
     * redirect all calls beginning with "process" to ProcessInterfaceActor
     * 
     * e.g. GET http://localhost:8080/process/8
     */
    pathPrefix(Entity.PROCESS) { requestContext =>
      	context.actorOf(Props[ProcessInterfaceActor]) ! requestContext
    } ~
    /**
     * redirect all calls beginning with "execution" to ProcessInterfaceActor
     * 
     * e.g. GET http://localhost:8080/process/8
     */
    pathPrefix(Entity.EXECUTION) { requestContext =>
      	context.actorOf(Props[ExecutionInterfaceActor]) ! requestContext
    }
    // TODO add more entities here and redirect the requests to the specific actors
  })
}