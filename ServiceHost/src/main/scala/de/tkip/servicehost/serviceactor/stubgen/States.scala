package de.tkip.servicehost.serviceactor.stubgen

import scala.collection.immutable.List
import de.tkip.servicehost.serviceactor.ServiceActor
import akka.actor.PoisonPill
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.behavior.state.StateData

class Target(id: String, min: Int, max: Int, createNew: Boolean, variable: Option[String]) {
  //  def apply(id: Int, min:Int, max:Int, createNew : Boolean, variable: Option[String]){
  val toExternal = true
  val defaultValues = false;
  val target = new de.tkip.sbpm.application.subject.behavior.Target(id, min, max, createNew, variable, toExternal, defaultValues)
  //}

}
object Target {
  def apply(id: String, min: Int, max: Int, createNew: Boolean, variable: String) = {
    new Target(id, min, max, createNew, Option(variable))
  }
}

abstract class State(val stateType: String, val id: Int, val exitType: String, val target: Target, val targetId: Int) {

  //  var id = -1 //, correlationId: Double
  //  var targetId = -1
  //  var exitType: String = ""
  //  var target: Target = null
  //  var stateType = ""
  //
  //  def apply(_state: String, _id: Int, _exitType: String, _target: Target, _targetId: Int) = new State {
  //    this.id = _id;
  //    this.targetId = _targetId
  //    this.exitType = _exitType
  //    this.target = _target
  //    this.stateType = _state
  //  }

  def process()(implicit actor: ServiceActor)

}

case class ReceiveState(override val stateType: String, override val id: Int,override val exitType: String,override val target: Target,override val targetId: Int) extends State(stateType, id, exitType, target, targetId) {

  def process()(implicit actor: ServiceActor) {
    // do nothing
  }

  def handle(msg: Any)(implicit actor: ServiceActor) {
    actor.storeMsg(msg)
    actor.changeState()
  }
}
case class SendState(override val stateType: String, override val id: Int, override val exitType: String, override val target: Target, override val targetId: Int) extends State(stateType, id, exitType, target, targetId) {
  def process()(implicit actor: ServiceActor) {
    val msg = ""
    send(msg)
    actor.changeState()
  }

  def send(msg: String)(implicit actor: ServiceActor) {

    val messageID = 100 //TODO change if needed

    val messageType = exitType

    val userID = actor.getUserID()
    val processID = actor.getProcessID()
    val subjectID = actor.getSubjectID()
    val sender = actor.getSender()

    val fileInfo = None
    
    val message = SubjectToSubjectMessage(
        messageID,
        processID,
        userID,
        subjectID,
        target.target,
        messageType,
        msg,
        fileInfo)
    
    println("sending message: " + message)   
    println(sender)
    sender ! message
      
  }
}
case class ExitState(override val stateType: String, override val id: Int, override val exitType: String, override val target: Target, override val targetId: Int) extends State(stateType, id, exitType, target, targetId) {

  def process()(implicit actor: ServiceActor) {
    actor.terminate()
  }
}

//case object ReceiveState {
//  def apply(_state: String, _id: Int, _exitType: String, _target: Target, _targetId: Int) = new ReceiveState(_state, _id, _exitType, _target, _targetId)
//}
//
//object SendState {
//  override def apply(_state: String, _id: Int, _exitType: String, _target: Target, _targetId: Int) = new SendState(_state, _id, _exitType, _target, _targetId)
//}
//
//object ExitState {
//  override def apply(_state: String, _id: Int, _exitType: String, _target: Target, _targetId: Int) = new ExitState(_state, _id, _exitType, _target, _targetId)
//}
