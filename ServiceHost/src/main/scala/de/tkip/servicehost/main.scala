package de.tkip.servicehost

import akka.actor._
import akka.pattern.ask
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Await;
import scala.concurrent.Promise;
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.collection.mutable.Map
import scalaj.http.{ Http, HttpOptions }
import Messages.RegisterServiceMessage
import Messages.ExecuteServiceMessage
import java.util.Date
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.eventbus.RemotePublishActor
import de.tkip.servicehost.ReferenceXMLActor.Reference
import de.tkip.servicehost.serviceactor.stubgen.StubGeneratorActor
import Messages.{ CreateXMLReferenceMessage, GetAllClassReferencesMessage }
import de.tkip.servicehost.Messages.UploadService
import java.io.File
import java.io.FileOutputStream
import de.tkip.servicehost.Messages.UpdateRepository
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

object main extends App {
  val system = ActorSystem("sbpm")
  val log = system.log

  log.info("main starting..")

  implicit val timeout = Timeout(15 seconds)

  val repoUrl = "http://localhost:8181/repo"

  val referenceXMLActor = system.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")
  var serviceHost: ActorRef = null

  if (args.contains("service") && args.length >= 2) {
    val path = args(args.indexOf("service") + 1)

    val generator = system.actorOf(Props[StubGeneratorActor], "stub-generator-actor")

    val future = generator ? path // ask pattern: response will be stored in future
    future onComplete {
      case Success(res) => {
          val ref = res.asInstanceOf[Reference]
          log.info("generation completed, json file copied to: " + ref.json)
          log.info("shutting down akka system..")
          system.shutdown
        }
      case Failure(e) => {
          e.printStackTrace()
          log.info("shutting down akka system..")
          system.shutdown
        }
    }
  } else {
    system.actorOf(Props[ServiceActorManager], "service-actor-manager")
    system.actorOf(Props[RemotePublishActor], "eventbus-remote-publish")
    serviceHost = system.actorOf(Props[ServiceHostActor], "subject-provider-manager")
    log.info("serviceHost path: " + serviceHost.path)
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
    log.debug("registerInterface")

    log.debug("ask ReferenceXMLActor for all registered services")
    val referencesFuture: Future[Any] = referenceXMLActor ? GetAllClassReferencesMessage
    val references = Await.result(referencesFuture, timeout.duration).asInstanceOf[List[Reference]]

    for {reference <- references} {
      log.debug("reference: " + reference)
    }

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
      log.info("Registered interface at local repository")
    } else {
      log.error("Some error occurred; " + result)
    }
  }

  log.info("main started")
}
