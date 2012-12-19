package de.tkip.sbpm.application.test

import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._
import akka.actor._
import akka.pattern.ask
import akka.util.duration._
import akka.util.Timeout
import scala.collection.mutable.ArrayBuffer
import akka.dispatch.Await
import akka.dispatch.Future
import ProcessAttributes._

object CreateProcessTest extends App {

  val system = ActorSystem("TextualEpassIos")
  val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
  val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))
  val instanceInterface = system.actorOf(Props(new InstanceInterfaceActor))

  // instantiate subjectProvider and processes
  val userID = 10
  val processID = 100
  subjectProviderManager ! CreateSubjectProvider(userID)
  implicit val timeout = Timeout(5 seconds)
  instanceInterface ! CreateProcess(userID)
  instanceInterface ! CreateProcess(userID)
  instanceInterface ! CreateProcess(userID)
  instanceInterface ! CreateProcess(userID)

  println("fertig")

  class InstanceInterfaceActor extends Actor {
    def receive = {
      case cp: CreateProcess => 
        (subjectProviderManager ? cp)  onSuccess {
          case pc: ProcessCreated => println("Process created: " + pc.processID)
          case _ => println("ungültig")
        }
      case pc: ProcessCreated =>
        println("Process created: " + pc.processID)
      case _ => println("ungültig")
    }
  }
}

