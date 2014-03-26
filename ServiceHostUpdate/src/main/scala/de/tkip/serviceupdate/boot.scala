package de.tkip.serviceupdate

import akka.actor._
import akka.remote._
import de.tkip.servicehost.Messages._
import de.tkip.serviceupdate.Messages.UploadServiceToHost
import java.io.File

object boot extends App {
  println("started")
  if (args.length > 0) {
    
    var host: String = null
    var port: String = null
    var serviceId: String = null
    var classPath: String = null
    var className: String = null
    var jsonName: String = null
    
    /** starting actor system */
    val system = ActorSystem("serviceupdater")
  
    implicit val updateActor = system.actorOf(Props[ServiceUpdateActor], "serviceUpdateActor")
  
    if (args.length == 1) {
    	if(args(0).contains(":")) {
    		host = args(0).split(":")(0).replaceAll("localhost", "127.0.0.1")
    		port = args(0).split(":")(1)
    		updateActor ! UpdateRepository(host, port)
    	} else {
    	  println("target in wrong format. Expected: \"hostname:port\", given: " + args(0))
    	} 
    } else if (args.length == 4) {
    
      host = args(0).split(":")(0).replaceAll("localhost", "127.0.0.1")
      port = args(0).split(":")(1)
      serviceId = args(1)
      className = args(2)
      if (!className.endsWith(".class")) className = className + ".class" 
      jsonName = args(3)      
      
      updateActor ! UploadServiceToHost(host, port, serviceId, className, jsonName)
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
