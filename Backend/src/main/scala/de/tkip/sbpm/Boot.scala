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

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import akka.actor.{ActorSystem, Props, actorRef2Scala}
import akka.util.Timeout
import akka.pattern._
import akka.io.IO

import ActorLocator._

import de.tkip.sbpm.application._
import de.tkip.sbpm.persistence.query.Schema
import de.tkip.sbpm.persistence.testdata.Entities
import de.tkip.sbpm.persistence.PersistenceActor
import de.tkip.sbpm.rest._
import de.tkip.sbpm.rest.auth._
import de.tkip.sbpm.rest.google.{GDriveActor, GCalendarActor}
import spray.can.Http

import de.tkip.sbpm.logging.LogPersistenceActor

object Boot extends App {

  implicit val system = ActorSystem("sbpm")
  val logging = system.log

  sys.addShutdownHook {
    logging.debug("Shutting down the system...")
    val stopFutures = Future.sequence(rootActors.map(gracefulStop(_, 5 seconds)))
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
    system.actorOf(Props[GDriveActor], googleDriveActorName),
    system.actorOf(Props[GCalendarActor], googleCalendarActorName),
    system.actorOf(Props[LogPersistenceActor], logPersistenceActorName)
  )


  // binding the frontendInterfaceActor to a HttpListener
  IO(Http) ! Http.Bind(frontendInterfaceActor, interface = "localhost", port = sys.env.getOrElse("SBPM_PORT", "8080").toInt)

  printf(sys.env.getOrElse("SBPM_PORT", "8080"))
  printf(sys.env.getOrElse("AKKA_PORT", "2552"))

  // db init code below
  implicit val timout = Timeout(30 seconds)
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
