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
import de.tkip.sbpm.external.api.GoogleDriveActor
import de.tkip.sbpm.external.api.GoogleUserInformationActor


object Boot extends App with SprayCanHttpServerApp {
  val logging = system.log

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
  val googleAuthActor = system.actorOf(Props[GoogleAuthActor], googleAuthActorName)
  val googleDriveActor = system.actorOf(Props[GoogleDriveActor], googleDriveActorName)
  val googleUserInformationActor = system.actorOf(Props[GoogleUserInformationActor], googleUserInformationActorName)

  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(frontendInterfaceActor) ! Bind(interface = "localhost", port = 8080)

  // db init code below
  implicit val timout = Timeout(30 seconds)
  implicit val context = system
  implicit val executionContext = system.dispatcher

  // check startup actions defined in config
  val startupAction = system.settings.config.getString("sbpm.db.startupAction")
  val dropAction = startupAction matches "^recreate(-debug)?$"
  val createAction = startupAction matches "^(re)?create(-debug)?$"
  val debugAction = startupAction matches "^(re)?create-debug$"

  var dbFuture = Future()
  if (dropAction)
    dbFuture = dbFuture map { case _ => persistenceActor ? DropDatabase }
  if (createAction)
    dbFuture = dbFuture map { case _ => persistenceActor ? InitDatabase }
  if (debugAction)
    dbFuture = dbFuture map { case _ => TestData.insert(persistenceActor) }

  dbFuture.onFailure {
    case e => logging.error(e, e.getMessage)
  }
}
