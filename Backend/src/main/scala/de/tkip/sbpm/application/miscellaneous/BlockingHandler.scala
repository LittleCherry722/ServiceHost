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

case class BlockUser(userID: UserID)
case class UnBlockUser(userID: UserID)
case class SendProcessInstanceCreated(userID: UserID)

class BlockingActor extends Actor {
  private type HasTargetUser = { def userID: UserID }
  private val userActors = MutableMap[UserID, UserBlockingActor]()
  private val logger = Logging(context.system, this)

  def receive = {
    case message: HasTargetUser => {
      handleMessage(message.userID, message)
    }
    case action: ActionExecuted => {
      handleMessage(action.ea.userID, action)
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
  private val blockedActions: ArrayBuffer[Any] =
    ArrayBuffer[Any]()

  def handleMessage: PartialFunction[Any, Unit] = {
    case action: ActionExecuted => {
      blockedActions += action
    }

    case created: SendProcessInstanceCreated => {
      blockedActions += created
    }

    case b: BlockUser => {
      remainingBlocks += 1
    }

    case b: UnBlockUser => {
      remainingBlocks -= 1
      if (remainingBlocks == 0) {
        for (action <- blockedActions) {
          context.parent ! action
        }
        blockedActions.clear()
      }
    }
  }
}

class UserBlockingHandler {
  //  def addListener: Unit
  //  def addCallbackFkt:Unit
  //  private var blocked = 0
  @volatile private var blocked: Int = 0

  def addCallback(callbackFunction: () => Unit) {
    // callback, sendback, feste methode?
  }

  def addSendback(actor: akka.actor.ActorRef, message: Any) {

  }

  // TODO vllt mit ids? message id, subjectid?
  def block {
    blocked += 1
  }

  def unblock {
    synchronized { // TODO synchronized?
      blocked -= 1
      // TODO check if < 0
      if (blocked == 0) {
        // TODO call the callback fkt
      }
    }
  }

  private def callback() { // sendback

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