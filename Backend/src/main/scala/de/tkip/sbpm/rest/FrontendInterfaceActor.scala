package de.tkip.sbpm.rest

import akka.actor.Actor

import akka.actor.Props
import spray.routing._
import spray.http._
import MediaTypes._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.event.Logging
import spray.json._
import de.tkip.sbpm.rest._

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
  
  // Unmarshalling of a JSON to a message of type Communication
  // Getting the message ready to be sent
  def read(js: JsValue) = {
    js match {
      case JsArray(JsString(prefix) :: JsString(information) :: Nil) => 
        new Message(prefix, information)
      case _ => println("Unexpected Message Type")
    }
  }
  
  // Marshalling of a message of type Communication to a JSON structure
  // After receiving the message, extracting the command prefix and the 
  // information
  def write(msg: Message) = {
    JsArray(JsString(msg.prefix), JsString(msg.information))
  }
}