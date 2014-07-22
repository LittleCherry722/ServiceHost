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

import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.eventbus.RemotePublishActor
import de.tkip.sbpm.instrumentation.ClassTraceLogger
import de.tkip.servicehost.ReferenceXMLActor.Reference
import de.tkip.servicehost.serviceactor.stubgen.StubGeneratorActor
import Messages._
import de.tkip.servicehost.Messages._

object main extends App with ClassTraceLogger {
  val system = ActorSystem("sbpm")
  val log = system.log

  log.info("main starting..")

  import DefaultJsonProtocol._
  

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
    system.actorOf(Props[RemotePublishActor], "eventbus-remote-publish")
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
    log.warning("TODO: deregisterInterfaces")
    // TODO: delete the interfaces from repo // for ... registeredInterfaces
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

    val obj: JsObject = sourceString.asJson.asInstanceOf[JsObject]

    val interfaceName: String = obj.getFields("name").head.convertTo[String]
    val processId: Int = obj.getFields("processId").head.convertTo[Int]
    val graph: GraphSubject = obj.getFields("graph").head.convertTo[GraphSubject]
    val messages: Map[String, GraphMessage] = obj.getFields("messages").head.convertTo[Map[String, GraphMessage]]
    val conversations: Map[String, GraphConversation] = obj.getFields("conversations").head.convertTo[Map[String, GraphConversation]]

    val name: String = graph.name
    val relatedSubjectId: String = graph.relatedSubjectId.getOrElse(name)
    val relatedInterfaceId: Int = graph.relatedInterfaceId.getOrElse(nextId)

    val id: Int = processId // TODO: what should be used here?
    val interfaceId: Int = relatedInterfaceId // TODO: what should be used here?
    val graphId: Int = nextId // TODO: what should be used here?
    val date: Int = 123456789 // TODO: what should be used here?


    val impl = InterfaceImplementation(processId, interfaceId, de.tkip.sbpm.model.Address(hostname, port), name)

    val newGraph = GraphSubject(
      graph.id,
      graph.name,
      graph.subjectType,
      graph.isDisabled,
      graph.isStartSubject,
      graph.inputPool,
      Some(relatedSubjectId), // graph.relatedSubjectId
      Some(relatedInterfaceId), // graph.relatedSubjectId
      Some(true), // graph.isImplementation
      graph.externalType,
      graph.role,
      None, // graph.url // TODO: repository does not parse url!
      Some(List(impl)), // graph.implementations
      graph.comment,
      graph.variables,
      graph.macros
    )

    // TODO: graphJsonFormat is inconsistent, if that is resolved create a Graph instance instead of JsValues
    val interface = JsObject(
      "id" -> id.toJson,
      "interfaceId" -> interfaceId.toJson,
      "name" -> interfaceName.toJson,
      "port" -> port.toJson,
      "graph" -> JsObject(
        "id" -> graphId.toJson,
        "processId" -> processId.toJson,
        "date" -> date.toJson,
        "routings" -> JsArray(), // TODO: may not leave empty?
        "definition" -> JsObject(
          "conversations" -> conversations.toJson,
          "messages" -> messages.toJson,
          "process" -> List(newGraph).toJson
        )
      )
    )

    val jsonString = interface.prettyPrint // TODO: compactPrint 

    log.debug("generated interface json for '" + interfaceName + "'; POST it to repo")

    //println(jsonString)

    val post = Http.postData(repoUrl, jsonString)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.connTimeout(10000)).option(HttpOptions.readTimeout(30000))
    val result = post.responseCode

    if (result == 200) {
      var id: Int = post.asString.toInt

      log.info("Registered interface for '" + interfaceName + "' with id '" + id + "' at repository")
      registeredInterfaces(id) = reference
    } else {
      log.warning("Some error occurred; HTTP Code: " + result)
    }
  }

  log.info("main started")
}

