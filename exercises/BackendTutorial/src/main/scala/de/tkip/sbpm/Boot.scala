package de.tkip.sbpm


import akka.actor.{ ActorSystem, Props }
import akka.io.IO

import spray.can.Http

import de.tkip.sbpm.ActorLocator._
import de.tkip.sbpm.application.ProcessInstanceActor
import de.tkip.sbpm.rest.FrontendInterfaceActor
import de.tkip.sbpm.model.TestData._
import de.tkip.sbpm.model.TestData

object Boot extends App {

  implicit val system = ActorSystem("sbpmtutorial")

  val frontendInterfaceActor =
    system.actorOf(Props[FrontendInterfaceActor], frontendInterfaceActorName)
  val processInstanceActor =
    system.actorOf(Props(new ProcessInstanceActor(1)), processInstanceActorName)

  // create a new HttpServer using our handler tell it where to bind to
  IO(Http) ! Http.Bind(frontendInterfaceActor, interface = "localhost", port = 8080)

  println("Http server started...")
}
