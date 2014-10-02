package de.tkip.servicehost

import java.io.File
import java.io.FileOutputStream
import java.util.Date
import spray.json._
import scala.collection.mutable.ArrayBuffer

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global;
import scala.util.{Success, Failure}
import scalaj.http.{ Http, HttpOptions }

import de.tkip.sbpm.{ ActorLocator => BackendActorLocator }
import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.eventbus.RemotePublishActor
import de.tkip.sbpm.instrumentation.ClassTraceLogger
import de.tkip.sbpm.repository.RepositoryPersistenceActor
import de.tkip.sbpm.repository.RepositoryPersistenceActor._
import de.tkip.sbpm.rest.JsonProtocol.{GraphHeader, createInterfaceHeaderFormat}
import de.tkip.servicehost.ReferenceXMLActor.Reference
import de.tkip.servicehost.serviceactor.stubgen.{ ServiceExport, StubGeneratorActor }
import Messages._
import de.tkip.servicehost.Messages._

object main extends App with ClassTraceLogger {
  val system = ActorSystem("sbpm")
  val log = system.log

  log.info("main starting..")

  import DefaultJsonProtocol._
  import StubGeneratorActor.serviceExportFormat
  

  var currentId = 777
  private def nextId = {
    val id = currentId
    currentId += 1
    id
  }

  implicit val timeout = Timeout(15 seconds)

  
  protected def configString(key: String) =
    system.settings.config.getString(key)

  val repoUrl = configString("sbpm.repo.address") + "/interfaces"
  val hostname: String = configString("akka.remote.netty.tcp.hostname")
  val port: Int = configString("akka.remote.netty.tcp.port").toInt

  val registeredInterfaces = scala.collection.mutable.Map[Int, Reference]()

  val referenceXMLActor = system.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")
  val repositoryPersistenceActor = system.actorOf(Props[RepositoryPersistenceActor], BackendActorLocator.repositoryPersistenceActorName)
  var serviceHost: ActorRef = null

  if (args.contains("service") && args.length >= 2) {
    val path = args(args.indexOf("service") + 1)

    val generator = system.actorOf(Props[StubGeneratorActor], "stub-generator-actor")

    val future = generator ?? path // ask pattern: response will be stored in future
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
    //system.actorOf(Props[RemotePublishActor], "eventbus-remote-publish")
    serviceHost = system.actorOf(Props[ServiceHostActor], "subject-provider-manager")
    log.info("serviceHost path: " + serviceHost.path)
    registerInterfaces()

    sys.addShutdownHook {
      log.info("Shutting down the system...")
      // TODO: stop futures / running actors
      deregisterInterfaces()

      system.shutdown();
    }
  }

  def deregisterInterfaces(): Unit = {
    log.info("deregisterInterfaces. registeredInterfaces: {}", registeredInterfaces)

    // delete the interfaces from repo
    val f_seq = for ((interfaceId, reference) <- registeredInterfaces)
      yield (repositoryPersistenceActor ?? DeleteInterface(interfaceId))

    val f = Future.sequence(f_seq)
    Await.result(f, timeout.duration)
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
  def registerInterfaces(): Unit = {
    log.debug("registerInterfaces")

    log.debug("ask ReferenceXMLActor for all registered services")
    val referencesFuture: Future[Any] = referenceXMLActor ?? GetAllClassReferencesMessage
    val references = Await.result(referencesFuture, timeout.duration).asInstanceOf[List[Reference]]
    for {reference <- references} {
      registerInterface(reference)
    }
    log.info("finished registerInterfaces")
  }

  def registerInterface(reference: Reference): Unit = {
    log.debug("read service: " + reference)

    val file = reference.json
    val source = scala.io.Source.fromFile(file)
    val sourceString: String = source.mkString
    source.close()

    // create objects from json

    val processId: Int = nextId //1337 //obj.getFields("processId").head.convertTo[Int] // TODO: what should be used here?
    val date: java.sql.Timestamp = new java.sql.Timestamp(System.currentTimeMillis()) // TODO: what should be used here?
    val routings: Seq[GraphRouting] = List() // TODO: what should be used here?


    val serviceExport: ServiceExport = sourceString.parseJson.convertTo[ServiceExport]
    val graphSubject: GraphSubject = serviceExport.graph.copy(
      role = None,
      subjectType = "single",
      isImplementation = Some(true)
    )

    val subjects: Map[String, GraphSubject] = Map(graphSubject.id -> graphSubject)


    val graph = Graph(
      None,
      Some(serviceExport.processId),
      date,
      serviceExport.conversations,
      serviceExport.messages,
      subjects,
      routings
    )



    val gHeader = GraphHeader(
      serviceExport.name,
      None,
      true, // TODO??
      Some(graph),
      false, // TODO??
      Some(processId)
    )

    val interfaceIdFuture: Future[Option[Int]] = (repositoryPersistenceActor ?? SaveInterface(gHeader, Some(Map()))).mapTo[Option[Int]]

    val interfaceId: Option[Int] = Await.result(interfaceIdFuture, timeout.duration)

    if (interfaceId.isDefined) {
      var id: Int = interfaceId.get
      log.info("Registered interface for '" + serviceExport.name + "'; got interfaceId '" + interfaceId + "' from repository")
      registeredInterfaces(id) = reference
    } else {
      log.warning("Some error occurred")
    }
  }

  log.info("main started")
}

