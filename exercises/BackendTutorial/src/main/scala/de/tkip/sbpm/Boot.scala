package de.tkip.sbpm

import akka.actor.Props

import spray.can.server.SprayCanHttpServerApp

import de.tkip.sbpm.ActorLocator._
import de.tkip.sbpm.application.ProcessInstanceActor
import de.tkip.sbpm.rest.FrontendInterfaceActor
import de.tkip.sbpm.model.TestData._

object Boot extends App with SprayCanHttpServerApp {

  val frontendInterfaceActor =
    system.actorOf(Props[FrontendInterfaceActor], frontendInterfaceActorName)
  val processInstanceActor =
    system.actorOf(Props(new ProcessInstanceActor(test1)), processInstanceActorName)

  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(frontendInterfaceActor) !
    Bind(interface = "localhost", port = 8080)

  println("Http server started...")
}
