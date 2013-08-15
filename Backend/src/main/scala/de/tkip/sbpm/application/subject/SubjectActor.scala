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
import scala.collection.mutable
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
import akka.pattern.ask
import akka.pattern.pipe
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.util.Timeout

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
  implicit val timeout = Timeout(2000)

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
  //  private val internalBehaviorActor =
  //    context.actorOf(Props(new InternalBehaviorActor(data, inputPoolActor)))

  // this map maps the Macro Names to the corresponding actors
  private val macroBehaviorActors = mutable.Map[String, InternalBehaviorRef]()
  private var macroIdCounter = 0

  private def insertMacro(callActor: Option[ActorRef], name: String) {
    val macroId = name + s"@$macroIdCounter"
    macroIdCounter += 1
    val entry @ (_, macroActor) =
      macroId -> context.actorOf(Props(new InternalBehaviorActor(data, inputPoolActor)))

    val macroStates = subject.macros(name)
    for (state <- macroStates) {
      macroActor ! state
    }

    macroBehaviorActors += entry

    macroActor ! StartMacroExecution
  }

  private def killMacro(macroId: String) {
    val behaviorActor = macroBehaviorActors(macroId)
  }

  private def restart() {
    macroBehaviorActors map (_._2 ! PoisonPill)
    macroBehaviorActors.clear()
    insertMacro(None, subject.mainMacroName)
  }

  override def preStart() {
    //    insertMacro(None, subject.mainMacroName)
    //    restart()
    // add all states in the internal behavior
    //    for (state <- subject.mainMacro) {
    //      internalBehaviorActor ! state
    //    }
  }

  def receive = {
    case sm: SubjectToSubjectMessage => {
      // a message from an other subject can be forwarded into the inputpool
      inputPoolActor.forward(sm)
    }

    case s: Stored => {
      // TODO:
    }

    case history.Transition(from, to, msg) => {
      // forward history entries from internal behavior up to instance actor
      context.parent !
        history.Entry(new Date(), subjectName, from, to, if (msg != null) Some(msg) else None)
    }
    case transition: history.NewHistoryTransitionData => {
      // forward history entries from internal behavior up to instance actor
      context.parent !
        history.NewHistoryEntry(new Date(), Some(userID), null, Some(subjectID), Some(transition), None)
    }

    case terminated: SubjectTerminated => {
      context.parent ! terminated
    }

    case _: StartSubjectExecution => {
      restart()
    }

    case gaa: GetAvailableAction => {
      // TODO to all macros
      // forward the request to the inputpool actor
      //      internalBehaviorActor ! gaa
      // TODO collect all actions of all macros
      // Create a Future with the available actions
      val actionFutures =
        Future.sequence(
          for ((_, c) <- macroBehaviorActors) yield (c ? gaa).mapTo[Seq[AvailableAction]])

      // and pipe the actions back to the sender
      actionFutures pipeTo sender
    }

    case action: ExecuteAction => {
      // TODO to the correct macro
      //      internalBehaviorActor.forward(action)
      // FIXME thats the wrong way!
      macroBehaviorActors map (_._2 forward action)
    }

    //    case br: SubjectBehaviorRequest => {
    //      // TODO to which macro?
    //      internalBehaviorActor.forward(br)
    //    }

    case message: SubjectProviderMessage => {
      // a message to the subject provider will be send over the process instance
      context.parent ! message
    }

    case s => {
      logger.error("SubjectActor " + userID + " does not support: " + s)
    }
  }
}
