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
import de.tkip.sbpm.application.ProcessInstanceActor.{MappingInfo, AgentsMap, RegisterSubjects}
import de.tkip.sbpm.application.subject.CallMacroStates
import de.tkip.sbpm.application.subject.misc.MacroTerminated
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import akka.event.Logging

trait VPoint {
  def x: Int
  def y: Int
}

case class VSinglePoint(x: Int, y: Int) extends VPoint
case class VStartEnd(start: VSinglePoint, end: VSinglePoint)
case class VRoute(points: Seq[VSinglePoint])
case class VRoutes(routes: Seq[VRoute])

case class VGreenPoint(x: Int, y: Int) extends VPoint
case class VRedPoint(x: Int, y: Int, r: Int) extends VPoint

case class VBluePoint(x: Int, y: Int) extends VPoint
case class VBlueGroup(num: Int, points: Seq[VBluePoint])

object VasecJsonProtocol extends DefaultJsonProtocol {
  implicit def vPointToVSinglePointConversion(p: VPoint): VSinglePoint = VSinglePoint(p.x, p.y)
  implicit def vPointToVGreenPointConversion(p: VPoint): VGreenPoint = VGreenPoint(p.x, p.y)
  
  implicit val vSinglePointFormat = jsonFormat2(VSinglePoint)
  implicit val vStartEndFormat = jsonFormat2(VStartEnd)
  implicit val vRouteFormat = jsonFormat1(VRoute)

  implicit val vGreenPointFormat = jsonFormat2(VGreenPoint)
  implicit val vRedPointFormat = jsonFormat3(VRedPoint)
  implicit val vBluePointFormat = jsonFormat2(VBluePoint)
  implicit val vBlueGroupFormat = jsonFormat2(VBlueGroup)
}

case class VasecStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  import VasecJsonProtocol._

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
    val startEnd: VStartEnd = v1.get.messages(v1.get.messages.length-1).messageContent.parseJson.convertTo[VStartEnd]

    val data = scala.collection.mutable.ArrayBuffer[VPoint]()

    for (i <- 1 to num) {
      val x = rnd(startEnd.start.x, startEnd.end.x, 10)
      val y = rnd(startEnd.start.y, startEnd.end.y, 10)

      if (typ == "green") {
        data += VGreenPoint(x, y)
      }
      else if (typ == "red") {
        val r = rnd(1, 10)
        data += VRedPoint(x, y, r)
      }
      else if (typ == "blue") {
        data += VBluePoint(x, y)
      }
    }

    if (typ == "green") {
      addVariable(out, data.toList.asInstanceOf[List[VGreenPoint]].toJson.compactPrint)
    }
    else if (typ == "red") {
      addVariable(out, data.toList.asInstanceOf[List[VRedPoint]].toJson.compactPrint)
    }
    else if (typ == "blue") {
      val group = VBlueGroup(1, data.toList.asInstanceOf[List[VBluePoint]])
      addVariable(out, Array(group).toJson.compactPrint)
    }
  }
  else if (args(0) == "CLEAR") {
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

    addVariable(to, JsArray(data.toList).compactPrint)
  }
  else if (args(0) == "SELECT") {
    val from = args(1)
    val to = args(2)

    val v: Option[Variable] = removeVariable(from)
    log.error("SELECT.v: {}", v)

    val data = scala.collection.mutable.ArrayBuffer[VBlueGroup]()

    for (m <- v.get.messages) {
      data ++= m.messageContent.parseJson.convertTo[Array[VBlueGroup]]
    }

    val first = data.remove(0)
    log.error("MOVE.FIRST: {}", first)
    val x: VBluePoint = first.points(0)
    val xgreen: VGreenPoint = x

    addVariable(from, data.toList.toJson.compactPrint)
    addVariable(to, Array(xgreen).toJson.compactPrint)
  }
  else if (args(0) == "GENROUTE") {
    val startziel = args(1)
    val from = args(2)
    val to = args(3)

    val v1: Option[Variable] = getVariable(startziel)
    log.error("GENROUTE.v1: {}", v1)
    val startEnd: VStartEnd = v1.get.messages(v1.get.messages.length-1).messageContent.parseJson.convertTo[VStartEnd]

    val v2: Option[Variable] = getVariable(from)
    log.error("GENROUTE.v2: {}", v2)
    def convert(l: Array[VGreenPoint]): Array[VSinglePoint] = l map { a => a: VSinglePoint }
    val points: Array[VSinglePoint] = convert(v2.get.messages(v2.get.messages.length-1).messageContent.parseJson.convertTo[Array[VGreenPoint]])

    def compare(a: VPoint, b: VPoint): Int = {
      import scala.math.Ordered.orderingToOrdered
      (a.x, a.y) compare (b.x, b.y)
    }
    val sorted: Array[VSinglePoint] = points.sortWith((a, b) => (compare(a, b) < 0))

    val route: VRoute = VRoute(Array(startEnd.start) ++ sorted ++ Array(startEnd.end))

    addVariable(to, Array(route).toJson.compactPrint)
  }
  else if (args(0) == "INTERSECTS") {
    val route = args(1)
    val red = args(2)
    val to = args(3)

    var intersects = false


    if (intersects) {
      addVariable(to, "TRUE")
    }
    else {
      addVariable(to, "FALSE")
    }
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
