package de.tkip.sbpm.application.subject.behavior

import org.scalatest.{ BeforeAndAfterAll, FunSuiteLike }
import akka.testkit.{ TestKit, TestActorRef }
import akka.actor._
import akka.actor.ActorDSL._
import de.tkip.sbpm.application.subject.SubjectData
import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.application.subject.misc.{ Rejected, Stored, SubjectToSubjectMessage }
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

private class DummyBlockingActor extends Actor {
  def receive = FSM.NullFunction
}

class InputPoolActorTest extends TestKit(ActorSystem("TestSystem")) with FunSuiteLike with BeforeAndAfterAll {

  val subject = Subject("Subj1", -1, Map(), false,null)
  val subjectData = SubjectData(1, 1, 1, null, TestActorRef[DummyBlockingActor], subject)

  override def afterAll() {
    system.shutdown()
  }

  test("register single subscriber") {
    val actor = TestActorRef(new InputPoolActor(subjectData))
    implicit val i = inbox()

    actor ! SubscribeIncomingMessages(2, "other", "test")

    assert(i.receive().isInstanceOf[InputPoolMessagesChanged])
    assert(i.receive() === InputPoolSubscriptionPerformed)
  }

  test("message receiving after registration") {
    val actor = TestActorRef(new InputPoolActor(subjectData))
    implicit val i = inbox()
    val msg = SubjectToSubjectMessage(1, 1, 1, "other", null, "test", "test msg!")

    actor ! SubscribeIncomingMessages(2, "other", "test")
    actor ! msg

    assert(i.receive() match {
      case InputPoolMessagesChanged("other", "test", m) => m.isEmpty
      case _ => false
    },
      "An empty inputpool should not contain a message")
    assert(i.receive() === InputPoolSubscriptionPerformed)
    assert(i.receive() === Stored(1))
    assert(
      i.receive() match {
        case InputPoolMessagesChanged("other", "test", m) => true
        case r => println(s"invalid: $r"); false

      },
      "The changed message should be send to the receive state")
  }

  test("close input pool") {
    val actor = TestActorRef(new InputPoolActor(subjectData))
    implicit val i = inbox()
    val msg = SubjectToSubjectMessage(1, 1, 1, "other", null, "test", "test msg!")

    actor ! SubscribeIncomingMessages(2, "other", "test")
    actor ! CloseInputPool(("other", "test"))
    actor ! msg

    assert(i.receive() match {
      case InputPoolMessagesChanged("other", "test", m) => m.isEmpty
      case _ => false
    },
      "A closed inputpool should not contain a message")
    assert(i.receive() === InputPoolSubscriptionPerformed)
    assert(i.receive() === InputPoolClosed)
    //assert(i.receive() === Rejected(1))
  }
  
  test("queueing test with disabled message") {
    val actor = TestActorRef(new InputPoolActor(subjectData))
    implicit val i = inbox()
    val msg = SubjectToSubjectMessage(1, 1, 1, "other", null, "test", "test msg!")
    
    actor ! SubscribeIncomingMessages(2, "other", "test")
    actor ! msg

    assert(i.receive().isInstanceOf[SubjectToSubjectMessage])
    //assert(i.receive() === Stored(1))
    //assert(i.receive() === Rejected(1))
  }
  
  
}

class ClosedChannelsTest extends FunSuiteLike {

  private val DummyChannel = ("other", "test")

  test("no closed channels") {
    val closedChannels = new ClosedChannels

    assert(closedChannels.isChannelClosedAndNotReOpened(DummyChannel) === false)
  }

  test("closed channel") {
    val closedChannels = new ClosedChannels
    closedChannels.close((DummyChannel))

    assert(closedChannels.isChannelClosedAndNotReOpened(DummyChannel) === true)
  }

  test("closed channel with pseudo type") {
    val closedChannels = new ClosedChannels
    closedChannels.close(((AllSubjects, AllMessages)))

    assert(closedChannels.isChannelClosedAndNotReOpened(DummyChannel) === true)
  }

  test("opened channel") {
    val closedChannels = new ClosedChannels
    closedChannels.open((("other", "test")))

    assert(closedChannels.isChannelClosedAndNotReOpened(DummyChannel) === false)
  }

  test("reopened channel") {
    val closedChannels = new ClosedChannels
    closedChannels.close((DummyChannel))
    closedChannels.open(DummyChannel)

    assert(closedChannels.isChannelClosedAndNotReOpened(DummyChannel) === false)
  }

  test("reopened channel with pseudo type") {
    val closedChannels = new ClosedChannels
    closedChannels.close(DummyChannel)
    closedChannels.open((DummyChannel._1, AllMessages))

    assert(closedChannels.isChannelClosedAndNotReOpened(DummyChannel) === false)
  }
}
