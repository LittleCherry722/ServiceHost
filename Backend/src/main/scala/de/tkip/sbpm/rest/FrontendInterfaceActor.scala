/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.tkip.sbpm.rest

import akka.actor.{ ActorRef, Actor, Props }
import spray.routing._
import spray.http._
import spray.http.Uri._
import spray.client.pipelining._
import de.tkip.sbpm.rest.auth.CookieAuthenticator
import de.tkip.sbpm.rest.auth.SessionDirectives._
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.logging.LoggingResponseActor
import spray.json._
import spray.httpx.SprayJsonSupport._
import de.tkip.sbpm.bir._
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.rest._
import scala.concurrent.Future
import DefaultJsonProtocol._
import akka.event.Logging

object Entity {
  val PROCESS = "process"
  val EXECUTION = "processinstance"
  val TESTEXECUTION = "testexecuted"
  val USER = "user"
  val ROLE = "role"
  val GROUP = "group"
  val CONFIGURATION = "configuration"
  val OAUTH2CALLBACK = "oauth2callback"
  val ISALIVE = "isalive"
  val GOOGLEDRIVE = "googledrive"
  val LOGGING = "logging"
  val DEBUG = "debug"
  val REPOSITORY = "repo"
  val MESSAGE = "message"

  // TODO define more entities if you need them
}

class FrontendInterfaceActor extends Actor with DefaultLogging with HttpService {

  def actorRefFactory = context

  // execution context e.g. for authentication
  import context.dispatcher

  // akka config prefix
  protected val configPath = "sbpm."

  // read string from akka config
  protected def configString(key: String) =
    context.system.settings.config.getString(configPath + key)

  // read bool from akka config
  protected def configFlag(key: String) =
    context.system.settings.config.getBoolean(configPath + key)

  private val traceLogger = Logging(context.system, this)

  private val frontendBaseUrl = configString("frontend.baseUrl")
  private val frontendIndexFile = configString("frontend.indexFile")
  private val frontendBaseDir = configString("frontend.baseDirectory")
  private val authenticationEnabled = configFlag("rest.authentication")

  implicit val rejectionHandler = RejectionHandler {
    // on authorization required rejection -> provide user a set of
    // supported auth schemes in the WWW-Authenticate header
    // and delete invalid session cookies
    case auth.AuthenticationRejection(schemes, realm, sessionId) :: _ => {
      respondWithHeader(HttpHeaders.`WWW-Authenticate`(schemes.map(HttpChallenge(_, realm)))) {
        if (sessionId.isDefined) {
          session(sessionId.get)(context) {
            session =>
              setSessionCookie(session)(context) {
                complete(StatusCodes.Unauthorized)
              }
          }
        } else {
          deleteSession(actorRefFactory) {
            complete(StatusCodes.Unauthorized)
          }
        }
      }
    }
  }

  private val executionInterfaceActor = context.actorOf(Props[ExecutionInterfaceActor], "execution-interface")
  private val processInterfaceActor = context.actorOf(Props[ProcessInterfaceActor], "process-interface")
  private val gResponsActor = context.actorOf(Props[GResponseActor], "gresponse")
  private val logResponseActor = context.actorOf(Props[LoggingResponseActor], "logresponse")
  private val userInterfaceActor = context.actorOf(Props[UserInterfaceActor], "user-interface")
  private val roleInterfaceActor = context.actorOf(Props[RoleInterfaceActor], "role-interface")
  private val groupInterfaceActor = context.actorOf(Props[GroupInterfaceActor], "group-interface")
  private val configurationInterfaceActor = context.actorOf(Props[ConfigurationInterfaceActor], "configuration-interface")
  private val debugInterfaceActor = context.actorOf(Props[DebugInterfaceActor], "debug-interface")
  private val gbirInterfaceActor = context.actorOf(Props[GoogleBIRInterfaceActor], "gbir-interface")
  private val changeInterfaceActor = context.actorOf(Props[ChangeInterfaceActor], "change-interface")
  private val messageInterfaceActor = context.actorOf(Props[MessageInterfaceActor], "message-interface")
  private val repositoryInterfaceActor = context.actorOf(Props[RepositoryInterfaceActor], "repository-interface")

  private val receiver = runRoute({
    pathPrefix("BIR") {
      delegateTo(gbirInterfaceActor)
      //      post {
      //      formFields("content") { content => ctx =>
      //          println("content is: "+content)
      //      }
      //      }
    } ~
      pathPrefix("changes") {
        delegateTo(changeInterfaceActor)
      } ~
      /**
       * redirect all calls beginning with "processinstance" (val EXECUTION) to ExecutionInterfaceActor
       *
       * e.g. GET http://localhost:8080/processinstance/8
       */
      pathPrefix(Entity.EXECUTION) {
        authenticated {
          delegateTo(executionInterfaceActor)
        }
      } ~
      /**
       * redirect all calls beginning with "process" to ProcessInterfaceActor
       *
       * e.g. GET http://localhost:8080/process/8
       */
      pathPrefix(Entity.PROCESS) {
        authenticated {
          delegateTo(processInterfaceActor)
        }
      } ~
      pathPrefix(Entity.MESSAGE) {
        authenticated {
          delegateTo(messageInterfaceActor)
        }
      } ~
      /**
       * forward all posts to /oauth2callback unauthenticated to GoogleAuthActor
       */
      pathPrefix(Entity.OAUTH2CALLBACK) {
        delegateTo(gResponsActor)
      } ~
      /**
       * forward all gets and posts to /googledrive unauthenticated to GoogleAuthActor
       */
      //TODO add authentication for google drive
      pathPrefix(Entity.GOOGLEDRIVE) {
        delegateTo(gResponsActor)
      } ~
      pathPrefix(Entity.LOGGING) {
        delegateTo(logResponseActor)
      } ~
      pathPrefix(Entity.USER) {
        /**
         * redirect posts to /user/login to UserInterfaceActor
         * without authentication
         */
        (pathSuffixTest("login") & post) {
          delegateTo(userInterfaceActor)
        } ~
          /**
           * redirect all other calls beginning with "user" to UserInterfaceActor
           * after authentication
           * e.g. GET http://localhost:8080/user/8
           */
          authenticated {
            delegateTo(userInterfaceActor)
          }
      } ~
      /**
       * redirect all calls beginning with "role" to RoleInterfaceActor
       *
       * e.g. GET http://localhost:8080/role/8
       */
      pathPrefix(Entity.ROLE) {
        authenticated {
          delegateTo(roleInterfaceActor)
        }
      } ~
      /**
       * redirect all calls beginning with "group" to GroupInterfaceActor
       *
       * e.g. GET http://localhost:8080/group/8
       */
      pathPrefix(Entity.GROUP) {
        authenticated {
          delegateTo(groupInterfaceActor)
        }
      } ~
      /**
       * redirect all calls beginning with "configuration" to ConfigurationInterfaceActor
       *
       * e.g. GET http://localhost:8080/configuration/sbpm.debug
       */
      pathPrefix(Entity.CONFIGURATION) {
        authenticated {
          delegateTo(configurationInterfaceActor)
        }
      } ~
      /**
       * redirect all calls beginning with "debug" to DebugInterfaceActor
       *
       * e.g. GET http://localhost:8080/debug/sbpm.debug
       */
      pathPrefix(Entity.DEBUG) {
        authenticated {
          delegateTo(debugInterfaceActor)
        }
      } ~
      pathPrefix(Entity.REPOSITORY) {
        authenticated {
          delegateTo(repositoryInterfaceActor)
        }
      } ~
      pathPrefix(Entity.ISALIVE) {
        get {
          complete(StatusCodes.OK)
          // TODO do some health check stuff and return StatusCodes.OK
        }
      } ~
      get {
        serveStaticFiles
      }
  })

  def logReceive:PartialFunction[Any, Any] = {
    case request: spray.http.HttpRequest => {
      val path = request.uri.path
      if(!path.startsWith(Path.SingleSlash + frontendBaseUrl)){
        traceLogger.debug("TRACE: =========================================================================")
        traceLogger.debug("TRACE: request " + request.method + ": " + path)
        traceLogger.debug("TRACE: -------------------------------------------------------------------------")
      }
      request
    }
    case something => {
      something
    }
  }

  def receive = logReceive andThen receiver

  def serveStaticFiles: Route = {
    // root folder -> redirect to frontendBaseUrl
    path("") {
      redirect("/" + frontendBaseUrl + "/", StatusCodes.MovedPermanently)
    } ~
      // get index
      path(frontendBaseUrl) {
        getFromFile(frontendBaseDir + frontendIndexFile)
      } ~
      // server other static content from dir
      pathPrefix(frontendBaseUrl) {
        getFromDirectory(frontendBaseDir)
      }
  }

  /**
   * Delegates the current request to the specified *InterfaceActor
   * without authentication.
   */
  private def delegateTo(actor: ActorRef): Route = {
    traceLogger.debug("TRACE: from " + this.self + " to " + actor +" RequestContext")
    requestContext => actor ! requestContext
  }

  /**
   * Checks if user is authenticated or tries to authenticate
   * him respectively. Cancels the request, if user cannot be authenticated.
   */
  private def authenticated(op: => Route) = {
    if (authenticationEnabled) {
      // authenticate using session cookie or Authorization header
      authenticate(new CookieAuthenticator) {
        session =>
          // auth successful -> set session cookie
          setSessionCookie(session)(context) {
            op
          }
      }
    } else {
      op
    }
  }
}
