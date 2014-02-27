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
import DefaultJsonProtocol._
import spray.http.HttpIp

object RepoActor {
  case object GetAllInterfaces

  case class GetInterface(id: ID)

  case class AddInterface(ip: HttpIp, entry: String)

  case object Reset

  case class Interface(address: Address,
                       id: ID,
                       name: String,
                       graph: JsObject)
  case class Address(ip: String, port: Int)
  type ID = Int

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val addressFormat = jsonFormat2(Address)
    implicit val interfaceFormat = jsonFormat4(Interface)
  }
}

class RepoActor extends Actor with ActorLogging {
  import RepoActor.MyJsonProtocol._

  import RepoActor._

  val interfaces = mutable.Map[ID, mutable.Set[Interface]]()
  var currentId = 1

  def receive = {
    case GetAllInterfaces => {
      val list = interfaces.values.fold(mutable.Set.empty) { (a, b) => a ++ b }.toList

      log.info("entries: {}", list.map{_.graph}.toJson.prettyPrint)

      sender ! list.toJson.prettyPrint
    }

    case GetInterface(implId) => {
      val list = interfaces.values.fold(mutable.Set.empty) { (a, b) => a ++ b }.toList
      val filtered = list.find { impl => impl.id == implId }.map{_.graph}

      log.info("entries for id: {}", filtered.toJson.prettyPrint)

      sender ! filtered.toJson.prettyPrint
    }

    case AddInterface(ip, entry) => {
      log.info("adding new interface")
      val entryJs = entry.asJson.asJsObject
      val id = entryJs.fields.getOrElse("interfaceId", nextId.toJson).convertTo[Int]
      val name = entryJs.fields.getOrElse("name", "").toString()
      val ce = convertEntry(entryJs, id)
      val interface = Interface(getAddress(ip, entryJs), id, name, ce)
      interfaces(id) = (interfaces.getOrElse(id, mutable.Set[Interface]()) += interface)
      sender ! Some(interface.graph.toJson.prettyPrint)
    }

    case Reset => {
      log.info("resetting...")
      interfaces.clear()
    }
  }

  private def getAddress(ip: HttpIp, entry: JsObject) = {
    val port = entry.fields("port")
    Address(ip.value, port.toString.toInt)
  }

  private def convertEntry(entry: JsObject, id: Int) = {
    val graph = entry.fields("graph").asJsObject
    val convertedGraph = convertGraph(id, graph)

    var fields = entry.fields
    fields -= "port"
    fields -= "url"
    fields -= "id"
    fields += ("id" -> id.toJson)
    fields += ("date" -> System.currentTimeMillis.toJson)
    fields += ("graph" -> convertedGraph)

    entry.copy(fields)
  }

  private def convertGraph(id: Int, graph: JsObject) = {
    var fields = graph.fields
    fields -= "id"
    fields += ("id" -> nextId.toJson)
    graph.copy(fields)
  }

  private def nextId = {
    val id = currentId
    currentId += 1
    id
  }
}
