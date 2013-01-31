package de.tkip.sbpm

import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props
import akka.actor.ActorSystem
import ActorLocator._
import de.tkip.sbpm.rest.FrontendInterfaceActor
import de.tkip.sbpm.application.SubjectProviderManagerActor
import de.tkip.sbpm.application.ProcessManagerActor
import de.tkip.sbpm.persistence.PersistenceActor
import de.tkip.sbpm.persistence.InitDatabase
import de.tkip.sbpm.application.ContextResolverActor

object Boot extends App with SprayCanHttpServerApp {

  // create and start our service actor
  val persistenceActor = system.actorOf(Props[PersistenceActor], persistenceActorName)
  val contextResolver = system.actorOf(Props[ContextResolverActor], contextResolverActorName)
  val processManagerActor = system.actorOf(Props[ProcessManagerActor], processManagerActorName)
  val subjectProviderManagerActor = system.actorOf(Props[SubjectProviderManagerActor], subjectProviderManagerActorName)
  val frontendInterfaceActor = system.actorOf(Props[FrontendInterfaceActor], frontendInterfaceActorName)

  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(frontendInterfaceActor) ! Bind(interface = "localhost", port = 8080)

  persistenceActor ! InitDatabase
}