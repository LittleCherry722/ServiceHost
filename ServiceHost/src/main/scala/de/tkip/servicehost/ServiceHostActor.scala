package de.tkip.servicehost

import java.io.File
import java.io.FileOutputStream

import scala.collection.mutable.Map
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.ReferenceXMLActor.Reference
import de.tkip.sbpm.application.miscellaneous.{CreateProcessInstance, CreateServiceInstance}
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.Stored
import de.tkip.sbpm.instrumentation.InstrumentedActor

class ServiceHostActor extends InstrumentedActor {

  val serviceManager = ActorLocator.serviceActorManager

  override def preStart {
    println("ServiceHostActor is created ......")
  }

  override def postStop {

  }

  override def preRestart(reason: Throwable, message: Option[Any]) {

  }

  override def postRestart(reason: Throwable) {

  }

  def wrappedReceive = {
    case register: RegisterServiceMessage => {
      log.debug("received RegisterServiceMessage: " + register)
      sender !! Some("some RegisterServiceMessage answer")
    }

    case execute: ExecuteServiceMessage => {
      log.debug("received ExecuteServiceMessage: " + execute)
      serviceManager forward (execute)
      sender !! Some("some ExecuteServiceMessage answer")
    }

    case request: CreateProcessInstance => {
      log.debug("received CreateProcessInstance: " + request)
      serviceManager forward request
    }

    case request: CreateServiceInstance => {
      log.debug("received CreateServiceInstance" + request)
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
      val classPath = "target/scala-2.11/classes/de/tkip/servicehost/serviceactor/stubgen"
      log.debug(upload.serviceClasses.toString)
      extractFile(upload.serviceClasses, classPath)
      extractFile(Map(upload.serviceJsonName -> upload.serviceJson), jsonPath)

      implicit val timeout = Timeout(15 seconds)
      val future = ActorLocator.referenceXMLActor ?? CreateXMLReferenceMessage(upload.serviceId, classPath.replaceAll("target/scala-2.11/classes/", "").replaceAll("/", ".")
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
      log.warning("received something: " + something)
      sender !! Some("some answer")
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
