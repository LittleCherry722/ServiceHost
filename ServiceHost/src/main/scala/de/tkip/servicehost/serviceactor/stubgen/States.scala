package de.tkip.servicehost.serviceactor.stubgen

import de.tkip.sbpm.application.miscellaneous.{CreateServiceInstance, CreateProcessInstance}
import de.tkip.sbpm.{ActorLocator => BackendActorLocator}
import de.tkip.sbpm.repository.RepositoryPersistenceActor._
import scala.concurrent.duration._
import scala.collection.immutable.List
import de.tkip.servicehost.ActorLocator

import akka.actor.{Props, ActorRef, PoisonPill}
import akka.event.LoggingAdapter
import scala.collection.mutable.{Queue, Map}
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.behavior.state.StateData
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.instrumentation.ClassTraceLogger
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.servicehost._
import scala.concurrent.Await
import de.tkip.sbpm.application.ProcessInstanceActor.MessageContent

class Target(subjectID: SubjectID, min: Int, max: Int, createNew: Boolean, variable: Option[String]) {
  //  def apply(id: Int, min:Int, max:Int, createNew : Boolean, variable: Option[String]){
  val toExternal = false
  val defaultValues = true;
  val target = new de.tkip.sbpm.application.subject.behavior.Target(subjectID, 0, 1, createNew, variable, toExternal, defaultValues)
  //}

}

object Target {
  def apply(id: String, min: Int, max: Int, createNew: Boolean, variable: String) = {
    new Target(id, min, max, createNew, Option(variable))
  }
}

abstract class State(val stateType: String, val id: Int, val exitType: String, val targets: Map[BranchID, Target], val targetIds: Map[BranchID, Int], val text: String, val variableId: String, val correlationId: String) extends ClassTraceLogger {
  def process()(implicit actor: ServiceActor)
}

case class ReceiveState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String, override val correlationId: String) extends State("receive", id, exitType, targets, targetIds, text, variableId, correlationId) {


  def process()(implicit actor: ServiceActor) {
    actor.processMsg(id)
  }

  def handle(msg: Option[Any] = None)(implicit actor: ServiceActor) {
    actor.changeState(id)
  }

}

case class SendState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String, override val correlationId: String) extends State("send", id, exitType, targets, targetIds, text, variableId, correlationId) {
  def process()(implicit actor: ServiceActor) {
    actor.processSendState(id)
    //actor.changeState()
  }

  def getTarget(branchCondition: String): de.tkip.sbpm.application.subject.behavior.Target = {
    if (targets.size > 1) {
      targets(branchCondition).target
    } else targets.head._2.target
  }

}

case class ExitState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String, override val correlationId: String) extends State("exit", id, exitType, targets, targetIds, text, variableId, correlationId) {

  def process()(implicit actor: ServiceActor) {
    actor.reset()
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

case class ActionState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String, override val correlationId: String) extends State("action", id, exitType, targets, targetIds, text, variableId, correlationId) {
  override def process()(implicit actor: ServiceActor) {

  }
}


case class CloseIPState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String, override val correlationId: String) extends State("$closeip", id, exitType, targets, targetIds, text, variableId, correlationId) {

  def process()(implicit actor: ServiceActor): Unit = {
    actor.processCloseIP(id)
  }

}

case class OpenIPState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String, override val correlationId: String) extends State("$openip", id, exitType, targets, targetIds, text, variableId, correlationId) {

  def process()(implicit actor: ServiceActor): Unit = {
    actor.processOpenIP(id)
  }
}

case class ActivateState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String, override val correlationId: String) extends State("$activatestate", id, exitType, targets, targetIds, text, variableId, correlationId) {
  def process()(implicit actor: ServiceActor): Unit = {
    actor.addState(id)
    //actor.isNormalRunning(id)
  }
}

case class DeactivateState(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String, override val correlationId: String) extends State("$deactivatestate", id, exitType, targets, targetIds, text, variableId, correlationId) {
  def process()(implicit actor: ServiceActor): Unit = {
    actor.killState(id)
  }
}