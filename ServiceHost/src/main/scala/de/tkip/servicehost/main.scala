package de.tkip.servicehost

import java.util.Date
import spray.json._
import DefaultJsonProtocol._
import scala.collection.mutable.ArrayBuffer

import akka.actor._
import scalaj.http.{Http, HttpOptions}
import Messages.RegisterServiceMessage
import Messages.ExecuteServiceMessage
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.eventbus.RemotePublishActor

/*

Momentan funktioniert es nur so: Starte Instanz von Prozess Großunternehmen. Führe aus bis send. Message kommt hier an.

 */


object main extends App {
  println("main started")
  val repoUrl = "http://localhost:8181/repo/interfaces"
  val system = ActorSystem("sbpm")

  // TODO add other root Actors
  
  system.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")  
  system.actorOf(Props[ServiceActorManager], "service-actor-manager")
  system.actorOf(Props[RemotePublishActor], "eventbus-remote-publish")
  system.actorOf(Props[ServiceHostActor], "subject-provider-manager")
  registerInterface()


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
    val processes = ArrayBuffer[JsValue]()

    println("read processes")
    // TODO: read directory
    for {file <- Array("./src/main/resources/staples.json")} {
      println("read process " + file)

      val source = scala.io.Source.fromFile(file)
      val sourceString: String = source.mkString
      source.close()

      val sourceJson: JsValue = sourceString.asJson
      val processGraph: JsValue = sourceJson.asInstanceOf[JsObject].getFields("graph").head
      processes += processGraph
    }

    println("read all processes")

    // TODO: direkt JsObjects erzeugen
    // TODO: werte in application.conf eintragen und hier auslesen
    val interfaceJson: JsValue = ("""
    {
      "id": 1,
      "interfaceId": 1234,
      "name": "MyServiceHost",
      "port": 2553,
      "graph": {
        "id": 123456,
        "processId": 21,
        "date": 123456789,
        "routings": [],
        "definition": {
          "conversations": {"a": "b"},
          "messages": {"c": "d"},
          "process": """ + processes.toArray.toJson.prettyPrint + """
        }
      }
    }
    """).asJson

    println("generated interfaceJson")

    val jsonString = interfaceJson.prettyPrint // TODO: compactPrint

    println("printed json to str and POST it")

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
      serviceManager forward(execute)
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
      println("received Stored: "+s)
    }
    case something => {
      println("received something: "+something)
      sender ! Some("some answer")
    }
  }
}
