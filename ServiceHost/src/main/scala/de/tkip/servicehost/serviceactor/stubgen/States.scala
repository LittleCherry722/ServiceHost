package de.tkip.servicehost.serviceactor.stubgen

import scala.collection.immutable.List

import akka.actor.PoisonPill
import akka.event.LoggingAdapter

import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.behavior.state.StateData
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.instrumentation.ClassTraceLogger
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.servicehost.serviceactor.ServiceAttributes._

class Target(id: String, min: Int, max: Int, createNew: Boolean, variable: Option[String]) {
  //  def apply(id: Int, min:Int, max:Int, createNew : Boolean, variable: Option[String]){
  val toExternal = false
  val defaultValues = true;
  val target = new de.tkip.sbpm.application.subject.behavior.Target(id, 0, 1, createNew, variable, toExternal, defaultValues)
  //}

}
object Target {
  def apply(id: String, min: Int, max: Int, createNew: Boolean, variable: String) = {
    new Target(id, min, max, createNew, Option(variable))
  }
}

abstract class State(val stateType: String, val id: Int, val exitType: String, val targets: Map[BranchID, Target], val targetIds: Map[BranchID, Int]) extends ClassTraceLogger {

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

case class ReceiveState(override val id: Int,override val exitType: String,override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int])extends State("receive", id, exitType, targets, targetIds) {
  
  
  def process()(implicit actor: ServiceActor) {
    actor.processMsg()
    // do nothing
  }

  def handle(msg: Any)(implicit actor: ServiceActor) {
//    actor.storeMsg(msg)
    actor.changeState()
  }
}
case class SendState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int]) extends State("send", id, exitType, targets, targetIds) {
  def process()(implicit actor: ServiceActor) {
    val msg = actor.getMessage()
    send(msg)
    actor.changeState()
  }

  def send(msg: String)(implicit actor: ServiceActor) {

    val messageID = 100 //TODO change if needed
    val messageType = targetIds.head._1 
    val userID = actor.getUserID()
    val processID = actor.getProcessID()
    val subjectID = actor.getSubjectID()
    val sender = actor.getDestination()
    val fileInfo = None
    val target = getTarget(actor.getBranchCondition)
    target.insertTargetUsers(Array(1))

    val message = SubjectToSubjectMessage(
        messageID,
        processID,
        1,
        subjectID,
        target,
        messageType,      //messageType,
        msg,
        fileInfo)
    
    //log.debug("sending message: " + message + " to " + sender)
    sender !! message
  }
  
  def getTarget(branchCondition: String): de.tkip.sbpm.application.subject.behavior.Target = {
    if (targets.size > 1) {
      targets(branchCondition).target
    } else targets.head._2.target
  }
}
case class ExitState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int]) extends State("exit", id, exitType, targets, targetIds) {

  def process()(implicit actor: ServiceActor) {
    actor.terminate()
  }
}
//
//object SendState {
//  override def apply(_state: String, _id: Int, _exitType: String, _target: Target, _targetId: Int) = new SendState(_state, _id, _exitType, _target, _targetId)
//}
//
//object ExitState {
//  override def apply(_state: String, _id: Int, _exitType: String, _target: Target, _targetId: Int) = new ExitState(_state, _id, _exitType, _target, _targetId)
//}
