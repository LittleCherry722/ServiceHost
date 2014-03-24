package de.tkip.servicehost

import akka.actor.Actor
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.serviceactor._
import java.io.File
import scala.xml.pull.XMLEventReader
import scala.xml.pull.EvElemStart
import scala.io.Source

object ReferenceXMLActor {

  class Reference(name: String, reference: String) {
    def toXml = scala.xml.Unparsed("<reference service=\"" + name + "\" path=\"" + reference + "\"/>\n")
  }

}

class ReferenceXMLActor extends Actor {
  import ReferenceXMLActor.Reference

  private val xmlFilePath = "./src/main/resources/service_references.xml"
  val packet = "de.tkip.servicehost.serviceactor.stubgen"

  def receive: Actor.Receive = {
    case createReference: CreateXMLReferenceMessage => {
      createXMLReference(createReference.serviceID, createReference.classPath)
    }
    case GetAllClassReferencesMessage => {
      sender ! getAllReferences
    }
    case getReference: GetClassReferenceMessage => {
      sender ! getReferenceMessage(getReference.serviceID)
    }
  }

  def getAllReferences(): List[Reference] = {
    val src = Source.fromFile(new File(xmlFilePath))
    val reader = new XMLEventReader(src)
    var references: List[Reference] = List()
    reader foreach {
      case EvElemStart(_, _, attrs, _) =>
        val map = attrs.asAttrMap
        if(map.contains("path"))
          references = references ::: List((map("service"), map("path"))).map(refInstance)
      case _ =>
    }
    references
  }

  def createXMLReference(id: String, classPath: String) {
    val references = getAllReferences :+ new Reference(id, classPath)

    val xmlContent =
      <references>
        { references.map(_.toXml) }
      </references>

    scala.xml.XML.save(xmlFilePath, xmlContent)
  }
  
  def refInstance(tuple:(String, String)): Reference ={
    new Reference(tuple._1, tuple._2)
  }
  
  def getReferenceMessage(id: String): ClassReferenceMessage = {
    val src = Source.fromFile(new File(xmlFilePath))
    val reader = new XMLEventReader(src)
    reader foreach {
      case EvElemStart(_, _, attrs, _) =>
        val list = attrs.asAttrMap.values.toList
        if (list.contains(id)) {
          if(list.indexOf(id)==0)
        	return new ClassReferenceMessage(id, Class.forName(list(1)).asInstanceOf[Class[ServiceActor]])
          else
            return new ClassReferenceMessage(id, Class.forName(list(0)).asInstanceOf[Class[ServiceActor]])
        }
      case _ =>
    }
    null
  }

}
