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
import scala.collection.mutable.ArrayBuffer

case class ActionExecuted(ea: ExecuteAction)
case class SubjectStarted(userID: UserID)

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
  protected def getAvailableAction: (StateType, Array[ActionData])

  protected def createAvailableAction(processInstanceID: ProcessInstanceID) = {
    val (stateType, actionData) = getAvailableAction
    AvailableAction(
      userID,
      processInstanceID,
      subjectID,
      id,
      stateName,
      StateType.fromStateTypetoString(stateType),
      actionData)
  }

  def receive = {
    case ga: GetAvailableAction => {
      sender ! createAvailableAction(ga.processInstanceID)
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
      processInstanceActor ! SubjectStarted(userID)
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def getAvailableAction: (StateType, Array[ActionData]) =
    (StartStateType, Array())
}

protected case class EndStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  // TODO direct beenden?
  internalBehaviorActor ! SubjectTerminated(userID, subjectID)

  override protected def getAvailableAction: (StateType, Array[ActionData]) =
    (EndStateType, Array())
}

protected case class ActStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  override def receive = {

    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, ActStateString, input) => {
      val index = indexOfInput(input.text)
      if (index != -1) {
        internalBehaviorActor ! ChangeState(stateID, transitions(index).successorID, null)

        processInstanceActor ! ActionExecuted(ea)
      } else {
        // invalid input
        processInstanceActor ! ActionExecuted(ea)
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
  //TODO messageType =! label
  override protected def getAvailableAction: (StateType, Array[ActionData]) =
    //		  (ActStateType, transitions.map(_.messageType))
    (ActStateType,
      transitions.map((t: Transition) => ActionData(t.messageType, true)))
}

protected case class ReceiveStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  private case class TransitionMeta(
      from: SubjectID,
      messageType: MessageType,
      successorID: StateID,
      var messageID: MessageID = -1,
      var messageContent: Option[MessageContent] = None) {
    var ready = false
    def apply(message: TransportMessage) {
      ready = true
      messageID = message.messageID
      messageContent = Some(message.messageContent)
      // TODO validatecheck
    }
  }

  private val meta: Map[(SubjectID, MessageType), TransitionMeta] =
    transitions.map((t: Transition) =>
      ((t.subjectName, t.messageType), TransitionMeta(t.subjectName, t.messageType, t.successorID)))
      .toMap[(SubjectID, MessageType), TransitionMeta]

  // request if there is a message for this subject
  inputPoolActor !
    RequestForMessages(transitions.map(convertTransitionToRequest(_)))

  override def receive = {
    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, ReceiveStateString, input) => {
      if (!transitions.isEmpty) {
        // TODO richtige metatatransition, TODO correspontingsubject isDefined
        val transition = meta((input.correspondingSubject.get, input.text)) // TODO name
        val message = HistoryMessage(transition.messageID, transition.messageType, transition.from, subjectID, transition.messageContent.get)
        internalBehaviorActor ! ChangeState(id, transition.successorID, message)

        processInstanceActor ! ActionExecuted(ea)
      }
    }

    case sm: TransportMessage => {
      // TODO checken ob richtige message
      logger.debug("Receive@" + userID + "/" + subjectID + ": Message \"" +
        sm.messageType + "\" from \"" + sm.from +
        "\" with content \"" + sm.messageContent + "\"")
      meta(sm.from, sm.messageType)(sm)
    }

    case s => {
      super.receive(s)
    }
  }

  private def convertTransitionToRequest(transition: Transition) =
    SubjectMessageRouting(
      transition.subjectName,
      transition.messageType)

  // TODO label richtig, gedanken machen
  override protected def getAvailableAction: (StateType, Array[ActionData]) = {
    //    	  (stateType, Array(messageContent))TODO
    (ReceiveStateType,
      (for ((k, t) <- meta) yield {
        ActionData(
          t.messageType,
          t.ready,
          correspondingSubject = Some(t.from),
          messageContent = t.messageContent)
      }).toArray)
  }
}

protected case class SendStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  import scala.collection.mutable.{ Map => MutableMap }
  var messageData: Option[String] = None
  val unsentMessageIDs: MutableMap[MessageID, Transition] = MutableMap[MessageID, Transition]()

  // TODO sowas wie timeout ist nicht drin
  // TODO message ids
  override def receive = {
    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, SendStateString, input) => {
      // send subjectInternalMessage before sending executionAnswer to make sure that the executionAnswer 
      // can be blocked until a potentially new subject is created to ensure all available actions will 
      // be returned when asking
      messageData = input.messageContent
      for (transition <- transitions) {
        if (messageData.isDefined) {
          val messageType = transition.messageType
          val toSubject = transition.subjectName
          val messageID = nextMessageID
          unsentMessageIDs(messageID) = transition
          processInstanceActor !
            SubjectInternalMessage(
              messageID,
              userID,
              subjectID,
              toSubject,
              messageType,
              messageData.get)
        }

        processInstanceActor ! ActionExecuted(ea)
      }
    }

    // TODO stored vielleicht besser spezifizieren
    case Stored(messageID) if (unsentMessageIDs.contains(messageID)) => {
      // create the History Entry
      if (messageData.isDefined) {
        val transition = unsentMessageIDs(messageID)
        internalBehaviorActor !
          ChangeState(id, transition.successorID,
            HistoryMessage(messageID, transition.messageType, subjectID, transition.subjectName, messageData.get))
      } else {
        // TODO was tun?
        logger.error("Stored received before sending the message")
      }
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def getAvailableAction: (StateType, Array[ActionData]) =
    (SendStateType,
      transitions.map((t: Transition) =>
        ActionData(
          t.messageType,
          !messageData.isDefined,
          correspondingSubject = Some(t.subjectName))))

  /**
   * Returns and adds a new message ID for the next message
   */
  private def nextMessageID: Int = {
    scala.util.Random.nextInt
  }
}
