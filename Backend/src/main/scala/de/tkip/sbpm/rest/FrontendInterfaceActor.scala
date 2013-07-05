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

import java.io.File

import scala.reflect.ClassTag

import akka.actor.{Actor, Props, ActorRef, ActorSystem, PoisonPill}
import akka.event.Logging

import spray.routing._
import spray.http._
import spray.json._
import MediaTypes._

import de.tkip.sbpm.rest.auth.CookieAuthenticator
import de.tkip.sbpm.rest.auth.SessionDirectives._
import de.tkip.sbpm.rest.ProcessAttribute._
import de.tkip.sbpm.model.User
import de.tkip.sbpm.logging.DefaultLogging

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
  val DEBUG = "debug"

  // TODO define more entities if you need them
}

class FrontendInterfaceActor extends Actor with DefaultLogging with HttpService {

  def actorRefFactory = context

  // akka config prefix
  protected val configPath = "sbpm."

  // read string from akka config
  protected def configString(key: String) =
    context.system.settings.config.getString(configPath + key)

  // read bool from akka config
  protected def configFlag(key: String) =
    context.system.settings.config.getBoolean(configPath + key)

  private val frontendBaseUrl = configString("frontend.baseUrl")
  private val frontendIndexFile = configString("frontend.indexFile")
  private val frontendBaseDir = configString("frontend.baseDirectory")
  private val authenticationEnabled = configFlag("rest.authentication")

  implicit val rejectionHandler = RejectionHandler.fromPF {
    // on authorization required rejection -> provide user a set of
    // supported auth schemes in the WWW-Authenticate header
    // and delete invalid session cookies
    case auth.AuthenticationRejection(schemes, realm, sessionId) :: _ => {
      respondWithHeader(HttpHeaders.`WWW-Authenticate`(schemes.map(HttpChallenge(_, realm)))) {
        if (sessionId.isDefined) {
          session(sessionId.get)(context) { session =>
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

  def receive = runRoute({
    /**
     * redirect all calls beginning with "processinstance" (val EXECUTION) to ExecutionInterfaceActor
     *
     * e.g. GET http://localhost:8080/processinstance/8
     */
    pathPrefix(Entity.EXECUTION) {
      authenticateAndHandleWith[ExecutionInterfaceActor]
    } ~
      /**
       * redirect all calls beginning with "process" to ProcessInterfaceActor
       *
       * e.g. GET http://localhost:8080/process/8
       */
      pathPrefix(Entity.PROCESS) {
        authenticateAndHandleWith[ProcessInterfaceActor]
      } ~
      /**
       * forward all posts to /oauth2callback unauthenticated to GoogleAuthActor
       */
      pathPrefix(Entity.OAUTH2CALLBACK) {
          handleWith[GoogleResponseActor]
      } ~
      /**
       * forward all gets and posts to /googledrive unauthenticated to GoogleAuthActor
       */
      //TODO add authentication for google drive
      pathPrefix(Entity.GOOGLEDRIVE) {
          handleWith[GoogleDriveInterfaceActor]
      } ~
      pathPrefix(Entity.USER) {
        /**
         * redirect posts to /user/login to UserInterfaceActor
         * without authentication
         */
        (pathTest("login") & post) {
          handleWith[UserInterfaceActor]
        } ~
          /**
           * redirect all other calls beginning with "user" to UserInterfaceActor
           * after authentication
           * e.g. GET http://localhost:8080/user/8
           */
          authenticateAndHandleWith[UserInterfaceActor]
      } ~
      /**
       * redirect all calls beginning with "role" to RoleInterfaceActor
       *
       * e.g. GET http://localhost:8080/role/8
       */
      pathPrefix(Entity.ROLE) {
        authenticateAndHandleWith[RoleInterfaceActor]
      } ~
      /**
       * redirect all calls beginning with "group" to GroupInterfaceActor
       *
       * e.g. GET http://localhost:8080/group/8
       */
      pathPrefix(Entity.GROUP) {
        authenticateAndHandleWith[GroupInterfaceActor]
      } ~
      /**
       * redirect all calls beginning with "configuration" to ConfigurationInterfaceActor
       *
       * e.g. GET http://localhost:8080/configuration/sbpm.debug
       */
      pathPrefix(Entity.CONFIGURATION) {
        authenticateAndHandleWith[ConfigurationInterfaceActor]
      } ~
      /**
       * redirect all calls beginning with "debug" to DebugInterfaceActor
       *
       * e.g. GET http://localhost:8080/debug/sbpm.debug
       */
      pathPrefix(Entity.DEBUG) {
        authenticateAndHandleWith[DebugInterfaceActor]
      } ~
      get {
        /**
         * Serve static files under ../ProcessManagement/
         */
        // trailing / -> get index
        path(frontendBaseUrl + "/") {
          getFromFile(frontendBaseDir + frontendIndexFile)
        } ~
	      // no trailing slash -> redirect to index OR root folder -> redirect to frontendBaseUrl
	      (path(frontendBaseUrl) | path("")) {
	        redirect("/" + frontendBaseUrl + "/", StatusCodes.MovedPermanently)
	      } ~
	      // server other static content from dir
	      pathPrefix(frontendBaseUrl) {
	        getFromDirectory(frontendBaseDir)
	      }
      } ~
      pathPrefix(Entity.ISALIVE){
    	  get {
		      complete(StatusCodes.OK)
		      // TODO do some health check stuff and return StatusCodes.OK
    	  }
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

  /**
   * Redirect the current request to the specified *InterfaceActor
   * and checks if user is authenticated or tries to authenticate
   * him respectively.
   * Cancels the request, if user cannot be authenticated.
   */
  private def authenticateAndHandleWith[A <: Actor: ClassTag]: RequestContext => Unit = {
    if (authenticationEnabled) {
      // authenticate using session cookie or Authorization header
      authenticate(new CookieAuthenticator) { session =>
        // auth successful -> set session cookie
        setSessionCookie(session)(context) {
          handleWith[A]
        }
      }
    } else {
      handleWith[A]
    }
  }

}