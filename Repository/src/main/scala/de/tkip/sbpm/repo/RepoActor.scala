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

object RepoActor {

  case class GetEntry(id: Int)

  case object GetEntries

  case class CreateEntry(entry: String)

  case object Reset

}

class RepoActor extends Actor with ActorLogging {

  import RepoActor._

  val entries = mutable.Map[Int, JsObject]()
  var currentId = 1

  def receive = {
    case GetEntry(id) => {
      sender ! entries.get(id).map(_.prettyPrint)
    }

    case GetEntries => {
      val list = entries.values.toList

      log.info("entries: {}", list.toJson.prettyPrint)

      sender ! list.toJson.prettyPrint
    }

    case CreateEntry(entry) => {
      println("CREATE ENTRY\n\n" + entry + "\n\n")
      val id =  nextId
      val convertedEntry = convertEntry(entry.asJson.asJsObject, id)

      entries(id) = convertedEntry
      sender ! Some(convertedEntry.prettyPrint)
    }

    case Reset => {
      log.info("resetting...")
      entries.clear()
    }
  }

  private def convertEntry(entry: JsObject, id: Int) = {
    val processId = entry.fields("processId")
    val url = entry.fields("url")
    val interfaceId = entry.fields("subjectId")
    val graph = entry.fields("graph").asJsObject
    val convertedGraph = convertGraph(id, graph, processId, url, interfaceId)

    var fields = entry.fields
    fields -= "processId"
    fields -= "url"
    fields -= "subjectId"
    fields += ("id" -> id.toJson)
    fields += ("date" -> System.currentTimeMillis.toJson)
    fields += ("graph" -> convertedGraph)

    entry.copy(fields)
  }

  private def convertGraph(id: Int, graph: JsObject, processId: JsValue, url: JsValue, interfaceId: JsValue) = {
    val oldId = graph.fields("id")
    val subjectId = "ext" + id

    var fields = graph.fields
    fields -= "id"
    fields += ("id" -> subjectId.toJson)
    fields += ("relatedSubject" -> oldId)
    fields += ("relatedSubject" -> oldId)
    fields += ("relatedInterface" -> interfaceId)
    fields += ("relatedProcess" -> processId)
    fields += ("url" -> url)

    fields += ("subjectType" -> "external".toJson)
    fields += ("externalType" -> "interface".toJson)

    graph.copy(fields)
  }

  private def nextId = {
    val id = currentId
    currentId += 1
    id
  }
}
