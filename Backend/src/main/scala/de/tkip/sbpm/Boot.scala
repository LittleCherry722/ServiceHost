package de.tkip.sbpm

import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props
import akka.actor.ActorSystem

object Boot extends App with SprayCanHttpServerApp {

  // create and start our service actor
  val persistenceActor = system.actorOf(Props[de.tkip.sbpm.persistence.PersistenceActor], "PersitenceActor")
  println(persistenceActor)
  val processManager = system.actorOf(Props(new de.tkip.sbpm.application.ProcessManagerActor("ProcessManager")), "ProcessManager")
  val subjectProviderManager = system.actorOf(Props(new de.tkip.sbpm.application.SubjectProviderManagerActor(processManager)), "SubjectProviderManager")
  val service = system.actorOf(Props(new de.tkip.sbpm.rest.FrontendInterfaceActor(subjectProviderManager, persistenceActor)), "RESTAPI")
  
  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(service) ! Bind(interface = "localhost", port = 8080)

}