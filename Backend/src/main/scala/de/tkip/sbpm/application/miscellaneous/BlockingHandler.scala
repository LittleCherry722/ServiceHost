package de.tkip.sbpm.application.miscellaneous

import scala.collection.mutable.{ ArrayBuffer, Map => MutableMap }
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application.ProcessInstanceActor
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorContext
import akka.event.Logging
import akka.actor.IllegalActorStateException

case class BlockUser(userID: UserID)
case class UnBlockUser(userID: UserID)
case class SendProcessInstanceCreated(userID: UserID)

class BlockingActor extends Actor {
  private type HasTargetUser = { def userID: UserID }
  private val userActors = MutableMap[UserID, UserBlockingActor]()
  private val logger = Logging(context.system, this)

  def receive = {
    case action: ActionExecuted => {
      handleMessage(action.ea.userID, action)
    }
    case message @ BlockUser(userID) => {
      handleMessage(userID, message)
    }
    case message @ UnBlockUser(userID) => {
      handleMessage(userID, message)
    }
    case message @ SendProcessInstanceCreated(userID) => {
      handleMessage(userID, message)
    }
    case s => {
      logger.error("BlockingActor got message " + s)
    }
  }

  private def handleMessage(userID: UserID, message: Any) {
    userActors.getOrElseUpdate(userID, new UserBlockingActor(userID))
      .handleMessage(message)
  }
}

class UserBlockingActor(userID: UserID)(implicit val context: ActorContext) {
  private var remainingBlocks = 0
  private val blockedMessages: ArrayBuffer[Any] =
    ArrayBuffer[Any]()

  def handleMessage: PartialFunction[Any, Unit] = {
    case action: ActionExecuted => {
      blockedMessages += action
      trySendBlockedMessages()
    }

    case created: SendProcessInstanceCreated => {
      blockedMessages += created
      trySendBlockedMessages()
    }

    case b: BlockUser => {
      remainingBlocks += 1
    }

    case b: UnBlockUser => {
      remainingBlocks -= 1
      trySendBlockedMessages()
    }
  }

  /**
   * If there are no remaining blocks on this user,
   * this method will send all messages, which are remaining, and clear
   * the message pool
   */
  private def trySendBlockedMessages() {
    System.err.println(userID + "/ BLOCKS: " + remainingBlocks);
    System.err.println("MESSAGES: " + blockedMessages.mkString(", "));

    if (remainingBlocks == 0) {
      for (message <- blockedMessages) {
        context.parent ! message
      }
      blockedMessages.clear()
    } else if (remainingBlocks < 0) {
      throw new Exception("More unblocks than blocks for user " + userID)
    }
  }
}

class BlockingHandler(createExecuteActionAnswer: (ExecuteAction) => Unit) {

  // variables to help blocking of ActionExecuted messages
  private var waitingForContextResolver = ArrayBuffer[UserID]()
  private var waitingUserMap = Map[UserID, Int]()
  private var blockedAnswers = collection.mutable.Map[UserID, ActionExecuted]() // TODO mehrere actionexe..

  /**
   * This method checks if all subjects are parsed and ready to ask for actions
   * etc.
   */
  def allSubjectsReady(userID: UserID): Boolean = {
    !waitingForContextResolver.contains(userID) && waitingUserMap.getOrElse(userID, 1) == 0
  }

  /**
   * adds the userID to the waiting list for answers of the contextResolver
   */
  def waitForContextResolver(userID: UserID) {
    // set userID on waiting list for answer of contextresolver
    waitingForContextResolver += userID
  }

  /**
   * increases the number of tasks that are blocking the given userID from sending ExecuteActionAnswers by one
   */
  def blockUserID(userID: UserID) {
    waitingUserMap += userID -> (waitingUserMap.getOrElse(userID, 0) + 1)
    // println("blockuser: " + waitingUserMap.mkString(","))
  }

  /**
   * decrease the number of tasks that are blocking the given userID from sending ExecuteActionAnswers by one
   */
  def unblockUserID(userID: UserID) {
    val numberOfTasks = (waitingUserMap.getOrElse(userID, 1) - 1)
    waitingUserMap += userID -> (if (numberOfTasks < 0) 0 else numberOfTasks)
    // println("after unblocked: " + waitingUserMap.mkString(","))
  }

  /**
   * handle contextResolverAnswer to ensure blocking of ExecuteActionAnswers until all subjects (owned by the
   * subjectProvider that created the ExecuteAction request) have been created and started
   */
  def handleBlockingForSubjectCreation(userID: UserID) {
    if (waitingForContextResolver.size == 0) {
      return
    }
    // block user twice. once for subject creation and once for message delivery  
    blockUserID(userID)
    //    blockUserID(userID)

    // println("contextResolver: " + waitingForContextResolver.mkString(","))

    tryToReleaseBlocking(waitingForContextResolver.head)

    // delete userID from waiting list for answers of the contextresolver
    waitingForContextResolver = waitingForContextResolver.tail

    // println("contextResolver: " + waitingForContextResolver.mkString(","))
  }

  def handleBlockingForMessageDelivery(userID: UserID) {
    blockUserID(userID)
    // println("blockingForDelivery: " + waitingUserMap.mkString(","))
  }

  /**
   * handles SubjectStartedMessages and checks if no other task is blocking
   * if thats the case -> forward message else wait
   */
  def tryToReleaseBlocking(userID: UserID) {
    // if the given userID has no tasks that are blocking it -> forward message if one exists
    if (allSubjectsReady(userID) && blockedAnswers.contains(userID)) {
      createExecuteActionAnswer(blockedAnswers(userID).ea)
      // println("forward: " + blockedAnswers.mkString(","))
      blockedAnswers -= userID
    }
  }

  def handleUnblocking(userID: UserID) {
    unblockUserID(userID)
    tryToReleaseBlocking(userID)
  }

  def storeActionExecuted(message: ActionExecuted) {
    blockedAnswers += message.ea.userID -> message
  }

}