package de.tkip.sbpm

import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props
import de.tkip.sbpm.rest.FrontendInterfaceActor

object Boot extends App with SprayCanHttpServerApp {

  val frontendInterfaceActor = system.actorOf(Props[FrontendInterfaceActor], "FE-Actor")

  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(frontendInterfaceActor) ! Bind(interface = "localhost", port = 8080)

  println("Http server started...")
}
