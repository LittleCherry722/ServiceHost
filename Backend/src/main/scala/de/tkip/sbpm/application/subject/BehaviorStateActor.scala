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
case class SubjectStarted(userID: UserID, subjectID: SubjectID)

protected case class StateData(stateModel: State,
                               userID: UserID,
                               subjectID: SubjectID,
                               internalBehaviorActor: InternalBehaviorRef,
                               processInstanceActor: ProcessInstanceRef,
                               inputPoolActor: ActorRef)

/**
 * models the behavior through linking certain ConcreteBehaviorStates and executing them
 */
protected abstract class BehaviorStateActor(data: StateData) extends Actor {

  protected val logger = Logging(context.system, this)

  protected val model = data.stateModel
  protected val id = model.id
  protected val userID = data.userID
  protected val subjectID = data.subjectID
  protected val stateText = model.text
  protected val startState = model.startState
  protected val transitions = model.transitions
  protected val internalBehaviorActor = data.internalBehaviorActor
  protected val processInstanceActor = data.processInstanceActor
  protected val inputPoolActor = data.inputPoolActor

  if (startState && !delaySubjectReady) {
    processInstanceActor ! SubjectStarted(userID, subjectID)
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

  protected def delaySubjectReady = false

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
      stateText,
      StateType.fromStateTypetoString(stateType),
      actionData)
  }
}

protected case class EndStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  // TODO direct beenden?
  internalBehaviorActor ! SubjectTerminated(userID, subjectID)

  override def postStop() {
    logger.debug("End@" + userID + ", " + subjectID + "stops...")
  }

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
        // TODO invalid input
        processInstanceActor ! ActionExecuted(ea)
      }
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def getAvailableAction: (StateType, Array[ActionData]) =
    (ActStateType,
      transitions.map((t: Transition) => ActionData(t.messageType, true)))

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
}

protected case class ReceiveStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  // convert the transitions into a map of extended transitions, to work with
  // this map in the whole actor
  private val transitionsMap: Map[(SubjectID, MessageType), ExtendedTransition] =
    transitions.map((t: Transition) =>
      ((t.subjectID, t.messageType), ExtendedTransition(t.subjectID, t.messageType, t.successorID)))
      .toMap[(SubjectID, MessageType), ExtendedTransition]

  // request if there is a message for this subject
  inputPoolActor !
    RequestForMessages(transitions.map(convertTransitionToRequest(_)))

  override def receive = {
    // execute an action
    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, ReceiveStateString, input) if ({
      // check if the related subject exists
      input.relatedSubject.isDefined && {
        val from = input.relatedSubject.get
        val messageType = input.text
        // check if the related transition exists
        transitionsMap.contains((from, messageType)) &&
          // only execute transitions, which are ready to execute
          transitionsMap((from, messageType)).ready
      }
    }) => {
      // get the transition from the map
      val transition = transitionsMap((input.relatedSubject.get, input.text))
      // create the Historymessage
      val message =
        HistoryMessage(transition.messageID, transition.messageType, transition.from, subjectID, transition.messageContent.get)
      // change the state and enter the history entry
      internalBehaviorActor ! ChangeState(id, transition.successorID, message)

      // inform the processinstance, that this action is executed
      processInstanceActor ! ActionExecuted(ea)
    }

    case sm: TransportMessage if (transitionsMap.contains((sm.from, sm.messageType))) => {
      // TODO checken ob richtige message
      logger.debug("Receive@" + userID + "/" + subjectID + ": Message \"" +
        sm.messageType + "\" from \"" + sm.from +
        "\" with content \"" + sm.messageContent + "\"")

      transitionsMap(sm.from, sm.messageType).addMessage(sm)

      trySendSubjectStarted()
    }

    case InputPoolEmpty => {
      // if startstate inform the processinstance that this subject has started
      trySendSubjectStarted()
    }

    case s => {
      super.receive(s)
    }
  }

  override protected def delaySubjectReady = true

  // only for startstate creation, check if subjectready should be sent
  var sendSubjectReady = startState
  private def trySendSubjectStarted() {
    if (sendSubjectReady) {
      processInstanceActor ! SubjectStarted(userID, subjectID)
      sendSubjectReady = false
    }
  }

  override protected def getAvailableAction: (StateType, Array[ActionData]) =
    (ReceiveStateType,
      (for ((k, t) <- transitionsMap) yield {
        ActionData(
          t.messageType,
          t.ready,
          relatedSubject = Some(t.from),
          messageContent = t.messageContent)
      }).toArray)

  /**
   * Creates the SubjectMessageRouting for a Transition
   */
  private def convertTransitionToRequest(transition: Transition) =
    SubjectMessageRouting(
      transition.subjectID,
      transition.messageType)

  /**
   * This case class extends an transition with information about the related message
   */
  private case class ExtendedTransition(
      from: SubjectID,
      messageType: MessageType,
      successorID: StateID) {

    var ready = false
    var messageID: MessageID = -1
    var messageContent: Option[MessageContent] = None

    def addMessage(message: TransportMessage) {
      // validate
      if (!(message.messageType == messageType && message.from == from)) {
        logger.error("Transportmessage is invalid to transition: " + message +
          ", " + this)
        return
      }

      ready = true
      messageID = message.messageID
      messageContent = Some(message.messageContent)
    }
  }
}

protected case class SendStateActor(data: StateData)
    extends BehaviorStateActor(data) {

  import scala.collection.mutable.{ Map => MutableMap }
  var messageContent: Option[String] = None
  val unsentMessageIDs: MutableMap[MessageID, Transition] =
    MutableMap[MessageID, Transition]()

  // TODO sowas wie timeout ist nicht drin
  // TODO message ids vllt nicht zufaellig
  override def receive = {
    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, SendStateString, input) if ({
      input.messageContent.isDefined
    }) => {
      if (!messageContent.isDefined) {
        // send subjectInternalMessage before sending executionAnswer to make sure that the executionAnswer 
        // can be blocked until a potentially new subject is created to ensure all available actions will 
        // be returned when asking
        messageContent = input.messageContent
        for (transition <- transitions) {
          val messageType = transition.messageType
          val toSubject = transition.subjectID
          val messageID = nextMessageID
          unsentMessageIDs(messageID) = transition
          processInstanceActor !
            SubjectInternalMessage(
              messageID,
              userID,
              subjectID,
              toSubject,
              messageType,
              messageContent.get)

          processInstanceActor ! ActionExecuted(ea)
        }
      } else {
        logger.error("2 send-message request received")
      }
    }

    case Stored(messageID) if ({
      messageContent.isDefined &&
        unsentMessageIDs.contains(messageID)
    }) => {
      val transition = unsentMessageIDs(messageID)
      // Create the history message
      val message =
        HistoryMessage(messageID, transition.messageType, subjectID, transition.subjectID, messageContent.get)
      // Change the state and enter the History entry
      internalBehaviorActor ! ChangeState(id, transition.successorID, message)
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
          !messageContent.isDefined,
          relatedSubject = Some(t.subjectID))))

  /**
   * Generates a new message ID
   */
  private def nextMessageID: Int = scala.util.Random.nextInt
}
