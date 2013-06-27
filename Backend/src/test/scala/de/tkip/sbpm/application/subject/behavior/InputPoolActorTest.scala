package de.tkip.sbpm.application.subject.behavior

import org.scalatest.FunSuite
import akka.testkit.{TestKit, TestActorRef}
import akka.actor._
import akka.actor.ActorDSL._
import de.tkip.sbpm.application.subject.SubjectData
import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.application.subject.misc.{Rejected, Stored, SubjectToSubjectMessage}

private class DummyBlockingActor extends Actor {
  def receive = FSM.NullFunction
}

class InputPoolActorTest extends TestKit(ActorSystem("TestSystem")) with FunSuite {


  val subject = Subject("Subj1", -1, Array(), false)
  val subjectData = SubjectData(1, 1, 1, null, TestActorRef[DummyBlockingActor], subject)

  test("register single subscriber") {
    val actor = TestActorRef(new InputPoolActor(subjectData))
    implicit val i = inbox()

    actor ! SubscribeIncomingMessages(2, "other", "test")

    assert(i.receive() === InputPoolSubscriptionPerformed)
  }
  
  test("message receiving after registration") {
    val actor = TestActorRef(new InputPoolActor(subjectData))
    implicit val i = inbox()
    val msg = SubjectToSubjectMessage(1, 1, "other", null, "test", "test msg!")

    actor ! SubscribeIncomingMessages(2, "other", "test")
    actor ! msg

    assert(i.receive() === InputPoolSubscriptionPerformed)
    assert(i.receive() === Stored(1))
    assert(i.receive() === msg)
  }

  test("close input pool") {
    val actor = TestActorRef(new InputPoolActor(subjectData))
    implicit val i = inbox()
    val msg = SubjectToSubjectMessage(1, 1, "other", null, "test", "test msg!")

    actor ! SubscribeIncomingMessages(2, "other", "test")
    actor ! CloseInputPool(("other", "test"))
    actor ! msg

    assert(i.receive() === InputPoolSubscriptionPerformed)
    assert(i.receive() === InputPoolClosed)
    assert(i.receive() === Rejected(1))
  }
}
