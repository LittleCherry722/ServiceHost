package de.tkip.serviceupdate

import akka.actor.Actor
import akka.actor.ActorRef
import de.tkip.servicehost.Messages.UpdateRepository
import de.tkip.servicehost.Messages.UploadService
import java.io.FileInputStream
import java.io.File

class ServiceUpdateActor extends Actor{
  
//  val serviceHost = context.actorSelection("akka.tcp://sbpm@127.0.0.1:2553/user/service-actor-manager")
   
  def receive:Actor.Receive = {
//    case msg: UpdateRepository => 
//      println(self + "Got Message: " + msg + "from sender: " + sender)
    case msg: UpdateRepository => {
      println(self + " Got UpdateRepository " + sender)
      println("Sending to ServiceHost")
      val serviceHost: ActorRef = context.actorFor("akka.tcp://sbpm@"+ msg.host + ":" + msg.port + "/user/subject-provider-manager")
  
      serviceHost ! UpdateRepository
      
      val serviceClass: Array[Byte] = loadFile("src/main/resources/StaplesServiceActor.class")
      val serviceJson: Array[Byte] = loadFile("src/main/resources/service_export_Stapler_service.json")
      serviceHost ! UploadService("Staples", "StaplesActorService.class", serviceClass, "service_export_Stapler_service.json", serviceJson)
      
    }
    case anything => println("sth else: " + anything)
  }

  def loadFile(filePath: String): Array[Byte] = {
    val classFile = new File(filePath)
    println(classFile.getAbsolutePath())
    val fis = new FileInputStream(classFile) 
    val file: Array[Byte] = new Array[Byte](classFile.length().toInt)
    fis.read(file)
    fis.close
    file
  }
  def sendUpdateTo(ref: ActorRef) {
    ref ! UpdateRepository
  }
  
}