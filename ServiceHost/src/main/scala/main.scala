import akka.actor._
import scalaj.http.{Http, HttpOptions}
import Messages.RegisterServiceMessage
import Messages.ExecuteServiceMessage

import java.util.Date
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._


/*

Momentan funktioniert es nur so: Starte Instanz von Prozess Großunternehmen. Führe aus bis send. Message kommt hier an.

 */


object main extends App {
  println("main started")
  val repoUrl = "http://localhost:8181/repo"
  val system = ActorSystem("sbpm")

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
    val source = scala.io.Source.fromFile("./src/main/resources/interface.json")
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
  private var userId = 0
  private var processId = 0

  def receive: Actor.Receive = {
    case register: RegisterServiceMessage => {
      println("received RegisterServiceMessage: " + register)
      // TODO implement
      sender ! Some("some RegisterServiceMessage answer")
    }
    case execute: ExecuteServiceMessage => {
      println("received ExecuteServiceMessage: " + execute)
      // TODO implement
      sender ! Some("some ExecuteServiceMessage answer")
    }
    case request: CreateProcessInstance => {
      println("received CreateProcessInstance: " + request)
      userId = request.userID
      processId = request.processID
      // TODO implement

      // fake ProcessInstanceActor:

      val persistenceGraph = null
      val processName = ""
      val startedAt = new Date()
      val actions = null
      val processInstanceData = ProcessInstanceData(0, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
      sender ! ProcessInstanceCreated(request, self, processInstanceData)
    }
    case GetProxyActor => {
      println("received GetProxyActor")
      // TODO implement
      // fake ProcessInstanceProxyActor:
      sender ! self
    }
    case message: SubjectToSubjectMessage => {
      println("got SubjectToSubjectMessage " + message + " from " + sender)
      // TODO implement

      // fake InputPoolActor:

      // Unlock the sender
      sender ! Stored(message.messageID)
      println("unblocked sender")

      // TODO store and handle the message
      val target = Target("Großunternehmen",0,1,false,None,true,true)
      val messageType = "Message Type"
      val messageContent = "Message Content"
      val answer = SubjectToSubjectMessage(0, processId, userId, "Staples", target, messageType, messageContent)
      //val to_actor = TODO
      //println("send " + answer + " to " + to_actor)
      //to_actor ! answer
    }
    case something => {
      println("received something: "+something)
      sender ! Some("some answer")
    }
  }
}