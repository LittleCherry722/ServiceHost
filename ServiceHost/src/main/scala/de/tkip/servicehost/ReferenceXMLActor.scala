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

  case class Reference(processId: Int, subjectId: String, reference: String, json: String) {
    def toXml = scala.xml.Unparsed("<reference processid=\"" + processId + "\" subjectid=\"" + subjectId + "\" path=\"" + reference + "\" json=\"" + json + "\"/>\n")
  }

}

class ReferenceXMLActor extends InstrumentedActor {

  import ReferenceXMLActor.Reference

  private val xmlFilePath = "./src/main/resources/service_references.xml"
  val packet = "de.tkip.servicehost.serviceactor.stubgen"

  override def preStart: Unit = {
    log.debug("*******************  ReferenceXMLActor  *******************")
  }

  override def postStop {

  }

  override def preRestart(reason: Throwable, message: Option[Any]) {

  }

  override def postRestart(reason: Throwable) {

  }

  def wrappedReceive = {
    case createReference: CreateXMLReferenceMessage => {
      val ref: Reference = createXMLReference(createReference.subjectId, createReference.classPath, createReference.jsonPath)
      sender !! ref
    }

    case GetAllClassReferencesMessage => {
      sender !! getAllReferences
    }

    case getReference: GetClassReferenceMessageByProcessID => {
      sender !! getReferenceMessageByProcessID(getReference.processId)
    }

    case getReference: GetClassReferenceMessageBySubjectID => {
      sender !! getReferenceMessageBySubjectID(getReference.subjectId)
    }

    case AllService => {
      sender !! getAllReferences
    }

    case deleteService: DeleteService => {
      val failure = false
      val allService = getAllReferences()
      val newServices = allService.filter(re => re.processId != deleteService.serviceID)
      if(newServices.length < allService.length){
        val xmlContent =
          <references>
            {newServices.map(_.toXml)}
          </references>
        scala.xml.XML.save(xmlFilePath, xmlContent)
        sender ! true
      }else
        sender ! failure
    }

  }

  def getAllReferences(): List[Reference] = {
    var references: List[Reference] = List()
    val xmlFile = new File(xmlFilePath)
    if (xmlFile.exists()) {
      val src = Source.fromFile(xmlFile)
      val reader = new XMLEventReader(src)
      reader foreach {
        case EvElemStart(_, _, attrs, _) =>
          val map = attrs.asAttrMap
          if (map.contains("path"))
            references = references ::: List(Reference(map("processid").toInt, map("subjectid"), map("path"), map("json")))
        case _ =>
      }
    }
    references
  }

  def createXMLReference(subjectId: String, classPath: String, jsonPath: String): Reference = {
    val allOldReferences = getAllReferences

    var processId = if (allOldReferences.length > 0) {
      allOldReferences.reduceLeft((r1, r2) => if (r1.processId > r2.processId) r1 else r2).processId + 1
    } else {
      1
    }

    val ref = new Reference(processId, subjectId, classPath, jsonPath)

    log.info("adding " + ref + " to " + xmlFilePath)

    val references = allOldReferences :+ ref

    val xmlContent =
      <references>
        {references.map(_.toXml)}
      </references>

    scala.xml.XML.save(xmlFilePath, xmlContent)

    ref
  }

  def getReferenceMessageByProcessID(processId: Int): ClassReferenceMessage = {
    for {ref <- getAllReferences} {
      if (ref.processId == processId) return new ClassReferenceMessage(ref.subjectId, Class.forName(ref.reference).asInstanceOf[Class[ServiceActor]])
    }
    null
  }

  def getReferenceMessageBySubjectID(subjectId: String): ClassReferenceMessage = {
    for {ref <- getAllReferences} {
      if (ref.subjectId == subjectId) return new ClassReferenceMessage(ref.subjectId, Class.forName(ref.reference).asInstanceOf[Class[ServiceActor]])
    }
    null
  }

}
