package de.tkip.sbpm.repo

import akka.actor.{ActorLogging, Actor}
import scala.collection.mutable
import java.io.File
import scala.io.Source
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

  val templates = loadTemplates()
  val entries = mutable.Map[Int, JsObject]()

  private def loadTemplates() = {
    val filenames = Seq( "lieferant.json", "test.json", "staples.json" )
    val files = filenames.map( x => Source.fromInputStream(getClass.getResourceAsStream("/interfaces/" + x)) )

    val jsonObjects = files.map(_.mkString.asJson.asJsObject)
    jsonObjects.map(obj => (obj.fields("name"), obj)).toMap
  }

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
      val obj = entry.asJson.asJsObject
      val template = templates.get(obj.fields("name"))

      template match {
        case Some(t) => {
          log.info("add from template: {}", t)

          entries(t.fields("id").toString.toInt) = t

          sender ! Some(t.prettyPrint)
        }

        case None => sender ! None
      }
    }

    case Reset => {
      log.info("resetting...")
      entries.clear()
    }
  }
}
