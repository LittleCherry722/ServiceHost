package de.tkip.sbpm.application.subject

import org.scalatest.FunSuite
import akka.testkit.{TestKit, TestActorRef}
import akka.actor.ActorSystem

class InputPoolActorTest extends TestKit(ActorSystem("TestSystem")) with FunSuite {

  val subjectData = SubjectData(1, 1, 1, null, null, null)

  test("register single subscriber") {
    val pool = new InputPoolActor(subjectData)
    val actor = TestActorRef(pool)

    actor ! SubscribeIncomingMessages(2, "other", "test")
  }
}
