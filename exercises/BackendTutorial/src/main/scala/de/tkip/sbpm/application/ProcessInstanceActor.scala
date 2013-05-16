package de.tkip.sbpm.application

import akka.actor.Actor
import de.tkip.sbpm.model.TestPair
import akka.actor.Props
import de.tkip.sbpm.model.Subject

class ProcessInstanceActor(pair: TestPair) extends Actor {

  private val s1 = context.actorOf(Props(new SubjectActor(pair.subject1)))
  private val s2 = context.actorOf(Props(new SubjectActor(pair.subject2)))
  private val subjectMap = Map(
    pair.subject1.subjectID -> s1,
    pair.subject2.subjectID -> s2)

  def receive = {
    case message: SubjectMessage if (subjectMap contains message.subjectId) => {
      subjectMap(message.subjectId) forward message
    }
  }
}
