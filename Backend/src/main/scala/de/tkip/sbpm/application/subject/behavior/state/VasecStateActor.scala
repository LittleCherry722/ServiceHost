/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2014 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application.subject.behavior.state

import scala.Array.canBuildFrom
import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.mutable.{Map => MutableMap}

import scala.util.Sorting

// TODO: sortieren / aufräumen
import de.tkip.sbpm.application.subject.misc.{ActionData, SubjectToSubjectMessage}
import akka.actor.Actor
import spray.json._
import DefaultJsonProtocol._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import scala.util.{Success, Failure}
import akka.actor.actorRef2Scala
import scala.concurrent.ExecutionContext.Implicits.global;

import scalaj.http.Http
import de.tkip.sbpm.persistence.query._

import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.rest.JsonProtocol._

import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.exitCondLabel
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.timeoutLabel
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.SubjectID
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.subject.misc.AvailableAction
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionIDProvider
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.ProcessInstanceActor.{AgentsMap, RegisterSubjects}
import de.tkip.sbpm.application.subject.CallMacroStates
import de.tkip.sbpm.application.subject.misc.MacroTerminated
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import akka.event.Logging

// TODO: rename? This has nothing to do with vasec anymore
case class VasecStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  val args: Array[String] = data.stateModel.text.split(":")

  if (args(0) == "CLEAR") {
    val name = args(1)

    removeVariable(name)
  }
  else if (args(0) == "JOIN") {
    val from1 = args(1)
    val from2 = args(2)
    val to = args(3)

    val v1: Option[Variable] = getVariable(from1)
    val v2: Option[Variable] = getVariable(from2)

    var data = Vector[JsValue]()

    if (v1.isDefined) for (m <- v1.get.messages) {
      data = data ++ m.messageContent.parseJson.asInstanceOf[JsArray].elements
    }

    if (v2.isDefined) for (m <- v2.get.messages) {
      data = data ++ m.messageContent.parseJson.asInstanceOf[JsArray].elements
    }

    if (to == from1) removeVariable(from1)
    if (to == from2) removeVariable(from2)

    addVariable(to, JsArray(data.toList).compactPrint)
  }
  else {
    log.error("NOT IMPLEMENTED. args(0): {}" + args(0))
  }

  exit()

  override protected def getAvailableAction: Array[ActionData] = {
    Array()
  }

  protected def stateReceive = {
    case x => log.error("NOT IMPLEMENTED, IGNORE: " + x)
  }

  def exit(): Unit = {
    log.info("exit")

    changeState(exitTransitions(0).successorID, data, null)
  }
}
