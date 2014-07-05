package de.tkip.servicehost

import akka.actor.Actor
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.serviceactor._
import de.tkip.sbpm.logging.DefaultLogging
import java.io.File
import scala.xml.pull.XMLEventReader
import scala.xml.pull.EvElemStart
import scala.io.Source

object ReferenceXMLActor {

  case class Reference(name: String, reference: String, json: String) {
    def toXml = scala.xml.Unparsed("<reference service=\"" + name + "\" path=\"" + reference + "\" json=\"" + json + "\"/>\n")
  }

}

class ReferenceXMLActor extends Actor with DefaultLogging {
  import ReferenceXMLActor.Reference

  private val xmlFilePath = "./src/main/resources/service_references.xml"
  val packet = "de.tkip.servicehost.serviceactor.stubgen"

  def receive: Actor.Receive = {
    case createReference: CreateXMLReferenceMessage => {
      val ref: Reference = createXMLReference(createReference.serviceID, createReference.classPath, createReference.jsonPath)
      sender ! ref
    }
    case GetAllClassReferencesMessage => {
      sender ! getAllReferences
    }
    case getReference: GetClassReferenceMessage => {
      sender ! getReferenceMessage(getReference.serviceID)
    }
  }

  def getAllReferences(): List[Reference] = {
    var references: List[Reference] = List()
    val xmlFile = new File(xmlFilePath)
    if(xmlFile.exists()) {
      val src = Source.fromFile(xmlFile)
      val reader = new XMLEventReader(src)
      reader foreach {
        case EvElemStart(_, _, attrs, _) =>
          val map = attrs.asAttrMap
          if(map.contains("path"))
            references = references ::: List(Reference(map("service"), map("path"), map("json")))
        case _ =>
      }
    }
    references
  }

  def createXMLReference(id: String, classPath: String, jsonPath: String): Reference = {
    val ref = new Reference(id, classPath, jsonPath)

    log.info("adding " + ref + " to " + xmlFilePath)

    val references = getAllReferences :+ ref

    val xmlContent =
      <references>
        { references.map(_.toXml) }
      </references>

    scala.xml.XML.save(xmlFilePath, xmlContent)

    ref
  }
  
  def getReferenceMessage(id: String): ClassReferenceMessage = {
    for {ref <- getAllReferences} {
      if (ref.name == id) return new ClassReferenceMessage(id, Class.forName(ref.reference).asInstanceOf[Class[ServiceActor]])
    }
    null
  }

}
