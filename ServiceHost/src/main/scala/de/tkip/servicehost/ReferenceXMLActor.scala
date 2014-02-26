package de.tkip.servicehost

import akka.actor.Actor
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.serviceactor._

class ReferenceXMLActor extends Actor{
  val packet = "de.tkip.servicehost.serviceactor"
    //TODO create mapping from XML
  
  def receive: Actor.Receive = {
    case createReference: CreateXMLReferenceMessage => {
      //TODO implement 
    }
    case getReference: GetClassReferenceMessage =>{
      getReference.serviceID match {
        case "Staples" =>
//        	sender ! new ClassReferenceMessage(getReference.serviceID, classOf[StaplesServiceActor])
        	
        	sender ! new ClassReferenceMessage(getReference.serviceID, Class.forName(packet+".StaplesServiceActor").asInstanceOf[Class[ServiceActor]])
        case _ =>
          println(getReference.serviceID)
      }
      
    }
  }

}