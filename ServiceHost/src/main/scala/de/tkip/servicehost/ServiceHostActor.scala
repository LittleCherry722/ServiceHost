package de.tkip.servicehost

import akka.actor.Actor
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.Stored
import de.tkip.sbpm.logging.DefaultLogging
import java.io.File
import java.io.FileOutputStream
import scala.collection.mutable.Map

class ServiceHostActor extends Actor with DefaultLogging {

  val serviceManager = ActorLocator.serviceActorManager

  def receive: Actor.Receive = {
    case register: RegisterServiceMessage => {
      log.debug("received RegisterServiceMessage: " + register)
      sender ! Some("some RegisterServiceMessage answer")
    }
    case execute: ExecuteServiceMessage => {
      log.debug("received ExecuteServiceMessage: " + execute)
      serviceManager forward (execute)
      sender ! Some("some ExecuteServiceMessage answer")
    }
    case request: CreateProcessInstance => {
      log.debug("received CreateProcessInstance: " + request)
      serviceManager forward request
    }
    case GetProxyActor => {
      log.debug("received GetProxyActor")
      serviceManager forward GetProxyActor
    }
    case message: SubjectToSubjectMessage => {
      log.debug("got SubjectToSubjectMessage " + message + " from " + sender)
      serviceManager forward message
    }
    case s: Stored => {
      log.debug("received Stored: " + s)
    }
    case upload: UploadService => {
      val jsonPath = "src/main/resources/service_JSONs"
      val classPath = "target/scala-2.10/classes/de/tkip/servicehost/serviceactor/stubgen"
      log.debug(upload.serviceClasses.toString)
      extractFile(upload.serviceClasses, classPath)
      extractFile(Map(upload.serviceJsonName->upload.serviceJson), jsonPath)

      ActorLocator.referenceXMLActor ! CreateXMLReferenceMessage(upload.serviceId, classPath.replaceAll("target/scala-2.10/classes/", "").replaceAll("/", ".")
        + "." + upload.serviceClassName.replaceAll(".class", ""), jsonPath + "/" + upload.serviceJsonName)
      main.registerInterface
    }
    case UpdateRepository => {
      main.registerInterface
    }
    case something => {
      log.warning("received something unexpected: " + something)
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
