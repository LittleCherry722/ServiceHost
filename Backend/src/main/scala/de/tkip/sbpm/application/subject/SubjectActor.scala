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
import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.actor._
import java.util.UUID
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
import de.tkip.sbpm.application.subject.misc.DisableNonObserverStates
import de.tkip.sbpm.application.subject.misc.KillNonObserverStates
import de.tkip.sbpm.application.ProcessInstanceActor.RegisterSubjects
import akka.actor.Status.Failure

case class CallMacro(callActor: ActorRef, name: String)
case class CallMacroStates(callActor: ActorRef, name: String, macroStates: Array[State])

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
class SubjectActor(data: SubjectData) extends InstrumentedActor {
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
    context.actorOf(Props(new InputPoolActor(data)), "InputPoolActor____" + UUID.randomUUID().toString())
  // and the internal behavior
  //  private val internalBehaviorActor =
  //    context.actorOf(Props(new InternalBehaviorActor(data, inputPoolActor)),"InternalBehaviorActor____"+UUID.randomUUID().toString())

  // this map maps the Macro Names to the corresponding actors
  private val macroBehaviorActors = mutable.Map[String, InternalBehaviorRef]()
  private var macroIdCounter = 0

  private def insertMacro(callActor: Option[ActorRef], name: String, macroStates: Array[State]) {
    log.debug(s"Starting macro $name")
    val macroId = name + s"@$macroIdCounter"
    macroIdCounter += 1
    val entry @ (_, macroActor) =
      macroId -> context.actorOf(Props(
        new InternalBehaviorActor(macroId, callActor, data, inputPoolActor)), "InternalBehaviorActor____" + UUID.randomUUID().toString())

    // TODO: send all states at once?
    for (state <- macroStates) {
      macroActor ! state
    }

    macroBehaviorActors += entry

    log.debug(s"Started macro $name with id $macroId")

    macroActor ! StartMacroExecution
  }

  private def killMacro(macroId: String) {
    val behaviorActor = macroBehaviorActors(macroId)

    behaviorActor ! PoisonPill
    macroBehaviorActors -= macroId
  }

  private def killAll() {
    for ((a, actorRef) <- macroBehaviorActors) {
      actorRef ! PoisonPill
    }
    //    macroBehaviorActors map (_._2 ! PoisonPill)
    macroBehaviorActors.clear()
  }

  private def restart() {
    killAll()
    insertMacro(None, subject.mainMacroName, subject.mainMacro.states)
  }

  override def preStart() {
    //    insertMacro(None, subject.mainMacroName)
    //    restart()
    // add all states in the internal behavior
    //    for (state <- subject.mainMacro) {
    //      internalBehaviorActor ! state
    //    }
  }

  def wrappedReceive = {

    case sm: SubjectToSubjectMessage => {
      for { (key, name) <- subject.variablesMap } {
        for (a <- macroBehaviorActors.values) {
          log.info(s"send addVariable '$name' to '$a'")
          a ! AddVariable(name, sm)
        }
      }

      // a message from an other subject can be forwarded into the inputpool
      inputPoolActor.forward(sm)
    }

    case s: Stored => {
      // TODO:
    }

    case s @ KillNonObserverStates => {
      for ((a, actorRef) <- macroBehaviorActors) {
        actorRef ! s
      }
      //      macroBehaviorActors.map(_._2 ! s)
    }

    case s @ DisableNonObserverStates => {
      for ((a, actorRef) <- macroBehaviorActors) {
        actorRef ! s
      }
      //      macroBehaviorActors.map(_._2 ! s)
    }

    case transition: history.NewHistoryTransitionData => {
      val message = history.NewHistoryEntry(new Date(), Some(userID), null, Some(subjectID), Some(transition), None)
      // forward history entries from internal behavior up to instance actor
      context.parent ! message
    }

    case MacroTerminated(macroId) => {
      // TODO if its the mainmacro, kill everything
      if (macroId.contains(subject.mainMacroName)) {
        log.debug("Subject Terminated")
        killAll()
        val message = SubjectTerminated(userID, subjectID)
        context.parent ! message
      } else {
        log.debug(s"Macro terminated $macroId")
        killMacro(macroId)
      }
    }

    case _: StartSubjectExecution => {
      restart()
    }

    case CallMacro(callActor, name) => {
      val macroStates: Array[State] = if (!subject.macros.contains(name)) {
          // TODO was tun?
          log.error(s"Trying to call macro $name, but it is not available.")
          Array()
        } else {
          subject.macros(name).states
        }

      insertMacro(Some(callActor), name, macroStates)
    }

    case CallMacroStates(callActor, name, macroStates) => {
      insertMacro(Some(callActor), name, macroStates)
    }

    case gaa: GetAvailableAction => {
      // Create a Future with the available actions
      val actionFutures =
        Future.sequence(
          for ((_, c) <- macroBehaviorActors) yield (c ?? gaa).mapTo[Seq[AvailableAction]])

      // and pipe the actions back to the sender
      actionFutures pipeTo sender
    }

    case action: ExecuteAction => {
      if (macroBehaviorActors.contains(action.macroID)) {
        // route the action to the correct macro
        macroBehaviorActors(action.macroID) forward action
      } else {
        if (action.isInstanceOf[AnswerAbleMessage]) {
          val message = Failure(new IllegalArgumentException(
            "Invalid Argument: The macro does not exist"))
          val to = action.asInstanceOf[AnswerAbleMessage].sender
          to ! message
        }
      }
    }

    case message: SubjectProviderMessage => {
      // a message to the subject provider will be send over the process instance
      context.parent ! message
    }

    case m: RegisterSubjects => {
      context.parent forward m
    }

    case s => {
      log.error("SubjectActor " + userID + " does not support: " + s)
    }
  }
}
