package de.tkip.sbpm.application

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.PoisonPill
import de.tkip.sbpm.model._

import scala.concurrent._

import akka.util.Timeout
import akka.pattern.ask

class ProcessInstanceActor(pair: Int) extends Actor {
  implicit val timeout = Timeout(3000)
  private var subjectMap: Map[Int, ActorRef] = Map.empty[Int, ActorRef]
  changePair(1)

  def receive = {
    case message: SubjectMessage if (subjectMap contains message.subjectId) => {
      subjectMap(message.subjectId) forward message
    }
    case message: SubjectMessage => {
      System.err.println("Unknown Subject for: " + message)
    }
    case message: TestPairMessage if (List(1, 2) contains message.instance) => {
      changePair(message.instance)
    }
    case message: TestPairMessage => {
      System.err.println("Unknown TestPair for: " + message.instance)
    }
    case message: SubjectToSubjectMessage => {
      val future = (subjectMap(message.to) ? message).mapTo[Ack]
      val result = Await.result(future, timeout.duration)
      sender forward result
    }
  }

  private def changePair(newPair: Int) {
    // Kill the current running subjects
    subjectMap map (_._2 ! PoisonPill)

    // create a new map containing the new testpair
    val TestPair(s1, s2) = TestData(newPair)
    def entry(s: Subject) =
      s.subjectID -> context.actorOf(Props(new SubjectActor(s)))
    subjectMap = Map(entry(s1), entry(s2))
  }
}
