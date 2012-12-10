package de.tkip.sbpm

import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props
import akka.actor.ActorSystem

object Boot extends App with SprayCanHttpServerApp {

  // create and start our service actor
  val service = system.actorOf(Props[de.tkip.sbpm.rest.FrontendInterfaceActor], "RESTAPI")
  
  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(service) ! Bind(interface = "localhost", port = 8080)

}