package de.tkip.serviceupdate

import akka.actor._
import akka.remote._
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.ServiceHostActor

object boot extends App {
  println("started")
  if (args.length > 0) {
    val host = args(0).split(":")(0)
    val port = args(0).split(":")(1)
    println("host: " + host)
    println("port: " + port)
  
  }
  
  /** starting actor system */
  val system = ActorSystem("serviceupdater")
  
  implicit val updateActor = system.actorOf(Props[ServiceUpdateActor], "serviceUpdateActor")
  
//  val serviceHostURI = AddressFromURIString("akka.tcp://sbpm@localhost:2553")
//  val serviceHost = system.actorOf(Props[ServiceHostActor].withDeploy(Deploy(scope = RemoteScope(serviceHostURI))))
//  val serviceHost = system.actorFor("akka.tcp://sbpm@localhost:2553/user/subject-provider-manager")
  val serviceHost = system.actorSelection("akka.tcp://sbpm@localhost:2553/user/subject-provider-manager")
  
  serviceHost ! UpdateRepository
}
