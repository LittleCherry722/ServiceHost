package de.tkip.servicehost

import java.util.Date
import spray.json._
import scala.collection.mutable.ArrayBuffer
import scala.util.parsing.json.JSON

import akka.actor._
import akka.pattern.ask
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import scalaj.http.{ Http, HttpOptions }
import Messages.RegisterServiceMessage
import Messages.ExecuteServiceMessage
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.GraphJsonProtocol._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.eventbus.RemotePublishActor
import de.tkip.servicehost.ReferenceXMLActor.Reference
import de.tkip.servicehost.serviceactor.stubgen.StubGeneratorActor
import Messages.{ CreateXMLReferenceMessage, GetAllClassReferencesMessage }

/*

Momentan funktioniert es nur so: Starte Instanz von Prozess Großunternehmen. Führe aus bis send. Message kommt hier an.

 */


object main extends App {
  import DefaultJsonProtocol._
  

  var currentId = 777
  private def nextId = {
    val id = currentId
    currentId += 1
    id
  }

  implicit val timeout = Timeout(15 seconds)

  val system = ActorSystem("sbpm")
  
  protected def configString(key: String) =
    system.settings.config.getString(key)

  println("main started")
  val repoUrl = configString("sbpm.repo.address") + "/interfaces"

  val referenceXMLActor = system.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")

  if (args.contains("service") && args.length >= 2) {
    val path = args(args.indexOf("service") + 1)

    val generator = system.actorOf(Props[StubGeneratorActor], "stub-generator-actor")
//    val future: Future[Any]= ask(generator, path)
//    val res = Await.result(future, timeout.duration)
    generator ! path
    system.shutdown
  } // TODO add other root Actors
  else {
    system.actorOf(Props[ServiceActorManager], "service-actor-manager")
    system.actorOf(Props[RemotePublishActor], "eventbus-remote-publish")
    system.actorOf(Props[ServiceHostActor], "subject-provider-manager")
    registerInterfaces()

    sys.addShutdownHook {
      println("Shutting down the system...")
      // TODO: stop futures / running actors
      deregisterInterfaces()

      system.shutdown();
    }
  }

  def deregisterInterfaces(): Unit = {
    println("TODO: deregisterInterfaces")
    // TODO: delete the interfaces from repo
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
    println("registerInterfaces")
    
    val hostname: String = configString("akka.remote.netty.tcp.hostname")
    val port: Int = configString("akka.remote.netty.tcp.port").toInt


    println("ask ReferenceXMLActor for all registered services")
    val referencesFuture: Future[Any] = referenceXMLActor ? GetAllClassReferencesMessage
    val references = Await.result(referencesFuture, timeout.duration).asInstanceOf[List[Reference]]

    for {reference <- references} {
      println("read service: " + reference)

      val file = reference.jsonpath
      val source = scala.io.Source.fromFile(file)
      val sourceString: String = source.mkString
      source.close()

      // create objects from json

      val obj: JsObject = sourceString.asJson.asInstanceOf[JsObject]

      val interfaceName: String = obj.getFields("name").head.convertTo[String]
      val processId: Int = obj.getFields("processId").head.convertTo[Int] // TODO: needs to be included in frontend export
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
        List(impl), // graph.implementations
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

      println("generated interface json for '" + interfaceName + "'; POST it to repo")

      //println(jsonString)

      val post = Http.postData(repoUrl, jsonString)
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .option(HttpOptions.connTimeout(10000)).option(HttpOptions.readTimeout(30000))
      val result = post.responseCode

      if (result == 200) {
        println("Registered interface for '" + interfaceName + "' at repository")

        // TODO: store id for deregistration ?
        var id = post.asString
        println("id: " + id)
      } else {
        println("Some error occurred; HTTP Code: " + result)
      }

    }

    println("finished registerInterfaces")
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
    case something => {
      println("received something: " + something)
      sender ! Some("some answer")
    }
  }
}
