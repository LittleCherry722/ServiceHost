package de.tkip.sbpm.application.miscellaneous

//import de.tkip.sbpm.application.miscellaneous._
import scala.collection.mutable.ArrayBuffer

import akka.actor._
import akka.pattern.ask
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await
import akka.dispatch.Future

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

/**
 * models the behavior through linking certain ConcreteBehaviorStates and executing them
 */
abstract class BehaviourState(val stateID: StateID,
                              val stateAction: StateAction,
                              transitions: Array[Transition]) {

  for (i <- 0 until transitions.length) {
    if (transitions(i).successorID.isEmpty()) {
      transitions(i) =
        Transition(
          transitions(i).messageType,
          transitions(i).subjectName,
          stateID + ".br" + (i + 1).toString())
    }
  } // br for branch 

  private def build_transitionsMap(): Map[ExitCond, SuccessorID] = {
    val transitionsMap = collection.mutable.Map[ExitCond, SuccessorID]()
    for (i <- 0 until transitions.length) {
      transitionsMap += transitions(i).exitCond -> transitions(i).successorID
    }
    return transitionsMap.toMap
  }

  private val _transitionsMap: Map[ExitCond, SuccessorID] = build_transitionsMap
  def transitionsMap: Map[ExitCond, SuccessorID] = _transitionsMap

  def exitConds: Array[ExitCond] = for (t <- transitions) yield (t.exitCond)

  def performAction(processManager: ProcessManagerRef,
                    subjectName: SubjectName,
                    subjectProviderName: SubjectName,
                    inputpool: ActorRef): StateID

}

case class ActState(id: StateID, action: StateAction, transitions: Array[Transition])
    extends BehaviourState(id, action, transitions) { // ActState = ActionState

  def performAction(processManager: ProcessManagerRef,
                    subjectName: SubjectName,
                    subjectProviderName: SubjectName,
                    inputpool: ActorRef): StateID = {
    var action_choices: String = ""
    for (t <- transitions) action_choices += t.messageType + "\\"

    if (transitions.length == 1) {
      if (transitions(0).exitCond.messageType.equals("Done")) {
        println("Action@" + subjectProviderName + ": " + stateAction + " is done.")
        return transitionsMap(ExitCond("Done", "Do"))
      }
    }

    var output: String = "Action@" + subjectProviderName + ": " + stateAction +
      "\nWhat is the result ?\n(" + action_choices + ")?\n> "
    var input = readLine(output)

    while (!validat_input(input)) {
      println("Invalid input. Please enter one term of the selection:\n" +
        action_choices.toString())
      input = readLine(output)
    }

    def validat_input(input: String): Boolean = {
      for (t <- transitions) if (t.messageType.equals(input)) return true
      false
    }
    return transitionsMap(ExitCond(input, "Do"))
  }

}

case class EndState(id: StateID) extends BehaviourState(id, "End of Behavior: ", Array[Transition]()) {
  def performAction(processManager: ProcessManagerRef,
                    subjectName: SubjectName,
                    subjectProviderName: SubjectName,
                    inputpool: ActorRef): StateID = {
    println("End@" + subjectProviderName)
    null
  }
}

case class ReceiveState(s: StateID, val transitions: Array[Transition]) extends BehaviourState(s, "ReceiveAction", transitions) {

  override def performAction(processManager: ProcessManagerRef,
                             subjectName: SubjectName,
                             subjectProviderName: SubjectName,
                             inputpool: ActorRef): StateID = {

    var ret: StateID = "Default Receive return"

    implicit val timeout = Timeout(365 days)
    val future = inputpool.ask(RequestForMessages(exitConds))

    val ack = Await.result(future, timeout.duration)

    ack match {
      case tm: TransportMessage =>
        println("Receive@" + subjectProviderName + ": Message \"" +
          tm.fromCond.messageType + "\" from \"" + tm.fromCond.subjectName +
          "\" with content \"" + tm.messageContent + "\"")
        ret = transitionsMap(tm.fromCond)
      case ss =>
        println("Receive@ got something: " + ss)
        ret = "Timeout"
    }

    return ret
  }

}

case class SendState(s: StateID, transitions: Array[Transition]) extends BehaviourState(s, "SendAction", transitions) {

  def performAction(processManager: ProcessManagerRef,
                    fromSubject: SubjectName,
                    subjectProviderName: SubjectName,
                    inputpool: ActorRef): StateID = {

    val messageName = transitions(0).messageType
    val toSubject = transitions(0).subjectName
    val exitCond = transitions(0).exitCond

    //val messageContent = readLine("Send@" + toSubject + ": type in the content of the message " + messageName + " that will be send to the subject " + toSubject +"\n" )
    val messageContent = "The huge MessageContent" // for testruns

    var ret: StateID = "Default Send return"

    implicit val timeout = Timeout(365 days) // has to be adapted for timeout edges 
    val future =
      processManager.ask(
        SubjectMessage(
          ExitCond(exitConds(0).messageType, fromSubject),
          exitConds(0),
          messageContent))

    val ack = Await.result(future, timeout.duration)

    ack match {
      case Stored =>
        println("Send@" + subjectProviderName + ": \"" + messageName +
          "\" to \"" + toSubject + "\"")
        ret = transitionsMap(exitCond)
      case ss =>
        println("Send@ got something: " + ss)
        ret = "Timeout"
    }

    return ret
  }
}