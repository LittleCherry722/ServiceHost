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
import akka.event.Logging

protected case class StateData(userID: UserID,
                               subjectID: SubjectID,
                               stateID: StateID,
                               stateName: String,
                               transitions: Array[Transition],
                               internalBehaviorActor: InternalBehaviorRef,
                               processInstanceActor: ProcessInstanceRef,
                               inputPoolActor: ActorRef)

/**
 * models the behavior through linking certain ConcreteBehaviorStates and executing them
 */
protected abstract class BehaviorStateActor(data: StateData) extends Actor {

  val logger = Logging(context.system, this)

  val id = data.stateID
  val userID = data.userID
  val subjectID = data.subjectID
  val stateName = data.stateName
  val transitions = data.transitions
  val internalBehaviorActor = data.internalBehaviorActor
  val processInstanceActor = data.processInstanceActor
  val inputPoolActor = data.inputPoolActor

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
        AvailableAction(
          userID,
          ga.processInstanceID,
          subjectID,
          id,
          stateName,
          StateType.fromStateTypetoString(stateType),
          actionData)
    }

    case action: ExecuteAction => {
      logger.error("/" + userID + "/" + subjectID + "/" +
        id + " does not support " + action)
    }

    case s => {
      logger.error("BehaviorStateActor does not support: " + s)
    }
  }
}

protected case class StartStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  logger.debug("Start@" + userID + ", " + subjectID)

  override def receive = {
    case ea: StartSubjectExecution => {
      internalBehaviorActor ! NextState(transitions(0).successorID)
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def getAvailableAction: (StateType, Array[String]) =
    (StartStateType, Array())
}

protected case class EndStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  // TODO direct beenden?
  internalBehaviorActor ! SubjectTerminated(userID, subjectID)

  override protected def getAvailableAction: (StateType, Array[String]) =
    (EndStateType, Array())
}

protected case class ActStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  override def receive = {

    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, ActStateString, input) => {
      val index = indexOfInput(input)
      if (index != -1) {
        sender ! ExecuteActionAnswer(ea)

        internalBehaviorActor ! ChangeState(stateID, transitions(index).successorID, null)
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

protected case class ReceiveStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  var messageContent: String = ""
  var stateType: StateType = WaitingStateType

  // request if there is a message for this subject
  inputPoolActor !
    RequestForMessages(transitions.map(convertTransitionToRequest(_)))

  override def receive = {
    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, ReceiveStateString, input) => {
      if (!transitions.isEmpty) {
        sender ! ExecuteActionAnswer(ea)

        val message = HistoryMessage(-1, transitions(0).messageType, transitions(0).subjectName, subjectID, messageContent)
        internalBehaviorActor ! ChangeState(id, transitions(0).successorID, message)
      }
    }

    case sm: TransportMessage => {
      // TODO checken ob richtige message
      logger.debug("Receive@" + userID + "/" + subjectID + ": Message \"" +
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

protected case class SendStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  // TODO mehrere nachrichten gleichzeitig?
  val messageType = transitions(0).messageType
  val toSubject = transitions(0).subjectName
  val sucessorID = transitions(0).successorID

  var messageData: String = null

  // TODO sowas wie timeout ist nicht drin
  override def receive = {
    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, SendStateString, input) => {
      sender ! ExecuteActionAnswer(ea)
      messageData = input
      processInstanceActor !
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
        logger.error("Stored received before sending the message")
      }
      internalBehaviorActor !
        ChangeState(id, sucessorID,
          HistoryMessage(-1, messageType, subjectID, toSubject, messageData))
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def getAvailableAction: (StateType, Array[String]) =
    (if (messageData == null) SendStateType else WaitingStateType, Array())
}
