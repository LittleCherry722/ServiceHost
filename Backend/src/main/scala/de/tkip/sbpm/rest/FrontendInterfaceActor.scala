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
import java.io.File

object Entity {
  val PROCESS = "process"
  val EXECUTION = "processinstance"
  val TESTEXECUTION = "testexecuted"
  val USER = "user"
  val ROLE = "role"
  val GROUP = "group"
  val CONFIGURATION = "configuration"

  // TODO define more entities if you need them  
}
class FrontendInterfaceActor extends Actor with HttpService {
  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    logger.debug(getClass.getName + " stopped.")
  }

  def actorRefFactory = context

  def receive = runRoute({
    /**
     * redirect all calls beginning with "execution" to ExecutionInterfaceActor
     *
     * e.g. GET http://localhost:8080/execution/8
     */
    pathPrefix(Entity.EXECUTION) { requestContext =>
      context.actorOf(Props[ExecutionInterfaceActor]) ! requestContext
    } ~
      /**
       * redirect all calls beginning with "process" to ProcessInterfaceActor
       *
       * e.g. GET http://localhost:8080/process/8
       */
      pathPrefix(Entity.PROCESS) { requestContext =>
        context.actorOf(Props[ProcessInterfaceActor]) ! requestContext
      } ~
      /**
       * redirect all calls beginning with "testexecution" to TestExecutionInterfaceActor
       *
       * e.g. GET http://localhost:8080/testexecution/8
       */
      pathPrefix(Entity.TESTEXECUTION) { requestContext =>
        context.actorOf(Props[TestExecutionInterfaceActor]) ! requestContext
      } 
      /**
       * redirect all calls beginning with "user" to UserInterfaceActor
       *
       * e.g. GET http://localhost:8080/user/8
       */
      pathPrefix(Entity.USER) { requestContext =>
        context.actorOf(Props[UserInterfaceActor]) ! requestContext
      } ~
      /**
       * redirect all calls beginning with "role" to RoleInterfaceActor
       *
       * e.g. GET http://localhost:8080/role/8
       */
      pathPrefix(Entity.ROLE) { requestContext =>
        context.actorOf(Props[RoleInterfaceActor]) ! requestContext
      } ~
      /**
       * redirect all calls beginning with "group" to GroupInterfaceActor
       *
       * e.g. GET http://localhost:8080/group/8
       */
      pathPrefix(Entity.GROUP) { requestContext =>
        context.actorOf(Props[GroupInterfaceActor]) ! requestContext
      } ~
      /**
       * redirect all calls beginning with "configuration" to ConfigurationInterfaceActor
       *
       * e.g. GET http://localhost:8080/configuration/sbpm.debug
       */
      pathPrefix(Entity.CONFIGURATION) { requestContext =>
        context.actorOf(Props[ConfigurationInterfaceActor]) ! requestContext
      } ~
      /**
       * redirect /sbpm/ to the index.html in ../ProcessMangement/
       */
      path("sbpm/") {
        get {
        	getFromFile("../ProcessManagement/index.html")
        }
      } ~
      /**
       * Serve static files under ../ProcessManagement/
       */
      pathPrefix("sbpm") {
        get {
        	getFromDirectory("../ProcessManagement/")
        }
      } ~
      /**
       * Catch all
       */
      path(PathElement) { requestedEntity =>
        logger.debug("catchall")
      	complete(StatusCodes.NotFound, "Please choose an valid endpoint. (Requested="+requestedEntity+")")
      }
  })
}