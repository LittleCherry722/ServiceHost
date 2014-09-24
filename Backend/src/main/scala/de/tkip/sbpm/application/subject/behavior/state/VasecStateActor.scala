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
import de.tkip.sbpm.application.ProcessInstanceActor.{MappingInfo, AgentsMap, RegisterSubjects}
import de.tkip.sbpm.application.subject.CallMacroStates
import de.tkip.sbpm.application.subject.misc.MacroTerminated
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import akka.event.Logging

case class VasecStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  def splt(p: String): (Int, Int) = {
    val a = p.split("\\(")(1).split("\\)")(0).split(",")
    (a(0).toInt, a(1).toInt)
  }
  def cmp(x: String, y: String): Boolean = {
    val a = splt(x)
    val b = splt(y)
    (a._1 < b._1 || (a._1 == b._1 && a._2 < b._2))
  }
  def rnd(a: Int, b: Int, off: Int = 0): Int = {
    if (a > b) log.error("rnd: invalid input")
    a + scala.util.Random.nextInt(b-a+2*off) - off
  }

  val args: Array[String] = data.stateModel.text.split(":")

  if (args(0) == "GENERATE") {
    val startziel = args(1)
    val out = args(2)
    val num = args(3).toInt
    val typ = args(4)

    val v1: Option[Variable] = getVariable(startziel)
    log.error("SORT.v1: {}", v1)
    val a: Array[String] = v1.get.messages(v1.get.messages.length-1).messageContent.split(";")
    val (start, ziel) = (splt(a(0)),splt(a(1)))

    val data = scala.collection.mutable.ArrayBuffer[String]()

    for (i <- 1 to num) {
      val x = rnd(start._1, ziel._1, 10)
      val y = rnd(start._2, ziel._2, 10)

      data += "("+x+","+y+")"
    }

    val data_str = if (typ == "blue") {
      "[1|" + data.mkString(";") + "]"
    } else {
      data.mkString(";")
    }

    addVariable(out, data_str)
  }
  else if (args(0) == "JOIN") {
    val name = args(1)
    val sep = args(2)

    val v: Option[Variable] = removeVariable(name)

    val data = scala.collection.mutable.ArrayBuffer[String]()

    for (m <- v.get.messages) {
      data += m.messageContent
    }

    addVariable(name, data.mkString(sep))
  }
  else if (args(0) == "SELECT") {
    val from = args(1)
    val to = args(2)

    val v: Option[Variable] = removeVariable(from)
    log.error("SELECT.v: {}", v)

    val data = scala.collection.mutable.ArrayBuffer[String]()

    for (m <- v.get.messages) {
      data ++= m.messageContent.split("~")
    }

    val first = data.remove(0)
    log.error("MOVE.FIRST: {}", first)
    val x = first.split("\\[")(1).split("\\]")(0).split("\\|")(1).split(";")(0)

    addVariable(from, data.mkString("~"))
    addVariable(to, x)
  }
  else if (args(0) == "SORT") {
    val startziel = args(1)
    val from = args(2)
    val to = args(3)

    val v1: Option[Variable] = getVariable(startziel)
    log.error("SORT.v1: {}", v1)
    val a: Array[String] = v1.get.messages(v1.get.messages.length-1).messageContent.split(";")
    val (start, ziel) = (a(0),a(1))

    val v2: Option[Variable] = getVariable(from)
    log.error("SORT.v2: {}", v2)
    val points: Array[String] = v2.get.messages(v2.get.messages.length-1).messageContent.split(";")


    val sorted: Array[String] = points.sortWith(cmp)

    addVariable(to, start + ";" + sorted.mkString(";") + ";" + ziel)
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
