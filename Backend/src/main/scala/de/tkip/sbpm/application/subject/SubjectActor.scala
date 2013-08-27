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

package de.tkip.sbpm.application.subject

import java.util.Date
import akka.actor._
import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import akka.event.Logging
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.misc.Stored

case class SubjectData(
  userID: UserID,
  processID: ProcessID,
  processInstanceID: ProcessInstanceID,
  processInstanceActor: ProcessInstanceRef,
  blockingHandlerActor: ActorRef,
  subject: SubjectLike)

/**
 * contains and manages an InputPoolActor(Mailbox) and an InternalBehaviourActor
 */
class SubjectActor(data: SubjectData) extends Actor {
  private val logger = Logging(context.system, this)

  // extract the information out of the input
  private val subject: Subject = data.subject match {
    case s: Subject => s
    case _ =>
      throw new IllegalArgumentException("A Subjectactor need a Subject as data")
  }
  private val userID = data.userID

  private val subjectID: SubjectID = subject.id
  private val subjectName: String = subject.id
  // create the inputpool
  private val inputPoolActor: ActorRef =
    context.actorOf(Props(new InputPoolActor(data)))
  // and the internal behavior
  private val internalBehaviorActor =
    context.actorOf(Props(new InternalBehaviorActor(data, inputPoolActor)))

  override def preStart() {
    // add all states in the internal behavior
    for (state <- subject.states) {
      internalBehaviorActor ! state
    }
  }

  def receive = {
    case sm: SubjectToSubjectMessage => {
      // a message from an other subject can be forwarded into the inputpool
      inputPoolActor.forward(sm)
    }
    
    case s: Stored => {
      // TODO:
    }

    case transition: history.NewHistoryTransitionData => {
      // forward history entries from internal behavior up to instance actor
      context.parent !
        history.NewHistoryEntry(new Date(), Some(userID), null, Some(transition), None)
    }

    case terminated: SubjectTerminated => {
      context.parent ! terminated
    }

    case gaa: GetAvailableActions => {
      if (gaa.userID == userID) {
        // forward the request to the inputpool actor
        internalBehaviorActor ! gaa
      }
    }

    case br: SubjectBehaviorRequest => {
      internalBehaviorActor.forward(br)
    }

    case message: SubjectProviderMessage => {
      // a message to the subject provider will be send over the process instance
      context.parent ! message
    }

    case s => {
      logger.error("SubjectActor " + userID + " does not support: " + s)
    }
  }
}
