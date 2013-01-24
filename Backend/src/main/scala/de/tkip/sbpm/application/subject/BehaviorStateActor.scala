package de.tkip.sbpm.application.subject

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.Array.canBuildFrom
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._

/**
 * models the behavior through linking certain ConcreteBehaviorStates and executing them
 */
protected abstract class BehaviorStateActor(val stateID: StateID,
                                            val stateAction: StateAction,
                                            transitions: Array[Transition],
                                            internalBehavior: InternalBehaviorRef,
                                            processInstance: ProcessInstanceRef,
                                            subjectID: SubjectID,
                                            userID: UserID, // TODO braucht ma, 
                                            inputpool: ActorRef) extends Actor {

  //  for (i <- 0 until transitions.length) yield {
  //    if (transitions(i).successorID.isEmpty()) {
  //      transitions(i) =
  //        Transition(
  //          transitions(i).messageType,
  //          transitions(i).subjectName,
  //          stateID + ".br" + (i + 1).toString())
  //    }
  //  } // br for branch 

  /**
   *
   * @return
   *  (String, Array[String])
   * (StateType, Actions),
   * e.g.
   * ("Act", ["Approval", "Denial"]),
   * ("Send", []),
   * ("Receive", ["The huge Message Content"])
   */
  protected def getAvailableAction: (StateType, Array[String])

  def receive = {
    case ga: GetAvailableAction => {
      val (stateType, actionData) = getAvailableAction
      sender !
        AvailableAction(userID,
          ga.processInstanceID,
          subjectID,
          stateID,
          StateType.fromStateTypetoString(stateType),
          actionData)
    }

    case action: ExecuteAction => {
      println("/" + userID + "/" + subjectID + "/" + stateID + " does not support " + action)
    }

    case s => {
      println("BehaviorStateActor does not support: " + s)
    }
  }
}

protected case class StartStateActor(id: StateID,
                                     transition: Transition,
                                     internalBehavior: InternalBehaviorRef,
                                     processInstance: ProcessInstanceRef,
                                     subjectID: SubjectID,
                                     userID: UserID,
                                     inputpool: ActorRef)
    extends BehaviorStateActor(id,
      "Start of Behavior",
      Array[Transition](transition),
      internalBehavior,
      processInstance,
      subjectID,
      userID,
      inputpool) {

  println("Start@" + userID + ", " + subjectID)

  override def receive = {
    case ea: StartSubjectExecution => {
      internalBehavior ! NextState(transition.successorID)
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def getAvailableAction: (StateType, Array[String]) =
    (StartStateType, Array())
}

protected case class EndStateActor(id: StateID,
                                   internalBehavior: InternalBehaviorRef,
                                   processInstance: ProcessInstanceRef,
                                   subjectID: SubjectID,
                                   userID: UserID,
                                   inputpool: ActorRef)
    extends BehaviorStateActor(id,
      "End of Behavior: ",
      Array[Transition](),
      internalBehavior,
      processInstance,
      subjectID,
      userID,
      inputpool) {

  // TODO direct beenden?
  internalBehavior ! SubjectTerminated(userID, subjectID)

  override protected def getAvailableAction: (StateType, Array[String]) =
    (EndStateType, Array())
}

protected case class ActStateActor(id: StateID,
                                   action: StateAction,
                                   transitions: Array[Transition],
                                   internalBehavior: InternalBehaviorRef,
                                   processInstance: ProcessInstanceRef,
                                   subjectID: SubjectID,
                                   userID: UserID,
                                   inputpool: ActorRef)
    extends BehaviorStateActor(id,
      action,
      transitions,
      internalBehavior,
      processInstance,
      subjectID,
      userID,
      inputpool) { // ActState = ActionState

  override def receive = {

    case ea: ExecuteAction => {
      val index = indexOfInput(ea.actionInput)
      if (index != -1) {
        sender ! ExecuteActionAnswer(ea)
        internalBehavior ! NextState(transitions(index).successorID)
      } else {
        // invalid input
        sender ! ExecuteActionAnswer(ea)
      }
    }

    case s => {
      super.receive(s)
    }
  }

  private def indexOfInput(input: String): Int = {
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

protected case class ReceiveStateActor(s: StateID,
                                       transitions: Array[Transition],
                                       internalBehavior: InternalBehaviorRef,
                                       processInstance: ProcessInstanceRef,
                                       subjectID: SubjectID,
                                       userID: UserID,
                                       inputpool: ActorRef)
    extends BehaviorStateActor(s,
      "ReceiveAction",
      transitions,
      internalBehavior,
      processInstance,
      subjectID,
      userID,
      inputpool) {

  var messageContent: String = ""
  var stateType: StateType = ReceiveWaitingStateType

  // request if there is a message for this subject
  inputpool !
    RequestForMessages(transitions.map(convertTransitionToRequest(_)))

  override def receive = {
    case ea: ExecuteAction => {
      if (!transitions.isEmpty) {
        sender ! ExecuteActionAnswer(ea)
        internalBehavior ! NextState(transitions(0).successorID)
      }
    }

    case sm: TransportMessage => {
      // TODO checken ob richtige message
      println("Receive@" + userID + ": Message \"" +
        sm.messageType + "\" from \"" + sm.from +
        "\" with content \"" + sm.messageContent + "\"")
      messageContent = sm.messageContent
      stateType = ReceiveStateType
    }

    case s => {
      super.receive(s)
    }
  }

  private def convertTransitionToRequest(transition: Transition) =
    SubjectMessageRouting(transition.subjectName,
      transition.messageType)

  override protected def getAvailableAction: (StateType, Array[String]) = {
    (stateType, Array(messageContent))
  }
}

protected case class SendStateActor(s: StateID,
                                    transitions: Array[Transition],
                                    internalBehavior: InternalBehaviorRef,
                                    processInstance: ProcessInstanceRef,
                                    subjectID: SubjectID,
                                    userID: UserID,
                                    inputpool: ActorRef)
    extends BehaviorStateActor(s,
      "SendAction",
      transitions,
      internalBehavior,
      processInstance,
      subjectID,
      userID,
      inputpool) {

  // TODO mehrere nachrichten gleichzeitig?
  val messageName = transitions(0).messageType
  val toSubject = transitions(0).subjectName
  val sucessorID = transitions(0).successorID

  // TODO sowas wie timeout ist nicht drin
  override def receive = {
    case ea: ExecuteAction => {
      sender ! ExecuteActionAnswer(ea)
      processInstance !
        SubjectInternalMessage(subjectID,
          toSubject,
          messageName,
          ea.actionInput)
    }

    // TODO stored vielleicht besser spezifizieren
    case Stored => {
      internalBehavior ! NextState(sucessorID)
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def getAvailableAction: (StateType, Array[String]) =
    (SendStateType, Array())
}
