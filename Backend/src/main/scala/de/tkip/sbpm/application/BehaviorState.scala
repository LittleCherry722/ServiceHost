package de.tkip.sbpm.application

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.Array.canBuildFrom
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.subject._

/**
 * models the behavior through linking certain ConcreteBehaviorStates and executing them
 */
abstract class BehaviorStateActor(val stateID: StateID,
  val stateAction: StateAction,
  transitions: Array[Transition],
  internalBehavior: InternalBehaviorRef,
  processInstance: ProcessInstanceRef,
  subjectID: SubjectID,
  userID: UserID, // TODO braucht ma, 
  inputpool: ActorRef) extends Actor {

  for (i <- 0 until transitions.length) yield {
    if (transitions(i).successorID.isEmpty()) {
      transitions(i) =
        Transition(
          transitions(i).messageType,
          transitions(i).subjectName,
          stateID + ".br" + (i + 1).toString())
    }
  } // br for branch 

  /**
   *
   * @return
   *  (String, Array[String])
   * (StateType, Actions), e.g. ("Act", ["Approval", "Denial"]), ("Send", []), ("Receive", ["The huge Message Content"])
   */
  protected def getAvailableAction: (StateType, Array[String])

  def receive = {
    case ga: GetAvailableActions =>
      val (stateType, actionData) = getAvailableAction
      sender !
        AvailableAction(userID,
          ga.processInstanceID,
          subjectID,
          stateID,
          stateType,
          actionData)
    case s => println("BehaviorStateActor does not support: " + s)
  }
}

case class StartState(id: StateID,
  transition: Transition,
  internalBehavior: InternalBehaviorRef,
  processInstance: ProcessInstanceRef,
  subjectName: SubjectName,
  userID: UserID,
  inputpool: ActorRef)
  extends BehaviorStateActor(id,
    "Start of Behavior",
    Array[Transition](transition),
    internalBehavior,
    processInstance,
    subjectName,
    userID,
    inputpool) {

  println("Start@" + userID)

  override def receive = {

    case es: ExecuteState =>
      internalBehavior ! ExecuteState(transition.successorID)

    case ea: ExecuteAction =>
      internalBehavior ! ActionExecuted(transition.successorID)

    case s => super.receive(s)

  }

  override protected def getAvailableAction: (StateType, Array[String]) = (StartStateType, Array())

}

case class EndState(id: StateID,
  internalBehavior: InternalBehaviorRef,
  processInstance: ProcessInstanceRef,
  subjectName: SubjectName,
  userID: UserID,
  inputpool: ActorRef)
  extends BehaviorStateActor(id,
    "End of Behavior: ",
    Array[Transition](),
    internalBehavior,
    processInstance,
    subjectName,
    userID,
    inputpool) {

  override def receive = {
    case es: ExecuteState =>
      println("End@" + userID)

    case ea: ExecuteAction =>
      println("End@" + userID)

    case s => super.receive(s)

  }

  override protected def getAvailableAction: (StateType, Array[String]) = (EndStateType, Array())
}

case class ActState(id: StateID,
  action: StateAction,
  transitions: Array[Transition],
  internalBehavior: InternalBehaviorRef,
  processInstance: ProcessInstanceRef,
  subjectName: SubjectName,
  userID: UserID,
  inputpool: ActorRef)
  extends BehaviorStateActor(id,
    action,
    transitions,
    internalBehavior,
    processInstance,
    subjectName,
    userID,
    inputpool) { // ActState = ActionState

  override def receive = {

    case es: ExecuteState =>
        processDebuggingMessage
        internalBehavior ! new ExecuteState(transitions(index).successorID) with Debug

    case ea: ExecuteAction =>
      index = indexOfInput(ea.actionInput)
      if (index != -1) {
        internalBehavior ! ActionExecuted(transitions(index).successorID)
      }

    case s => super.receive(s)
  }

  var index = -1
  private def processDebuggingMessage {
    var actionChoices: String = ""
    for (t <- transitions) {
      actionChoices += t.messageType + "\\"
    }

    var output: String = "Action@" + userID + ": " + stateAction +
      "\nWhat is the result ?\n(" + actionChoices + ")?\n> "

    // TODO Hier Userinteraktion: nach Aktion fragen
    var input = readLine(output)

    index = -1
    index = indexOfInput(input)
    while (index == -1) {
      println("Invalid input. Please enter one term of the selection:\n" +
        actionChoices.toString())
      // userinteraktion, waere aber fehler im programm
      input = readLine(output)
      index = indexOfInput(input)
    }

  }

  def indexOfInput(input: String): Int = {
    var i = 0
    for (t <- transitions) {
      if (t.messageType.equals(input)) {
        return i
      }
      i += 1
    }
    -1
  }

  override protected def getAvailableAction: (StateType, Array[String]) =
    (ActStateType, transitions.map(_.messageType))
}

case class ReceiveState(s: StateID,
  transitions: Array[Transition],
  internalBehavior: InternalBehaviorRef,
  processInstance: ProcessInstanceRef,
  subjectName: SubjectName,
  userID: UserID,
  inputpool: ActorRef)
  extends BehaviorStateActor(s,
    "ReceiveAction",
    transitions,
    internalBehavior,
    processInstance,
    subjectName,
    userID,
    inputpool) {
  override def receive = {

    case es: ExecuteState =>
        processDebuggingMessage()
        internalBehavior ! new ExecuteState(ret) with Debug
        
    case ea: ActionExecuted =>
      if (!transitions.isEmpty) {
          checkInputPoolForWaitingMessages()
          internalBehavior ! ActionExecuted(transitions(0).successorID)
        }
      

    case s => super.receive(s)
  }

  var ret: StateID = "Default Receive return"
  var messageContent: String = null

  private def processDebuggingMessage() {
    checkInputPoolForWaitingMessages()

    var input = readLine("")
    // TODO Hier Userinteraktion: Nachricht anzeigen (und auf ok warten)
  }

  // if no message has been found so far -> ask inputpool if a message is waiting
  private def checkInputPoolForWaitingMessages() {
    if (messageContent == null) {
      implicit val timeout = Timeout(365 days)
      val future =
        inputpool.ask(
          RequestForMessages(
            transitions.map(
              convertTransitionToRequest(_))))

      val ack = Await.result(future, timeout.duration)

      ack match {
        case sm: TransportMessage =>
          println("Receive@" + userID + ": Message \"" +
            sm.messageType + "\" from \"" + sm.from +
            "\" with content \"" + sm.messageContent + "\"")
          // TODO richtigen index
          messageContent = sm.messageContent
          ret = transitions(0).successorID
        case ss =>
          println("Receive@ got something: " + ss)
          ret = "Timeout"
      }
    }
  }

  // TODO subjectname muesste ja bekannt sein koennen
  private def convertTransitionToRequest(transition: Transition) =
    SubjectMessageRouting(transition.subjectName,
      transition.messageType)

  // TODO input muss dann richtig sein 
  override protected def getAvailableAction: (StateType, Array[String]) =
    (ReceiveStateType, Array(messageContent))
}

case class SendState(s: StateID,
  transitions: Array[Transition],
  internalBehavior: InternalBehaviorRef,
  processInstance: ProcessInstanceRef,
  subjectName: SubjectName,
  userID: UserID,
  inputpool: ActorRef)
  extends BehaviorStateActor(s,
    "SendAction",
    transitions,
    internalBehavior,
    processInstance,
    subjectName,
    userID,
    inputpool) {
  override def receive = {

    case es: ExecuteState =>
        processDebuggingMessage()
        internalBehavior ! new ExecuteState(ret) with Debug
      

    case ea: ExecuteAction =>
      sendMessageToInputPool(ea.actionInput, false)
      internalBehavior ! ActionExecuted(ret)

    case s => super.receive(s)
  }

  // TODO mehrere nachrichten gleichzeitig?
  val messageName = transitions(0).messageType
  val toSubject = transitions(0).subjectName
  //    val exitCond = transitions(0).exitCond 
  val sucessorID = transitions(0).successorID

  var ret: StateID = "Default Send return"

  private def processDebuggingMessage() {
    // TODO Hier Userinteraktion: nach Nachricht fragen
    var messageContent = readLine("Send@" + toSubject + ": type in the content of the message " + messageName + " that will be send to the subject " + toSubject + "\n")
    //    val messageContent = "The huge MessageContent" // for testruns

    sendMessageToInputPool(messageContent, true)
  }

  private def sendMessageToInputPool(messageContent: String, isDebugMessage: Boolean) {
    implicit val timeout = Timeout(365 days) // has to be adapted for timeout edges 
    val debugMessage = new SubjectMessage(
      subjectName,
      toSubject,
      messageName,
      messageContent) with Debug

    val message = new SubjectMessage(
      subjectName,
      toSubject,
      messageName,
      messageContent)

    val future =
      processInstance.ask(
        if (isDebugMessage)
          debugMessage
        else
          message)

    val ack = Await.result(future, timeout.duration)

    ack match {
      case Stored =>
        println("Send@" + userID + ": \"" + messageName +
          "\" to \"" + toSubject + "\"")
        ret = sucessorID
      case ss =>
        println("Send@ got something: " + ss)
        ret = "Timeout"

    }
  }

  override protected def getAvailableAction: (StateType, Array[String]) =
    (SendStateType, Array())
}
