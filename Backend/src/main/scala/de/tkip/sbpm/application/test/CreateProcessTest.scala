package de.tkip.sbpm.application.test

import scala.collection.mutable.ArrayBuffer

import akka.actor._
import scala.concurrent.Await
import scala.concurrent.Future
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout

import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object CreateProcessTest extends App {

  val system = ActorSystem("TextualEpassIos")
  val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
  val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

  implicit val timeout = Timeout(5 seconds)

  // instantiate subjectProvider
  val future1 = subjectProviderManager ? CreateSubjectProvider()
  val userID: Int =
    Await.result(future1, timeout.duration).asInstanceOf[SubjectProviderCreated].userID

  println("UserID: " + userID)

  for (i <- 0 to 4) {
    val future2 = subjectProviderManager ? CreateProcess(userID)
    val processID: Int =
      Await.result(future2, timeout.duration).asInstanceOf[ProcessCreated].processID

    println("ProcessID: " + processID)
  }

}


