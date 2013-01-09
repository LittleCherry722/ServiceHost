package de.tkip.sbpm.application.test

import org.junit._
import org.junit.Assert._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

class CreateProcessTest {

  @Test
  def testProcessCreation {

    val system = ActorSystem("TextualEpassIos")
    val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
    val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

    implicit val timeout = Timeout(5 seconds)

    // instantiate subjectProvider
    val future1 = subjectProviderManager ? CreateSubjectProvider()
    val userID: Int =
      Await.result(future1, timeout.duration).asInstanceOf[SubjectProviderCreated].userID

    println("UserID: " + userID)

    val range = (0 to 4).toSet
    for (i <- range) {
      val future2 = subjectProviderManager ? CreateProcess(userID)
      val processID: Int =
        Await.result(future2, timeout.duration).asInstanceOf[ProcessCreated].processID

      assertTrue(range.contains(processID))
      range.drop(processID)

      println("ProcessID: " + processID)
    }
  }
}


