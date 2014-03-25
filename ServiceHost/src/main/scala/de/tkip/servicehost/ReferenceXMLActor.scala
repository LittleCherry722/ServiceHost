package de.tkip.servicehost

import akka.actor.Actor
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.serviceactor._
import java.io.File
import scala.xml.pull.XMLEventReader
import scala.xml.pull.EvElemStart
import scala.io.Source

class ReferenceXMLActor extends Actor {

  class Reference(name: String, reference: String, json:String) {
    def toXml = scala.xml.Unparsed("<reference service=\"" + name + "\" path=\"" + reference + "\""+" json=\""+json+"\"/>\n")
  }

  private val xmlFilePath = "./src/main/resources/service_references.xml"
  val packet = "de.tkip.servicehost.serviceactor.stubgen"

  def receive: Actor.Receive = {
    case createReference: CreateXMLReferenceMessage => {
      createXMLReference(createReference.serviceID, createReference.classPath, createReference.jsonPath)
    }
    case getReference: GetClassReferenceMessage => {
      sender ! getReferenceMessage(getReference.serviceID)
    }
  }

  def createXMLReference(id: String, classPath: String, jsonPath: String) {
    val src = Source.fromFile(new File(xmlFilePath))
    val reader = new XMLEventReader(src)
    var references: List[Reference] = List()
    reader foreach {
      case EvElemStart(_, _, attrs, _) =>
        val map = attrs.asAttrMap
        if(map.contains("path"))
          references = references ::: List((map("service"), map("path"),map("json"))).map(refInstance)
      case _ =>
    }
    references = references :+ new Reference(id, classPath, jsonPath)
    val xmlContent =
      <references>
        { references.map(_.toXml) }
      </references>

    scala.xml.XML.save(xmlFilePath, xmlContent)
  }
  
  def refInstance(tuple:(String, String,String)): Reference ={
    new Reference(tuple._1, tuple._2, tuple._3)
  }
  
  def getReferenceMessage(id: String): ClassReferenceMessage = {
    val src = Source.fromFile(new File(xmlFilePath))
    val reader = new XMLEventReader(src)
    reader foreach {
      case EvElemStart(_, _, attrs, _) =>
        val map:Map[String,String] = attrs.asAttrMap
        if (map.getOrElse("service", null) == id) {
          return new  ClassReferenceMessage(id, Class.forName(map("path")).asInstanceOf[Class[ServiceActor]])
        }
      case _ =>
    }
    null
  }

}