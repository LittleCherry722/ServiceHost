package de.tkip.servicehost.serviceactor.stubgen

import scala.collection.immutable.List
import de.tkip.servicehost.serviceactor.ServiceActor
import akka.actor.PoisonPill

import de.tkip.sbpm.application.subject.behavior.Transition

abstract class State {

  var comment, conversation, macro, message, state, subject, text, typeState, variable: String
  var deactivated, end, majorStartNode, start: Boolean
  var id, correlationId: Double
  var varMan: Map[String, String]

  def apply(id: Double, nextStateId: Double, args: Any)
  def apply(sourceNode: Map[String, Any], edge: Any, targetId: Double) {
    id = sourceNode("id").asInstanceOf[Int];
    end = sourceNode("end").asInstanceOf[Boolean];
    start = sourceNode("start").asInstanceOf[Boolean];
    majorStartNode = sourceNode("majorStartNode").asInstanceOf[Boolean];
    typeState = sourceNode("type").asInstanceOf[String];
  }
  def process()(implicit actor: ServiceActor)

}

case class ReceiveState extends State {
  def process()(implicit actor: ServiceActor) {

  }

  def handle(msg: Any)(implicit actor: ServiceActor) {
    actor.storeMsg(msg)
    actor.changeState()
  }
}
object SendState extends State {
  def process()(implicit actor: ServiceActor) {

  }

  def send(msg: String, transition: Transition)(implicit actor: ServiceActor) {
    val sender = actor.getSender()
    val messageType = transition.messageType
    val toSubject = transition.subjectID
    val messageID = 100 //TODO change if needed
    val target = transition.target.get
    
    val fileInfo = null

    val subjectId = id
    sender !
      SubjectToSubjectMessage(
        messageID,
        processID,
        userID,
        subjectID,
        target,
        messageType,
        msg,
        fileInfo)
  }
}
object ExitState extends State {

  def process()(implicit actor: ServiceActor) {
    actor.self ! PoisonPill
  }
}
