package de.tkip.servicehost.serviceactor.stubgen

import scala.collection.immutable.List
import de.tkip.servicehost.serviceactor.ServiceActor
import akka.actor.PoisonPill
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.behavior.state.StateData

class Target(id: Int, min: Int, max: Int, createNew: Boolean, variable: Option[String]) {
  //  def apply(id: Int, min:Int, max:Int, createNew : Boolean, variable: Option[String]){
  val toExternal = true
  val defaultValues = false;
  val target = new de.tkip.sbpm.application.subject.behavior.Target(String.valueOf(id), min, max, createNew, variable, toExternal, defaultValues)
  //}

}
object Target {
  def apply(id: Int, min: Int, max: Int, createNew: Boolean, variable: String) = {
    new Target(id, min, max, createNew, Option(variable))
  }
}

abstract class State {

  //  var comment, conversation, macro, message, state, subject, text, typeState, variable: String
  //  var deactivated, end, majorStartNode, start: Boolean
  var id = -1 //, correlationId: Double
  //  var varMan: Map[String, String]
  var targetId = -1
  var exitType: String = "" 
  var target: Target = null

  //  var sourceNode:  Map[String, Any]

  //  var edge : Transition

  def apply(id: Int, exitType: String, target: Target, targetId: Int) {
    this.id = id;
    this.targetId = targetId
    this.exitType = exitType
    this.target = target
  }
  //  def apply(id: Double, targetId: Double, args: Any){
  //    this.id = id;
  //    this.targetId = targetId
  //    //TODO: args???
  //  }
  //  def apply(sourceNode: Map[String, Any], edge: Transition, targetId: Double) {
  //    id = sourceNode("id").asInstanceOf[Int];
  //    end = sourceNode("end").asInstanceOf[Boolean];
  //    start = sourceNode("start").asInstanceOf[Boolean];
  //    majorStartNode = sourceNode("majorStartNode").asInstanceOf[Boolean];
  //    typeState = sourceNode("type").asInstanceOf[String];
  //    
  //    this.targetId = targetId
  //    this.edge = edge
  //    this.sourceNode = sourceNode
  //  }
  def process()(implicit actor: ServiceActor)

}

case class ReceiveState extends State {
  def process()(implicit actor: ServiceActor) {
    //TODO
  }

  def handle(msg: Any)(implicit actor: ServiceActor) {
    actor.storeMsg(msg)
    actor.changeState()
  }
}

object SendState extends State {
  def process()(implicit actor: ServiceActor) {
    val msg = ""
    send(msg)
  }

  def send(msg: String)(implicit actor: ServiceActor) {

    val messageID = 100 //TODO change if needed

    val messageType = exitType

    val userID = actor.getUserID()
    val processID = actor.getProcessID()
    val subjectID = actor.getSubjectID()
    val sender = actor.getSender()

    val fileInfo = None

    sender !
      SubjectToSubjectMessage(
        messageID,
        processID,
        userID,
        subjectID,
        target.target,
        messageType,
        msg,
        fileInfo)
  }
}

object ExitState extends State {

  def process()(implicit actor: ServiceActor) {
    actor.terminate()
  }
}
