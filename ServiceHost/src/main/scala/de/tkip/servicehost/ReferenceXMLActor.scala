package de.tkip.servicehost

import java.io.File

import akka.actor.Actor
import scala.io.Source
import scala.xml.pull.XMLEventReader
import scala.xml.pull.EvElemStart

import de.tkip.servicehost.Messages._
import de.tkip.servicehost.serviceactor._
import de.tkip.sbpm.instrumentation.InstrumentedActor

object ReferenceXMLActor {

  case class Reference(subjectId: String, reference: String, json: String) {
    def toXml = scala.xml.Unparsed("<reference subjectid=\"" + subjectId + "\" path=\"" + reference + "\" json=\"" + json + "\"/>\n")
  }

}

class ReferenceXMLActor extends InstrumentedActor {
  import ReferenceXMLActor.Reference

  private val xmlFilePath = "./src/main/resources/service_references.xml"
  val packet = "de.tkip.servicehost.serviceactor.stubgen"

  def wrappedReceive = {
    case createReference: CreateXMLReferenceMessage => {
      val ref: Reference = createXMLReference(createReference.subjectId, createReference.classPath, createReference.jsonPath)
      sender !! ref
    }
    case GetAllClassReferencesMessage => {
      sender !! getAllReferences
    }
    case getReference: GetClassReferenceMessage => {
      println("########################" + getReference.subjectId )
      sender !! getReferenceMessage(getReference.subjectId)
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
            references = references ::: List(Reference(map("subjectid"), map("path"), map("json")))
        case _ =>
      }
    }
    references
  }

  def createXMLReference(subjectId: String, classPath: String, jsonPath: String): Reference = {
    val ref = new Reference(subjectId, classPath, jsonPath)

    log.info("adding " + ref + " to " + xmlFilePath)

    val references = getAllReferences :+ ref

    val xmlContent =
      <references>
        { references.map(_.toXml) }
      </references>

    scala.xml.XML.save(xmlFilePath, xmlContent)

    ref
  }
  
  def getReferenceMessage(subjectId: String): ClassReferenceMessage = {
    for {ref <- getAllReferences} {
      if (ref.subjectId == subjectId) return new ClassReferenceMessage(subjectId, Class.forName(ref.reference).asInstanceOf[Class[ServiceActor]])
    }
    null
  }

}
