package de.tkip.sbpm.application.subject.behavior

import org.scalatest.FunSuite
import akka.testkit.{TestKit, TestActorRef}
import akka.actor._
import akka.actor.ActorDSL._
import de.tkip.sbpm.application.subject.SubjectData
import de.tkip.sbpm.model.Subject

class InputPoolActorTest extends TestKit(ActorSystem("TestSystem")) with FunSuite {

  val subject = Subject("Subj1", -1, Array(), false)
  val subjectData = SubjectData(1, 1, 1, null, null, subject)

  test("register single subscriber") {
    val actor = TestActorRef(new InputPoolActor(subjectData))
    implicit val i = inbox()

    actor ! SubscribeIncomingMessages(2, "other", "test")

    assert(i.receive() === InputPoolSubscriptionPerformed)
  }


}
