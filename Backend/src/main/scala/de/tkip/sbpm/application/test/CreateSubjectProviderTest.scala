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

object CreateSubjectProviderTest extends App {

  val system = ActorSystem("TextualEpassIos")
  val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
  val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

  implicit val timeout = Timeout(5 seconds)

  for (i <- 0 to 4) {
    // instantiate subjectProvider
    val future1 = subjectProviderManager ? CreateSubjectProvider()
    val userID1: Int =
      Await.result(future1, timeout.duration).asInstanceOf[SubjectProviderCreated].userID

    println("UserID: " + userID1)
  }

}

