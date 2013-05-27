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

package de.tkip.sbpm

import java.security.KeyStore
import java.security.SecureRandom
import ActorLocator._
import akka.actor.Props
import akka.actor.actorRef2Scala
import de.tkip.sbpm.application._
import de.tkip.sbpm.rest._
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import spray.can.server.SprayCanHttpServerApp
import akka.util.Timeout
import akka.pattern._
import scala.concurrent.duration._
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.actors.Logger
import scala.concurrent.Future
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.external.auth.GoogleAuthActor
import de.tkip.sbpm.persistence.query.Schema
import de.tkip.sbpm.persistence.testdata.Entities
import de.tkip.sbpm.rest.FrontendInterfaceActor
import de.tkip.sbpm.persistence.testdata.Entities
import de.tkip.sbpm.persistence.PersistenceActor
import de.tkip.sbpm.external.api.GoogleUserInformationActor
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.external.auth.GoogleAuthActor
import de.tkip.sbpm.external.api._
import de.tkip.sbpm.rest.auth._
import scala.concurrent.Await

object Boot extends App with SprayCanHttpServerApp {
  val logging = system.log

  sys.addShutdownHook {
    logging.debug("Shutting down the system...")
    val stopFutures = Future.sequence(rootActors.map(gracefulStop(_, 5 seconds)(system)))
    Await.result(stopFutures, 6 seconds)
    system.shutdown();
  }

  // for SSL support (if enabled in application.conf)
  implicit def sslContext: SSLContext = {
    val keyStoreResource = "/ssl-keystore.jks"
    val password = "sbpm1234"

    val keyStore = KeyStore.getInstance("jks")
    keyStore.load(getClass.getResourceAsStream(keyStoreResource), password.toCharArray)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }

  val frontendInterfaceActor = system.actorOf(Props[FrontendInterfaceActor], frontendInterfaceActorName);
  val subjectProviderManagerActor = system.actorOf(Props[SubjectProviderManagerActor], subjectProviderManagerActorName)
  val persistenceActor = system.actorOf(Props[PersistenceActor], persistenceActorName);

  // create and start our service actor
  val rootActors = List(persistenceActor,
    system.actorOf(Props[ContextResolverActor], contextResolverActorName),
    system.actorOf(Props[ProcessManagerActor], processManagerActorName),
    subjectProviderManagerActor,
    frontendInterfaceActor,
    system.actorOf(Props[SessionActor], sessionActorName),
    system.actorOf(Props[BasicAuthActor], basicAuthActorName),
    system.actorOf(Props[OAuth2Actor], oAuth2ActorName),
    system.actorOf(Props[UserPassAuthActor], userPassAuthActorName),
    system.actorOf(Props[GoogleAuthActor], googleAuthActorName),
    system.actorOf(Props[GoogleDriveActor], googleDriveActorName),
    system.actorOf(Props[GoogleUserInformationActor], googleUserInformationActorName))


  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(frontendInterfaceActor) ! Bind(interface = "localhost", port = sys.env.getOrElse("SBPM_PORT", "8080").toInt)

  // db init code below
  implicit val timout = Timeout(30 seconds)
  implicit val context = system
  implicit val executionContext = system.dispatcher

  // check startup actions defined in config
  val startupAction = system.settings.config.getString("sbpm.db.startupAction")
  val dropAction = startupAction matches "^recreate(-debug)?$"
  val createAction = startupAction matches "^(re)?create(-debug)?$"
  val debugAction = startupAction matches "^(re)?create-debug$"

  // execute all required db operations async and sequentially 
  var dbFuture = Future[Any]()

  val onFailure: PartialFunction[Throwable, Any] = {
    case e => logging.error(e, e.getMessage)
  }

  if (dropAction) {
    dbFuture = dbFuture flatMap { case _ => persistenceActor ? Schema.Drop }
    dbFuture.onFailure(onFailure)
  }
  if (createAction) {
    dbFuture = dbFuture flatMap { case _ => persistenceActor ? Schema.Create }
    dbFuture.onFailure(onFailure)
  }
  if (debugAction) {
    dbFuture = dbFuture flatMap { case _ => Entities.insert(persistenceActor) }
    dbFuture.onFailure(onFailure)
  }

  dbFuture.onFailure(onFailure)
}
