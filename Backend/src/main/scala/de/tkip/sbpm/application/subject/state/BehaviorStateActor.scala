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
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.application.RequestUserID
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.event.Logging
import scala.collection.mutable.ArrayBuffer

/**
 * The data, which is necessary to create any state
 */
protected case class StateData(
  stateModel: State,
  userID: UserID,
  subjectID: SubjectID,
  internalBehaviorActor: InternalBehaviorRef,
  processInstanceActor: ProcessInstanceRef,
  inputPoolActor: ActorRef,
  internalStatus: InternalStatus)

// the message to signal, that a timeout has expired
private case object TimeoutExpired

/**
 * The actor to perform a timeout
 * waits the given time (in millis)
 * then informs the parent, that the timeout has expired
 * and kills itself
 */
private class TimeoutActor(time: Long) extends Actor {

  override def preStart() {
    // just wait the time
    Thread.sleep(time)
    // inform the parent
    context.parent ! TimeoutExpired
    // and kill this actor
    context.stop(self)
  }

  def receive = FSM.NullFunction

}

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
  protected val stateType = model.stateType
  protected val transitions = model.transitions
  protected val internalBehaviorActor = data.internalBehaviorActor
  protected val processInstanceActor = data.processInstanceActor
  protected val inputPoolActor = data.inputPoolActor
  protected val internalStatus = data.internalStatus
  protected val variables = internalStatus.variables
  protected val timeoutTransition = transitions.find(_.isTimeout)
  protected val exitTransitions = transitions.filter(_.isExitCond)

  override def preStart() {

    // if it is needed, send a SubjectStarted message
    if (startState && !delaySubjectReady && !internalStatus.subjectStartedSent) {
      internalStatus.subjectStartedSent = true
      processInstanceActor ! SubjectStarted(userID, subjectID)
    }

    // if the state has a(n automatic) timeout transition, start the timeout timer
    if (timeoutTransition.isDefined) {
      val stateTimeout = timeoutTransition.get.myType.asInstanceOf[TimeoutCond]
      if (!stateTimeout.manual) {
        context.actorOf(Props(new TimeoutActor(stateTimeout.duration * 1000)))
      }
    }
  }

  // first try the "receive" function of the inheritance state
  // then use the "receive" function of this behavior state
  final def receive = generalReceive orElse stateReceive orElse errorReceive

  // the inheritance state must implement this function
  protected def stateReceive: Receive

  // the receive of this behavior state, it will be executed
  // if the state-receive does not match
  private def generalReceive: Receive = {
    case ga: GetAvailableAction => {
      sender ! createAvailableAction(ga.processInstanceID)
    }

    case TimeoutExpired => {
      executeTimeout()
    }

    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, _, input) if ({
      input.transitionType == timeoutLabel
    }) => {
      executeTimeout()
      processInstanceActor ! ActionExecuted(ea)
    }
  }

  private def errorReceive: Receive = {
    case message: AnswerAbleMessage => {
      message.sender ! akka.actor.Status.Failure(new IllegalArgumentException("Invalid input."))
    }

    case action: ExecuteAction => {
      logger.error("/" + userID + "/" + subjectID + "/" +
        id + " does not support " + action)
    }

    case s => {
      logger.error("BehaviorStateActor does not support: " + s)
    }
  }

  /**
   * Executes a timeout by executing the timeout edge
   *
   * override this function to execute an other transition when a timeout appears
   */
  protected def executeTimeout() {
    if (timeoutTransition.isDefined) {
      changeState(timeoutTransition.get.successorID, null)
    }
  }

  /**
   * This function returns if the subjectready message should be delayed,
   * default value is false
   *
   * override this function to delay the subject ready message
   *
   * @return whether the subject ready message should be delayed
   */
  protected def delaySubjectReady = false

  /**
   * Changes the state and creates a history entry with the history message
   */
  protected def changeState(successorID: StateID, historyMessage: HistoryMessage) {
    internalBehaviorActor ! ChangeState(id, successorID, internalStatus, historyMessage)
  }

  /**
   * Returns the available actions of the state
   */
  protected def getAvailableAction: Array[ActionData]

  /**
   * Creates the Available Action, which belongs to this state
   */
  protected def createAvailableAction(processInstanceID: ProcessInstanceID) = {
    var actionData = getAvailableAction
    if (timeoutTransition.isDefined) {
      actionData ++= Array(ActionData("timeout", true, timeoutLabel))
    }

    AvailableAction(
      userID,
      processInstanceID,
      subjectID,
      id,
      stateText,
      stateType.toString(),
      actionData)
  }
}

protected case class EndStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  // Inform the processinstance that this subject has terminated
  internalBehaviorActor ! SubjectTerminated(userID, subjectID)

  // nothing to receive for this state
  protected def stateReceive = FSM.NullFunction

  override def postStop() {
    logger.debug("End@" + userID + ", " + subjectID + "stops...")
  }

  override protected def getAvailableAction: Array[ActionData] =
    Array()
}

protected case class ActStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  protected def stateReceive = {

    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, ActStateString, input) => {
      val index = indexOfInput(input.text)
      if (index != -1) {
        changeState(exitTransitions(index).successorID, null)
        processInstanceActor ! ActionExecuted(ea)
      } else {
        // TODO invalid input
      }
    }
  }

  override protected def getAvailableAction: Array[ActionData] =
    exitTransitions.map((t: Transition) => ActionData(t.messageType, true, exitCondLabel))

  private def indexOfInput(input: String): Int = {
    var i = 0
    for (t <- exitTransitions) {
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
  private val exitTransitionsMap: Map[(SubjectID, MessageType), ExtendedExitTransition] =
    exitTransitions.map((t: Transition) =>
      ((t.subjectID, t.messageType), new ExtendedExitTransition(t)))
      .toMap[(SubjectID, MessageType), ExtendedExitTransition]

  // register to subscribe the messages at the inputpool
  inputPoolActor ! {
    // convert the transition array into the request array
    for (transition <- exitTransitions if (transition.target.isDefined)) yield {
      // maximum number of messages the state is able to process
      val count = transition.target.get.max
      // the register-message for the inputpool
      SubscribeIncomingMessages(id, transition.subjectID, transition.messageType, count)
    }
  }

  protected def stateReceive = {
    // execute an action
    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, ReceiveStateString, input) if ({
      // check if the related subject exists
      input.relatedSubject.isDefined && {
        val from = input.relatedSubject.get
        val messageType = input.text
        // check if the related transition exists
        exitTransitionsMap.contains((from, messageType)) &&
          // only execute transitions, which are ready to execute
          exitTransitionsMap((from, messageType)).ready
      }
    }) => {

      // get the transition from the map
      val transition = exitTransitionsMap((input.relatedSubject.get, input.text))
      // create the Historymessage
      val message =
        HistoryMessage(transition.messageID, transition.messageType, transition.from, subjectID, transition.messageContent.get)
      // change the state and enter the history entry
      changeState(transition.successorID, message)

      // inform the processinstance, that this action is executed
      processInstanceActor ! ActionExecuted(ea)
    }

    case sm: SubjectToSubjectMessage if (exitTransitionsMap.contains((sm.from, sm.messageType))) => {
      logger.debug("Receive@" + userID + "/" + subjectID + ": Message \"" +
        sm.messageType + "\" from \"" + sm.from +
        "\" with content \"" + sm.messageContent + "\"")

      exitTransitionsMap(sm.from, sm.messageType).addMessage(sm)

      val t = exitTransitionsMap(sm.from, sm.messageType).transition
      val varID = t.storeVar
      if (t.storeToVar) {
        variables.getOrElseUpdate(varID, Variable(varID)).addMessage(sm)
        System.err.println(variables.mkString("VARIABLES: {\n", "\n", "}")) //TODO
      }
    }

    case InputPoolSubscriptionPerformed => {
      // if this state is the startstate inform the processinstance,
      // that this subject has started
      trySendSubjectStarted()
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

  override protected def executeTimeout() {
    val exitTransition =
      exitTransitionsMap.map(_._2).filter(_.ready).map(_.transition)
        .reduceOption((t1, t2) => if (t1.priority < t2.priority) t1 else t2)

    if (exitTransition.isDefined) {
      // TODO richtige historymessage
      changeState(exitTransition.get.successorID, null)
    } else {
      super.executeTimeout()
    }
  }

  override protected def getAvailableAction: Array[ActionData] =
    (for ((k, t) <- exitTransitionsMap) yield {
      ActionData(
        t.messageType,
        t.ready,
        exitCondLabel,
        relatedSubject = Some(t.from),
        messageContent = t.messageContent, // TODO delete
        messages = Some(t.messages))
    }).toArray

  override protected def changeState(successorID: StateID, historyMessage: HistoryMessage) {
    // inform the inputpool, that this state is not waiting for messages anymore
    inputPoolActor ! UnSubscribeIncomingMessages(id)

    // change the state
    super.changeState(successorID, historyMessage)
  }

  /**
   * This case class extends an transition with information about the related message
   */
  private class ExtendedExitTransition(val transition: Transition) {
    val from: SubjectID = transition.subjectID
    val messageType: MessageType = transition.messageType
    val successorID: StateID = transition.successorID

    var ready = false
    var messageID: MessageID = -1
    var messageContent: Option[MessageContent] = None

    val messageData: ArrayBuffer[MessageData] = ArrayBuffer[MessageData]()

    def messages = messageData.toArray

    private var remaining = transition.target.get.min

    def addMessage(message: SubjectToSubjectMessage) {
      // validate
      if (!(message.messageType == messageType && message.from == from)) {
        logger.error("Transportmessage is invalid to transition: " + message +
          ", " + this)
        return
      }

      remaining -= 1
      ready = remaining <= 0

      // TODO auf mehrere messages umbauen, anstatt immer nur die letzte
      messageID = message.messageID
      messageContent = Some(message.messageContent)

      messageData += MessageData(message.userID, message.messageContent, message.fileID)
    }
  }
}

protected case class SendStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  import scala.collection.mutable.{ Map => MutableMap }

  private var remainingStored = 0
  private var messageContent: Option[String] = None
  private val unsentMessageIDs: MutableMap[MessageID, Transition] =
    MutableMap[MessageID, Transition]()

  // TODO
  private val sendTransition: Transition =
    transitions.find(_.isExitCond).get
  private val sendExitCond = sendTransition.myType.asInstanceOf[ExitCond]
  private val sendTarget = sendExitCond.target.get

  //  override def preStart() {
  // TODO so ist das noch nicht, besser machen!
  // ask the ContextResolver for the targetIDs
  // store them in a val
  implicit val timeout = Timeout(2000)
  val future =
    (ActorLocator.contextResolverActor
      ? (RequestUserID(
        SubjectInformation(sendTransition.subjectID),
        _.toArray)))
  val userIDs = Await.result(future, timeout.duration).asInstanceOf[Array[UserID]]
  System.err.println(userIDs.mkString(", "));

  //  }

  protected def stateReceive = {
    case ea @ ExecuteAction(userID, processInstanceID, subjectID, stateID, SendStateString, input) if ({
      // the message needs a content
      input.messageContent.isDefined
    }) => {
      if (!messageContent.isDefined) {
        // send subjectInternalMessage before sending executionAnswer to make sure that the executionAnswer 
        // can be blocked until a potentially new subject is created to ensure all available actions will 
        // be returned when asking
        messageContent = input.messageContent
        for (transition <- exitTransitions if transition.target.isDefined) yield {
          val messageType = transition.messageType
          val toSubject = transition.subjectID
          val messageID = nextMessageID
          unsentMessageIDs(messageID) = transition

          logger.debug("Send@" + userID + "/" + subjectID + ": Message[" +
            messageID + "} \"" + messageType + "to " + transition.target +
            "\" with content \"" + messageContent.get + "\"")

          val target = transition.target.get
          if (target.toVariable) {
            target.insertVariable(variables(target.variable.get))
          }

          if (userIDs.length == target.min == target.max) {
            target.insertTargetUsers(userIDs)
          } else if (input.targetUsersData.isDefined) {
            val targetUserData = input.targetUsersData.get
            // TODO validate?
            target.insertTargetUsers(targetUserData.targetUsers)
          } else {
            // TODO error?
          }

          remainingStored += target.min

          // TODO send to ausgewaehlten users
          processInstanceActor !
            SubjectToSubjectMessage(
              messageID,
              userID,
              subjectID,
              target,
              messageType,
              messageContent.get,
              input.fileId)

          processInstanceActor ! ActionExecuted(ea)
        }
      } else {
        logger.error("Second send-message action request received")
      }
    }

    case Stored(messageID) if ({
      messageContent.isDefined &&
        unsentMessageIDs.contains(messageID)
      // TODO might remove the message ID from unsentMessageIDs?
    }) => {
      val transition = unsentMessageIDs(messageID)
      // Create the history message
      val message =
        HistoryMessage(messageID, transition.messageType, subjectID, transition.subjectID, messageContent.get)
      // Change the state and enter the History entry
      remainingStored -= 1
      if (remainingStored == 0) {
        changeState(transition.successorID, message)
      }
    }
  }

  // TODO only send targetUserData when its not trivial
  override protected def getAvailableAction: Array[ActionData] =
    Array(
      ActionData(
        sendTransition.messageType,
        !messageContent.isDefined && userIDs.length >= sendTarget.min,
        exitCondLabel,
        relatedSubject = Some(sendTransition.subjectID),
        targetUsersData =
          Some(TargetUser(sendTarget.min, sendTarget.max, userIDs))))

  /**
   * Generates a new message ID
   */
  private def nextMessageID: Int = MessageIDProvider.nextMessageID()
}
