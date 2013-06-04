package de.tkip.sbpm.application

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.PoisonPill
import de.tkip.sbpm.model._

class ProcessInstanceActor(pair: Int) extends Actor {
  private var subjectMap: Map[Int, ActorRef] = Map.empty[Int, ActorRef]
  changePair(1)

  def receive = {
    case message: SubjectMessage if (subjectMap contains message.subjectId) => {
      subjectMap(message.subjectId) forward message
    }
    case message: SubjectMessage => {
      System.err.println("Unknown Subject for: " + message)
    }
    case ChangeTestData(n) => changePair(n)
    case message: SubjectToSubjectMessage if (subjectMap contains message.to) => {
      subjectMap(message.to) forward message
    }
    case Ack => {
      for ((k,v) <- subjectMap) {
        if (!(v == sender))
          subjectMap(k) forward Ack
      }
    }
  }

  def changePair(newPair: Int) {
    // Kill the current running subjects
    subjectMap map ( _._2 ! PoisonPill)

    // create a new map containing the new testpair
    val TestPair(s1, s2) = TestData(newPair)
    
    def entry(s: Subject) =
      s.subjectID -> context.actorOf(Props(new SubjectActor(s)))
    subjectMap = Map(entry(s1), entry(s2))
  }
  
  def f(n:Int) = if (n == 1) 2 else 1
}
