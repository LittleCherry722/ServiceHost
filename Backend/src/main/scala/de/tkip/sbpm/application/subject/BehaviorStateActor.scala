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
import de.tkip.sbpm.application.history.{
  Transition => HistoryTransition,
  Message => HistoryMessage,
  State => HistoryState
}
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._

/**
 * models the behavior through linking certain ConcreteBehaviorStates and executing them
 */
protected abstract class BehaviorStateActor(val stateID: StateID,
                                            val stateAction: StateAction,
                                            transitions: Array[Transition],
                                            internalBehaviorActor: InternalBehaviorRef,
                                            processInstance: ProcessInstanceRef,
                                            subjectID: SubjectID,
                                            userID: UserID, // TODO braucht ma, 
                                            inputPoolActor: ActorRef) extends Actor {
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
                                     internalBehaviorActor: InternalBehaviorRef,
                                     processInstance: ProcessInstanceRef,
                                     subjectID: SubjectID,
                                     userID: UserID,
                                     inputPoolActor: ActorRef)
    extends BehaviorStateActor(id,
      "Start of Behavior",
      Array[Transition](transition),
      internalBehaviorActor,
      processInstance,
      subjectID,
      userID,
      inputPoolActor) {

  println("Start@" + userID + ", " + subjectID)

  override def receive = {
    case ea: StartSubjectExecution => {
      internalBehaviorActor ! NextState(transition.successorID)
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def getAvailableAction: (StateType, Array[String]) =
    (StartStateType, Array())
}

protected case class EndStateActor(id: StateID,
                                   internalBehaviorActor: InternalBehaviorRef,
                                   processInstance: ProcessInstanceRef,
                                   subjectID: SubjectID,
                                   userID: UserID,
                                   inputPoolActor: ActorRef)
    extends BehaviorStateActor(id,
      "End of Behavior: ",
      Array[Transition](),
      internalBehaviorActor,
      processInstance,
      subjectID,
      userID,
      inputPoolActor) {

  // TODO direct beenden?
  internalBehaviorActor ! SubjectTerminated(userID, subjectID)

  override protected def getAvailableAction: (StateType, Array[String]) =
    (EndStateType, Array())
}

protected case class ActStateActor(id: StateID,
                                   action: StateAction,
                                   transitions: Array[Transition],
                                   internalBehaviorActor: InternalBehaviorRef,
                                   processInstance: ProcessInstanceRef,
                                   subjectID: SubjectID,
                                   userID: UserID,
                                   inputPoolActor: ActorRef)
    extends BehaviorStateActor(id,
      action,
      transitions,
      internalBehaviorActor,
      processInstance,
      subjectID,
      userID,
      inputPoolActor) { // ActState = ActionState

  override def receive = {

    case ea: ExecuteAction => {
      val index = indexOfInput(ea.actionInput)
      if (index != -1) {
        sender ! ExecuteActionAnswer(ea)
        //        internalBehaviorActor ! NextState(transitions(index).successorID)

        internalBehaviorActor ! ChangeState(id, transitions(index).successorID, null)
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

protected case class ReceiveStateActor(id: StateID,
                                       transitions: Array[Transition],
                                       internalBehaviorActor: InternalBehaviorRef,
                                       processInstance: ProcessInstanceRef,
                                       subjectID: SubjectID,
                                       userID: UserID,
                                       inputPoolActor: ActorRef)
    extends BehaviorStateActor(id,
      "ReceiveAction",
      transitions,
      internalBehaviorActor,
      processInstance,
      subjectID,
      userID,
      inputPoolActor) {

  var messageContent: String = ""
  var stateType: StateType = ReceiveWaitingStateType

  // request if there is a message for this subject
  inputPoolActor !
    RequestForMessages(transitions.map(convertTransitionToRequest(_)))

  override def receive = {
    case ea: ExecuteAction => {
      if (!transitions.isEmpty) {
        sender ! ExecuteActionAnswer(ea)

        val message = HistoryMessage(-1, transitions(0).messageType, transitions(0).subjectName, subjectID, messageContent)
        //        internalBehaviorActor ! NextState(transitions(0).successorID)
        internalBehaviorActor ! ChangeState(id, transitions(0).successorID, message)
      }
    }

    case sm: TransportMessage => {
      // TODO checken ob richtige message
      println("Receive@" + userID + "/" + subjectID + ": Message \"" +
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

protected case class SendStateActor(id: StateID,
                                    transitions: Array[Transition],
                                    internalBehaviorActor: InternalBehaviorRef,
                                    processInstance: ProcessInstanceRef,
                                    subjectID: SubjectID,
                                    userID: UserID,
                                    inputPoolActor: ActorRef)
    extends BehaviorStateActor(id,
      "SendAction",
      transitions,
      internalBehaviorActor,
      processInstance,
      subjectID,
      userID,
      inputPoolActor) {

  // TODO mehrere nachrichten gleichzeitig?
  val messageType = transitions(0).messageType
  val toSubject = transitions(0).subjectName
  val sucessorID = transitions(0).successorID

  var messageData: String = null

  // TODO sowas wie timeout ist nicht drin
  override def receive = {
    case ea: ExecuteAction => {
      sender ! ExecuteActionAnswer(ea)
      messageData = ea.actionInput
      processInstance !
        SubjectInternalMessage(subjectID,
          toSubject,
          messageType,
          messageData)
    }

    // TODO stored vielleicht besser spezifizieren
    case Stored => {
      // create the History Entry
      if (messageData == null) {
        // TODO was tun?
        System.err.println("Stored received before sending the message")
      }
      //      val message =
      //        HistoryMessage(-1, messageName, subjectID, toSubject, messageData)
      //        internalBehaviorActor ! NextState(sucessorID)
      internalBehaviorActor !
        ChangeState(id, sucessorID,
          HistoryMessage(-1, messageType, subjectID, toSubject, messageData))
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def getAvailableAction: (StateType, Array[String]) =
    (SendStateType, Array())
}
