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
  subjectData: SubjectData,
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

  protected val blockingHandlerActor = data.subjectData.blockingHandlerActor
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
    if (!delayUnblockAtStart ) {
      internalStatus.subjectStartedSent = true
//      processInstanceActor ! SubjectStarted(userID, subjectID)
      // TODO so richtig?
      blockingHandlerActor ! UnBlockUser(userID)
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
  protected def delayUnblockAtStart = false

  /**
   * Changes the state and creates a history entry with the history message
   */
  protected def changeState(successorID: StateID, historyMessage: HistoryMessage) {
    blockingHandlerActor ! BlockUser(userID)
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






