package de.tkip.serviceupdate

import akka.actor.Actor
import akka.actor.ActorRef
import de.tkip.servicehost.Messages.UpdateRepository
import de.tkip.servicehost.Messages.UploadService
import java.io.FileInputStream
import java.io.File
import java.io.FilenameFilter
import scala.collection.mutable.Map
import de.tkip.serviceupdate.Messages.UploadServiceToHost

class ServiceUpdateActor extends Actor {

  //  val serviceHost = context.actorSelection("akka.tcp://sbpm@127.0.0.1:2553/user/service-actor-manager")

  def receive: Actor.Receive = {
    case msg: UpdateRepository => {
      println(self + " Got UpdateRepository " + sender)
      println("Sending to ServiceHost")
      val serviceHost: ActorRef = context.actorFor("akka.tcp://sbpm@" + msg.host + ":" + msg.port + "/user/subject-provider-manager")

      serviceHost ! UpdateRepository
      
      
//      /** terminating */
//      context.system.shutdown
      
    }
    case msg: UploadServiceToHost => {
       val serviceHost: ActorRef = context.actorFor("akka.tcp://sbpm@" + msg.host + ":" + msg.port + "/user/subject-provider-manager")
       sendSourceCode(serviceHost, msg.serviceId, new File(msg.serviceClass), new File(msg.serviceJSON))
    }
    case anything => println("sth else: " + anything)
  }

  def sendSourceCode(serviceHost: ActorRef, serviceId: String, serviceClassFile: File, json: File) {
    val dir = serviceClassFile.getParentFile()
    val serviceClassName = serviceClassFile.getName()
    println(dir.getAbsolutePath())
    val classes: Array[File] = dir.listFiles(new FilenameFilter() {
      override def accept(dir: File, filename: String): Boolean = {
        if (filename.startsWith(serviceClassName.substring(0, serviceClassName.lastIndexOf(".")) + "$") || filename.equals(serviceClassName))
          true
        else
          false
      }
    })
    var classesMap: Map[String, Array[Byte]] = Map()
    for (file <- classes) {
      classesMap(file.getName()) = loadFile(file)
    }

    val serviceJson: Array[Byte] = loadFile(json)
    serviceHost ! UploadService(serviceId, serviceClassName, classesMap, json.getName(), serviceJson)

//    /** terminating */
//    context.system.shutdown
     
  }

  def loadFile(classFile: File): Array[Byte] = {
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