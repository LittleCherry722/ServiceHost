package de.tkip.sbpm.repo

import akka.actor.{ActorLogging, Actor}
import scala.collection.mutable
import java.io.File
import scala.io.Source
import spray.json._
import DefaultJsonProtocol._

class RepoActor extends Actor with ActorLogging {

  val templates = loadTemplates()
  val entries = mutable.Map[Int, JsObject]()

  private def loadTemplates() = {
    val folder = new File(getClass.getResource("/").toURI)
    val files = folder.listFiles().filter(_.getName.endsWith(".json"))

    files.foreach(f => log.info("found {}", f.getName))

    val jsonObjects = files.map(Source.fromFile(_).mkString.asJson.asJsObject)
    jsonObjects.map(obj => (obj.fields("name"), obj)).toMap
  }

  def receive = {
    case GetEntry(id) => {
      sender ! entries(id).prettyPrint
    }

    case GetEntries => {
      val list = entries.values.toList

      log.info("entries: {}", list.toJson.prettyPrint)

      sender ! list.toJson.prettyPrint
    }

    case CreateEntry(entry) => {
      val obj = entry.asJson.asJsObject
      val template = templates(obj.fields("name"))

      log.info("add from template: {}", template)

      entries(template.fields("id").toString.toInt) = template

      sender ! template.prettyPrint
    }
  }
}

case class GetEntry(id: Int)

case object GetEntries

case class CreateEntry(entry: String)
