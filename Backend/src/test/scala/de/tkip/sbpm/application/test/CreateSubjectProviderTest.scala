package de.tkip.sbpm.application.test

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import org.scalatest.FunSuite

import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.SubjectProviderManagerActor
import de.tkip.sbpm.application.miscellaneous.CreateSubjectProvider
import de.tkip.sbpm.application.miscellaneous.SubjectProviderCreated
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.UserID

class CreateSubjectProviderTest extends FunSuite {
  implicit val timeout = Timeout(5 seconds)
  val system = ActorSystem()
  val subjectProviderManager = system.actorOf(Props[SubjectProviderManagerActor], ActorLocator.subjectProviderManagerActorName)

  test("test subjectprovider creation") {

    for (i <- 0 to 4) {
      // instantiate subjectProvider
      val future = subjectProviderManager ? CreateSubjectProvider(i)
      val userID: UserID =
        Await.result(future, timeout.duration).asInstanceOf[SubjectProviderCreated].userID

      assert(userID == i)
    }
  }

}

