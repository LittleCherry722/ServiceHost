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

package de.tkip.sbpm.repo

import akka.actor.{ActorLogging, Actor}
import scala.collection.mutable
import spray.json._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.GraphJsonProtocol._
import spray.http.HttpIp

object RepoActor {
  case object GetAllInterfaces

  case class GetInterface(id: Int)

  case class AddInterface(ip: HttpIp, entry: String)

  case object Reset

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val addressFormat = jsonFormat2(Address)
    implicit val interfaceFormat = jsonFormat4(Interface)
  }
}

class RepoActor extends Actor with ActorLogging {
  import RepoActor.MyJsonProtocol._

  import RepoActor._

  val interfaces = mutable.Map[Int, Interface]()
  var currentId = 1

  def receive = {
    case GetAllInterfaces => {
      val list = interfaces.values.toList

      log.info("entries: {}", list.toJson.prettyPrint)

      sender ! list.map{addInterfaceImplementations(_)}.toJson.prettyPrint
    }

    case GetInterface(implId) => {
      val list = interfaces.values.toList
      val filtered = list.find { impl => impl.id == implId }.map{_.graph}

      log.info("entries for id: {}", filtered.toJson.prettyPrint)

      sender ! filtered.toJson.prettyPrint
    }

    case AddInterface(ip, entry) => {
      log.info("adding new interface")
      val entryJs = entry.asJson.asJsObject
      val interface = convertEntry(entryJs, ip)
      val id = interface.id
      interfaces(id) = interface
      sender ! Some(interface.toJson.prettyPrint)
    }

    case Reset => {
      log.info("resetting...")
      interfaces.clear()
    }
  }

  private def addInterfaceImplementations(interface: Interface) = {
    interface.copy(graph = interface.graph.copy(
      subjects = interface.graph.subjects.mapValues{
        subject => {
          val newImplementations = interfaces.values.toList.filter(interface => {
            val bList = interface.graph.subjects.values.toList.map(
              _.relatedInterfaceId == Some(interface.id)
            )
            bList.find(!_).getOrElse(false)
          })

          subject.copy(implementations = newImplementations.map(_.id))
        }
      }
    ))
  }

  private def getAddress(ip: HttpIp, entry: JsObject) = {
    val port = entry.fields("port")
    Address(ip.value, port.toString.toInt)
  }

  private def convertEntry(entry: JsObject, ip: HttpIp) = {
    var fields = entry.fields
    val graph = fields("graph").convertTo[Graph]
    val id = fields.getOrElse[JsValue]("interfaceId", nextId.toJson).convertTo[Int]
    val name = fields.getOrElse[JsValue]("name", "".toJson).convertTo[String]

    new Interface(getAddress(ip, entry), id, name, graph)
  }

  private def nextId = {
    val id = currentId
    currentId += 1
    id
  }
}
