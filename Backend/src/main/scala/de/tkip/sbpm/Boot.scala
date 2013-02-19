package de.tkip.sbpm

import java.security.KeyStore
import java.security.SecureRandom

import ActorLocator._
import akka.actor.Props
import akka.actor.actorRef2Scala
import de.tkip.sbpm.application._
import de.tkip.sbpm.rest._
import de.tkip.sbpm.persistence._
import de.tkip.sbpm.rest.auth._
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import spray.can.server.SprayCanHttpServerApp

object Boot extends App with SprayCanHttpServerApp {

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
  
  // create and start our service actor
  val persistenceActor = system.actorOf(Props[PersistenceActor], persistenceActorName)
  val contextResolver = system.actorOf(Props[ContextResolverActor], contextResolverActorName)
  val processManagerActor = system.actorOf(Props[ProcessManagerActor], processManagerActorName)
  val subjectProviderManagerActor = system.actorOf(Props[SubjectProviderManagerActor], subjectProviderManagerActorName)
  val frontendInterfaceActor = system.actorOf(Props[FrontendInterfaceActor], frontendInterfaceActorName)
  val sessionActor = system.actorOf(Props[SessionActor], sessionActorName)
  val basicAuthActor = system.actorOf(Props[BasicAuthActor], basicAuthActorName)
  val oAuth2Actor = system.actorOf(Props[OAuth2Actor], oAuth2ActorName)
  val userPassAuthActor = system.actorOf(Props[UserPassAuthActor], userPassAuthActorName)

  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(frontendInterfaceActor) ! Bind(interface = "localhost", port = 8080)

  persistenceActor ! InitDatabase

  // TODO create a processinstance for testreason: history, actions, graph etc...
  subjectProviderManagerActor ! de.tkip.sbpm.application.miscellaneous.CreateProcessInstance(1, 1)
}
