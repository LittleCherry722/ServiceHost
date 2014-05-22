package de.tkip.sbpm.application.test

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.SubjectProviderManagerActor
import de.tkip.sbpm.application.miscellaneous.CreateSubjectProvider
import de.tkip.sbpm.application.miscellaneous.SubjectProviderCreated
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.UserID
import akka.testkit.TestKit

class CreateSubjectProviderTest extends TestKit(ActorSystem("TestSystem")) with FunSuiteLike with BeforeAndAfterAll {
  implicit val timeout = Timeout(5 seconds)
  val subjectProviderManager = system.actorOf(Props[SubjectProviderManagerActor], ActorLocator.subjectProviderManagerActorName)

  override def afterAll() {
    system.shutdown()
  }

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

