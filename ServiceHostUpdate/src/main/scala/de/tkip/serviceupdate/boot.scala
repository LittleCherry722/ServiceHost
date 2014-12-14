package de.tkip.serviceupdate

import akka.actor._
import akka.remote._
import de.tkip.servicehost.Messages._
import de.tkip.serviceupdate.Messages.UploadServiceToHost
import java.io.File

object boot extends App {
  def parseHostPort(arg: String): Option[(String, String)] = {
    arg.split(":") match {
      case Array(h, p) => Some(h.replaceAll("localhost", "127.0.0.1"), p)
      case _           => None
    }
  }

  println("started")
  if (args.length > 0) {
    /** starting actor system */
    val system = ActorSystem("serviceupdater")

    implicit val updateActor = system.actorOf(Props[ServiceUpdateActor], "serviceUpdateActor")

    if (args.length == 1) {
      parseHostPort(args(0)) match {
        case Some((host, port)) => updateActor ! UpdateRepository(host, port)
        case None               => println("target in wrong format. Expected: \"hostname:port\", given: " + args(0))
      }
    } else if (args.length == 4) {
      val serviceId = args(1)
      val className = if (!args(2).endsWith(".class")) args(2) + ".class" else args(2)
      val jsonName = args(3)
      parseHostPort(args(0)) match {
        case Some((host, port)) => updateActor ! UploadServiceToHost(host, port, serviceId, className, jsonName)
        case None               => println("target in wrong format. Expected: \"hostname:port\", given: " + args(0))
      }
    } else {
      println("Wrong argument count. Expected: 4, Given: " + args.length)
      println("Usage: hostname:port [serviceID serviceClass serviceJSON]")
      println("If [serviceID serviceClass serviceJSON] are defined the specified service will be uploaded to the targethost."
	      + " Otherwise only an UpdateRepository-Message is sent to the host")
    }
  } else {
	  println("No target defined.")
	  println("Usage: hostname:port [serviceID serviceClass serviceJSON]")
	  println("If [serviceID serviceClass serviceJSON] are defined the specified service will be uploaded to the targethost."
	      + " Otherwise only an UpdateRepository-Message is sent to the host")
  }
}
