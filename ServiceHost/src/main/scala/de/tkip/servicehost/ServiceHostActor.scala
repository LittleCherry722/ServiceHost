package de.tkip.servicehost

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.ReferenceXMLActor.Reference
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.Stored
import java.io.File
import java.io.FileOutputStream
import scala.collection.mutable.Map

class ServiceHostActor extends Actor {

  val serviceManager = ActorLocator.serviceActorManager

  def receive: Actor.Receive = {
    case register: RegisterServiceMessage => {
      println("received RegisterServiceMessage: " + register)
      sender ! Some("some RegisterServiceMessage answer")
    }
    case execute: ExecuteServiceMessage => {
      println("received ExecuteServiceMessage: " + execute)
      serviceManager forward (execute)
      sender ! Some("some ExecuteServiceMessage answer")
    }
    case request: CreateProcessInstance => {
      println("received CreateProcessInstance: " + request)
      serviceManager forward request
    }
    case GetProxyActor => {
      println("received GetProxyActor")
      serviceManager forward GetProxyActor
    }
    case message: SubjectToSubjectMessage => {
      println("got SubjectToSubjectMessage " + message + " from " + sender)
      serviceManager forward message
    }
    case s: Stored => {
      println("received Stored: " + s)
    }
    case upload: UploadService => {
      val jsonPath = "src/main/resources/service_JSONs"
      val classPath = "target/scala-2.10/classes/de/tkip/servicehost/serviceactor/stubgen"
      println(upload.serviceClasses)
      extractFile(upload.serviceClasses, classPath)
      extractFile(Map(upload.serviceJsonName->upload.serviceJson), jsonPath)

      implicit val timeout = Timeout(15 seconds)
      val future = ActorLocator.referenceXMLActor ? CreateXMLReferenceMessage(upload.serviceId, classPath.replaceAll("target/scala-2.10/classes/", "").replaceAll("/", ".")
        + "." + upload.serviceClassName.replaceAll(".class", ""), jsonPath + "/" + upload.serviceJsonName)
      
      future onComplete {
        case Success(res) => {
            val ref = res.asInstanceOf[Reference]
            main.registerInterface(ref)
          }
        case Failure(e) => {
            e.printStackTrace()
          }
      }
    }
    case UpdateRepository => {
      main.registerInterfaces
    }
    case something => {
      println("received something: " + something)
      sender ! Some("some answer")
    }
  }

  def extractFile(files: Map[String, Array[Byte]], path: String) {
    for (file <- files.keys) {
      val filePath = new File(path + "/" + file)
      if (!filePath.getParentFile().exists()) filePath.getParentFile().mkdirs()
      val fos = new FileOutputStream(filePath)
      fos.write(files(file), 0, files(file).length)
      fos.close()
    }
  }

}
