package de.tkip.serviceupdate

import akka.actor.Actor
import akka.actor.ActorRef
import de.tkip.servicehost.Messages.UpdateRepository
import de.tkip.servicehost.Messages.UploadService
import java.io.FileInputStream
import java.io.File
import java.io.FilenameFilter
import scala.collection.mutable.Map

class ServiceUpdateActor extends Actor {

  //  val serviceHost = context.actorSelection("akka.tcp://sbpm@127.0.0.1:2553/user/service-actor-manager")

  def receive: Actor.Receive = {
    //    case msg: UpdateRepository => 
    //      println(self + "Got Message: " + msg + "from sender: " + sender)
    case msg: UpdateRepository => {
      println(self + " Got UpdateRepository " + sender)
      println("Sending to ServiceHost")
      val serviceHost: ActorRef = context.actorFor("akka.tcp://sbpm@" + msg.host + ":" + msg.port + "/user/subject-provider-manager")

      serviceHost ! UpdateRepository

      sendSourceCode(serviceHost, "StaplesServiceActor.class", new File("src/main/resources/service_export_Stapler_service.json"))
    }
    case anything => println("sth else: " + anything)
  }

  def sendSourceCode(serviceHost: ActorRef, serviceClassName: String, json: File) {
    val dir = new File("src/main/resources/")
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
    serviceHost ! UploadService("Staples", serviceClassName, classesMap, json.getName(), serviceJson)

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