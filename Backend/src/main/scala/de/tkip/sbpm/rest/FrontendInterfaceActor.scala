package de.tkip.sbpm.rest

import akka.actor.Actor
import de.tkip.sbpm.rest.ProcessAttribute._
import akka.actor.Props
import spray.routing._
import spray.http._
import MediaTypes._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.event.Logging
import spray.json._

object Entity {
  val PROCESS = "process"
  val	EXECUTION = "executed"
  val	USER = "user"
  val	ROLE = "role"
  // TODO define more entities if you need them  
}
class FrontendInterfaceActor(val subjectProviderManagerActorRef: SubjectProviderManagerActorRef, 
    val persistenceActorRef: PersistenceActorRef)extends Actor with HttpService {

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
      	context.actorOf(Props(new ProcessInterfaceActor(subjectProviderManagerActorRef, persistenceActorRef))) ! requestContext
    } ~
    /**
     * redirect all calls beginning with "execution" to ProcessInterfaceActor
     * 
     * e.g. GET http://localhost:8080/process/8
     */
    pathPrefix(Entity.EXECUTION) { requestContext =>
      	context.actorOf(Props[ExecutionInterfaceActor]) ! requestContext
    } ~
    /**
     * redirect all calls beginning with "user" to UserInterfaceActor
     * 
     * e.g. GET http://localhost:8080/user/8
     */
    pathPrefix(Entity.USER) { requestContext =>
      	context.actorOf(Props[UserInterfaceActor]) ! requestContext
    }
    
  })
  
}