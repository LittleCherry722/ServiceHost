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
    val convertedGraph = convertGraph(graph, processId, url, interfaceId)

    var fields = entry.fields
    fields -= "processId"
    fields -= "url"
    fields -= "subjectId"
    fields += ("id" -> id.toJson)
    fields += ("graph" -> convertedGraph)

    entry.copy(fields)
  }

  private def convertGraph(graph: JsObject, processId: JsValue, url: JsValue, interfaceId: JsValue) = {
    val id = graph.fields("id")

    var fields = graph.fields
    fields -= "id"
    fields += ("relatedSubject" -> id)
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
