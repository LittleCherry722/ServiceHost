/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
import de.tkip.sbpm.application.subject.misc.ActionExecuted

case class BlockUser(userID: UserID)
case class UnBlockUser(userID: UserID)
case class SendProcessInstanceCreated(userID: UserID)

class BlockingActor extends Actor {
  private type HasTargetUser = { def userID: UserID }
  private val userActors = MutableMap[UserID, UserBlocker]()
  private val logger = Logging(context.system, this)

  def receive = {
    // TODO Combine to 1 message
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
    userActors.getOrElseUpdate(userID, new UserBlocker(userID))
      .handleMessage(message)
  }
}

private class UserBlocker(userID: UserID)(implicit val context: ActorContext) {
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
