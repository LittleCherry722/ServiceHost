package de.tkip.servicehost

import akka.actor._
import akka.pattern.ask
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import scalaj.http.{ Http, HttpOptions }
import Messages.RegisterServiceMessage
import Messages.ExecuteServiceMessage
import java.util.Date
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.servicehost.serviceactor.stubgen.StubGeneratorActor
import Messages.CreateXMLReferenceMessage
import de.tkip.servicehost.Messages.UploadService
import java.io.File
import java.io.FileOutputStream
import de.tkip.servicehost.Messages.UpdateRepository

/*

Momentan funktioniert es nur so: Starte Instanz von Prozess Großunternehmen. Führe aus bis send. Message kommt hier an.

 */

object main extends App {

  println("main started")
  val repoUrl = "http://localhost:8181/repo"

  val system = ActorSystem("sbpm")

  var serviceHost: ActorRef = null
  if (args.contains("service") && args.length >= 2) {
    val path = args(args.indexOf("service") + 1)

    system.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")
    val generator = system.actorOf(Props[StubGeneratorActor], "stub-generator-actor")
//    implicit val timeout = Timeout(30 seconds)
//    val future: Future[Any]= ask(generator, path)
//    val res = Await.result(future, timeout.duration)
    generator ! path
    system.shutdown
  } // TODO add other root Actors
  else {
    system.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")
    system.actorOf(Props[ServiceActorManager], "service-actor-manager")
    serviceHost = system.actorOf(Props[ServiceHostActor], "subject-provider-manager")
    println(serviceHost.path)
    registerInterface()
  }

  /**
   * Registers the interface at the interface repository by sending the graph data and some additional information as
   * post request to the repository url
   *
   * Internal Subject name: Lokal (Kunde)
   * External Subject name: Extern (Zulieferer)
   * msg from Lokal -> Extern: input
   * msg from Extern -> Lokal: output
   */
  def registerInterface(): Unit = {
    println("registerInterface")
    //    val source = scala.io.Source.fromFile("./src/main/resources/interface.json")
    val source = scala.io.Source.fromFile("./src/main/resources/service_export_Stapler_service.json")
    val jsonString = source.mkString
    source.close()

    val result = Http.postData(repoUrl, jsonString)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.connTimeout(10000)).option(HttpOptions.readTimeout(30000))
      .responseCode

    if (200 == result) {
      println("Registered interface at local repository")
    } else {
      println("Some error occurred; " + result)
    }
  }
}

class ServiceHostActor extends Actor {

  val serviceManager = ActorLocator.serviceActorManager

  def receive: Actor.Receive = {
    case register: RegisterServiceMessage => {
      println("received RegisterServiceMessage: " + register)
      // TODO implement
      sender ! Some("some RegisterServiceMessage answer")
    }
    case execute: ExecuteServiceMessage => {
      println("received ExecuteServiceMessage: " + execute)
      // TODO implement
      serviceManager forward (execute)
      sender ! Some("some ExecuteServiceMessage answer")
    }
    case request: CreateProcessInstance => {
      println("received CreateProcessInstance: " + request)
      serviceManager forward request
    }
    case GetProxyActor => {
      println("received GetProxyActor")
      // TODO implement
      // fake ProcessInstanceProxyActor:
      serviceManager forward GetProxyActor
    }
    case message: SubjectToSubjectMessage => {
      println("got SubjectToSubjectMessage " + message + " from " + sender)
      // TODO implement
      serviceManager forward message
    }
    case s: Stored => {
      println("received Stored: " + s)
    }
    case upload: UploadService => {
      val jsonPath = "src/main/resources/service_JSONs"
      val classPath = "target/scala-2.10/classes/de/tkip/servicehost/serviceactor/stubgen"
      
      extractFile(upload.serviceClassName, upload.serviceClass, "CLASS")
      extractFile(upload.serviceJsonName, upload.serviceJson, "JSON")
      
      ActorLocator.referenceXMLActor ! CreateXMLReferenceMessage(upload.serviceId, classPath.replaceAll("target/scala-2.10/classes/", "").replaceAll("/", ".")
          + "." + upload.serviceClassName.replaceAll(".class", ""), jsonPath + "/" + upload.serviceJsonName)
      main.registerInterface
    }
    case update: UpdateRepository => {
      main.registerInterface
    }
    case something => {
      println("received something: " + something)
      sender ! Some("some answer")
    }
  }
  
  def extractFile(fileName: String, file: Array[Byte], path: String) {
    val filePath = new File(path + "/" + fileName)
    if (!filePath.getParentFile().exists()) filePath.getParentFile().mkdirs()
    
    val fos = new FileOutputStream(filePath)
    fos.write(file, 0, file.length)
    fos.close()
  }
  
}
